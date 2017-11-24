package com.revolsys.elevation.gridded.usgsdem;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.DoubleArrayGriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelReader;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.LinearUnit;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.Projection;
import com.revolsys.geometry.cs.ProjectionParameterNames;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.cs.esri.EsriCoordinateSystems;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.io.FileUtil;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.InputStreamResource;
import com.revolsys.spring.resource.NoSuchResourceException;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class UsgsGriddedElevationReader extends BaseObjectWithProperties
  implements GriddedElevationModelReader {

  private final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

  private final byte[] bytes = new byte[1024];

  private final Resource resource;

  private ReadableByteChannel channel;

  private double resolutionZ;

  private int rasterColCount;

  private int rasterRowCount;

  private int resolutionX;

  private GeometryFactory geometryFactory = GeometryFactory.wgs84();

  private double[] polygonBounds;

  private BoundingBox boundingBox = BoundingBox.empty();

  private boolean initialized;

  public UsgsGriddedElevationReader(final Resource resource,
    final Map<String, ? extends Object> properties) {
    this.resource = resource;
    setProperties(properties);
  }

  @Override
  public void close() {
    try {
      this.channel.close();
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private double fromDms(final double value) {
    final double degrees = Math.floor(value / 10000);
    final double minutes = Math.floor(Math.abs(value) % 10000 / 100);
    final double seconds = Math.abs(value) % 100;
    final double decimal = degrees + minutes / 60 + seconds / 3600;
    return decimal;
  }

  @Override
  public BoundingBox getBoundingBox() {
    init();
    return this.boundingBox;
  }

  private byte getByte() {
    final String string = getString(1);
    return Byte.valueOf(string);
  }

  protected ReadableByteChannel getChannel() {

    final String fileExtension = this.resource.getFileNameExtension();
    try {
      if (fileExtension.equals("zip")) {
        final ZipInputStream in = this.resource.newBufferedInputStream(ZipInputStream::new);
        final String fileName = this.resource.getBaseName();
        final String baseName = FileUtil.getBaseName(fileName);
        final String projName = baseName + ".prj";
        for (ZipEntry zipEntry = in.getNextEntry(); zipEntry != null; zipEntry = in
          .getNextEntry()) {
          final String name = zipEntry.getName();
          if (name.equals(projName)) {
            final String wkt = FileUtil.getString(new InputStreamReader(in, StandardCharsets.UTF_8),
              false);
            final GeometryFactory geometryFactory = EsriCoordinateSystems.getGeometryFactory(wkt);
            if (geometryFactory != null) {
              this.geometryFactory = geometryFactory;
            }
          } else if (name.equals(fileName)) {
            return new InputStreamResource(in).newReadableByteChannel();
          }
        }
        throw new IllegalArgumentException("Cannot find " + fileName + " in " + this.resource);
      } else if (fileExtension.equals("gz")) {
        final String baseName = this.resource.getBaseName();
        setGeometryFactory(this.resource.getParent().newChildResource(baseName));
        final InputStream in = this.resource.newBufferedInputStream();
        final GZIPInputStream gzIn = new GZIPInputStream(in);
        return new InputStreamResource(gzIn).newReadableByteChannel();
      } else {
        setGeometryFactory(this.resource);
        return this.resource.newReadableByteChannel();
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to open: " + this.resource, e);
    }
  }

  private Double getDouble(final int length) {
    final String string = getString(length);
    return Double.parseDouble(string);
  }

  private double getDouble12() {
    final String string = getString(12);
    return Double.parseDouble(string);
  }

  private double getDouble24() {
    final String string = getString(24);
    return Double.valueOf(string);
  }

  private Double getDoubleSci() {
    String string = getString(24);
    string = string.replace("D", "E");
    return Double.valueOf(string);
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public double getGridCellSize() {
    init();
    return this.resolutionX;
  }

  private int getInteger() {
    final String string = getString(6);
    return Integer.valueOf(string);
  }

  private int getInteger(final int length) {
    final String string = getString(length);
    return Integer.valueOf(string);
  }

  private double[] getPolygonCoordinates() {
    final int polygonSides = getInteger();
    final double[] bounds = new double[polygonSides * 2 + 2];
    int i = 0;
    for (; i < bounds.length - 2; i++) {
      bounds[i] = getDouble24();
    }
    bounds[i++] = bounds[0];
    bounds[i++] = bounds[1];
    return bounds;
  }

  private short getShort() {
    final String string = getString(5);
    return Short.valueOf(string);
  }

  private String getString(final int length) {
    this.buffer.get(this.bytes, 0, length);
    int offset = 0;
    while (offset < length && this.bytes[offset] == ' ') {
      offset++;
    }
    int size = length;
    while (size > offset + 1 && this.bytes[size - 1] == ' ') {
      size--;
    }
    final String s = new String(this.bytes, offset, size - offset, StandardCharsets.US_ASCII);
    return s;
  }

  private void init() {
    if (!this.initialized) {
      this.initialized = true;
      this.channel = getChannel();
      if (this.channel == null) {
        throw new NoSuchResourceException(this.resource);
      }
      readHeader();
    }
  }

  @Override
  public final GriddedElevationModel read() {
    init();
    try {

      final double[][] raster = new double[this.rasterColCount][];
      while (readBuffer()) {
        final int rowIndex = getInteger() - 1;
        int columnIndex = getInteger() - 1;

        final int rowCount = getInteger();
        final int colCount = getInteger();
        double[] yElevations = raster[columnIndex];
        if (yElevations == null) {
          yElevations = new double[rowIndex + rowCount];
          raster[columnIndex] = yElevations;
          Arrays.fill(yElevations, 0, rowIndex, Double.NaN);
        }

        final double x1 = getDouble24();
        final double y1 = getDouble24();
        final double z1 = getDouble24();
        final double minZ = getDouble24();
        final double maxZ = getDouble24();

        this.rasterRowCount = Math.max(this.rasterRowCount, rowIndex + rowCount);
        for (int i = 0; i < rowCount; i++) {
          final int value;
          if (i < 146) {
            value = getInteger();
          } else {
            final int offset = (i - 146) % 170;
            if (offset == 0) {
              readBuffer();
            }
            value = getInteger();
          }
          final double elevation = z1 + value * this.resolutionZ;
          if (elevation > -32767) {
            yElevations[rowIndex + i] = elevation;
          } else {
            yElevations[rowIndex + i] = Double.NaN;
          }
        }
        columnIndex++;
      }

      final DoubleArrayGriddedElevationModel elevationModel = new DoubleArrayGriddedElevationModel(
        this.geometryFactory, this.boundingBox.getMinX(), this.boundingBox.getMinY(),
        this.rasterColCount, this.rasterRowCount, this.resolutionX);
      elevationModel.setResource(this.resource);
      elevationModel.clear();

      for (int cellX = 0; cellX < raster.length; cellX++) {
        final double[] yElevations = raster[cellX];
        for (int cellY = 0; cellY < yElevations.length; cellY++) {
          final double elevation = yElevations[cellY];
          if (!Double.isNaN(elevation)) {
            elevationModel.setElevation(cellX, cellY, elevation);
          }
        }
      }
      return elevationModel;
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  public boolean readBuffer() throws IOException {
    this.buffer.clear();
    int totalReadCount = 0;
    while (totalReadCount < 1024) {
      final int readCount = this.channel.read(this.buffer);
      if (readCount == -1) {
        if (totalReadCount == 0) {
          return false;
        } else {
          final int position = this.buffer.position();
          this.buffer.flip();
          return position == 1024;
        }
      } else {
        totalReadCount += readCount;
      }
    }
    this.buffer.flip();
    return true;
  }

  private void readHeader() {
    try {
      if (readBuffer()) {
        final String fileName = getString(40);
        final String descriptor = getString(40);
        skip(29); // Blank 81 - 109
        final String seGeographic = getString(26);
        final String processCode = getString(1);
        skip(1);// Blank 137
        final String sectionIndicator = getString(3);
        final String originCode = getString(4);
        final int demLevelCode = getInteger();
        final int elevationPattern = getInteger();
        final int planimetricReferenceSystem = getInteger();
        final int zone = getInteger();
        final double[] projectionParameters = new double[15];
        for (int i = 0; i < projectionParameters.length; i++) {
          projectionParameters[i] = getDoubleSci();
        }
        final int planimetricUom = getInteger();
        final int verticalUom = getInteger();
        this.polygonBounds = getPolygonCoordinates();

        final double min = getDouble24();
        final double max = getDouble24();
        final double angle = getDouble24();
        if (angle != 0) {
          throw new IllegalArgumentException(
            "Angle=" + angle + " not currently supported for USGS DEM: " + this.resource);
        }
        final int verticalAccuracy = getInteger();
        final double resolutionX = getDouble12();
        final double resolutionY = getDouble12();
        if (resolutionX != resolutionY) {
          throw new IllegalArgumentException("resolutionX " + resolutionX + " != " + resolutionY
            + " resolutionY for USGS DEM: " + this.resource);
        }
        this.resolutionX = (int)resolutionX;
        if (resolutionX != this.resolutionX) {
          throw new IllegalArgumentException("resolutionX " + resolutionX
            + " must currently be an integer for USGS DEM: " + this.resource);
        }
        this.resolutionZ = getDouble12();
        this.rasterRowCount = getInteger();
        this.rasterColCount = getInteger();
        final Short largestContourInterval = getShort();
        final Byte largestContourIntervalUnits = getByte();
        final Short smallestContourInterval = getShort();
        final Byte smallest = getByte();
        final Integer sourceYear = getInteger(4);
        final Integer revisionYear = getInteger(4);
        final String inspectionFlag = getString(1);
        final String dataValidationFlag = getString(1);
        final String suspectAndVoidAreaFlag = getString(1);
        final Integer verticalDatum = getInteger(2);
        final Integer horizontalDatum = getInteger(2);
        final Integer dataEdition = getInteger(4);
        final Integer percentVoid = getInteger(4);
        final Integer edgeMatchWest = getInteger(2);
        final Integer edgeMatchNorth = getInteger(2);
        final Integer edgeMatchEast = getInteger(2);
        final Integer edgeMatchSouth = getInteger(2);
        final Double verticalDatumShift = getDouble(7);

        LinearUnit linearUnit = null;
        if (planimetricUom == 1) {
          linearUnit = EpsgCoordinateSystems.getLinearUnit("foot");
        } else if (planimetricUom == 2) {
          linearUnit = EpsgCoordinateSystems.getLinearUnit("metre");
        }
        GeographicCoordinateSystem geographicCoordinateSystem;
        switch (horizontalDatum) {
          case 0:
            geographicCoordinateSystem = null;
          break;
          case 1:
            geographicCoordinateSystem = (GeographicCoordinateSystem)EpsgCoordinateSystems
              .getCoordinateSystem("NAD27");
          break;
          case 2:
            geographicCoordinateSystem = (GeographicCoordinateSystem)EpsgCoordinateSystems
              .getCoordinateSystem("WGS 72");
          break;
          case 3:
            geographicCoordinateSystem = (GeographicCoordinateSystem)EpsgCoordinateSystems
              .getCoordinateSystem("WGS 84");
          break;
          case 4:
            geographicCoordinateSystem = (GeographicCoordinateSystem)EpsgCoordinateSystems
              .getCoordinateSystem("NAD83");
          break;

          default:
            throw new IllegalArgumentException("horizontalDatum=" + horizontalDatum
              + " not currently supported for USGS DEM: " + this.resource);
        }
        if (1 == planimetricReferenceSystem) {
          final int coordinateSystemId = 26900 + zone;
          this.geometryFactory = GeometryFactory.floating2d(coordinateSystemId);
        } else if (2 == planimetricReferenceSystem) {
          throw new IllegalArgumentException(
            "planimetricReferenceSystem=" + planimetricReferenceSystem
              + " not currently supported for USGS DEM: " + this.resource);
        } else if (3 == planimetricReferenceSystem) {
          final MapEx parameters = new LinkedHashMapEx();
          parameters.put(ProjectionParameterNames.LONGITUDE_OF_CENTER,
            fromDms(projectionParameters[4]));
          parameters.put(ProjectionParameterNames.STANDARD_PARALLEL_1,
            fromDms(projectionParameters[2]));
          parameters.put(ProjectionParameterNames.STANDARD_PARALLEL_2,
            fromDms(projectionParameters[3]));
          parameters.put(ProjectionParameterNames.LATITUDE_OF_CENTER,
            fromDms(projectionParameters[5]));
          parameters.put(ProjectionParameterNames.FALSE_EASTING, projectionParameters[6]);
          parameters.put(ProjectionParameterNames.FALSE_NORTHING, projectionParameters[7]);

          final Projection projection = EpsgCoordinateSystems.getProjection("Albers_Equal_Area");
          final ProjectedCoordinateSystem projectedCoordinateSystem = new ProjectedCoordinateSystem(
            -1, "", geographicCoordinateSystem, null, projection, parameters, linearUnit, null,
            null, false);
          final ProjectedCoordinateSystem projectedCoordinateSystem2 = (ProjectedCoordinateSystem)EpsgCoordinateSystems
            .getCoordinateSystem(projectedCoordinateSystem);
          if (projectedCoordinateSystem2 == projectedCoordinateSystem
            || projectedCoordinateSystem2 == null) {
            this.geometryFactory = GeometryFactory.floating2d(projectedCoordinateSystem2);
          } else {
            final int coordinateSystemId = projectedCoordinateSystem2.getCoordinateSystemId();
            this.geometryFactory = GeometryFactory.floating2d(coordinateSystemId);
          }
        } else {
          throw new IllegalArgumentException(
            "planimetricReferenceSystem=" + planimetricReferenceSystem
              + " not currently supported for USGS DEM: " + this.resource);
        }
        final Polygon polygon = this.geometryFactory
          .polygon(this.geometryFactory.linearRing(2, this.polygonBounds));
        this.boundingBox = polygon.getBoundingBox();
      }
    } catch (final Exception e) {
      try {
        close();
      } catch (final Exception e1) {
      }
      throw Exceptions.wrap(e);
    }
  }

  private void setGeometryFactory(final Resource resource) {
    final GeometryFactory geometryFactory = EsriCoordinateSystems.getGeometryFactory(resource);
    if (geometryFactory != null) {
      this.geometryFactory = geometryFactory;
    }
  }

  private void skip(final int count) {
    this.buffer.position(this.buffer.position() + count);
  }

  @Override
  public String toString() {
    return this.resource.toString();
  }

}
