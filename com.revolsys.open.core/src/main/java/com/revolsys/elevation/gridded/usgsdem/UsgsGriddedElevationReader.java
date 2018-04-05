package com.revolsys.elevation.gridded.usgsdem;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelReader;
import com.revolsys.geometry.cs.CompoundCoordinateSystem;
import com.revolsys.geometry.cs.CoordinateOperationMethod;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.HorizontalCoordinateSystem;
import com.revolsys.geometry.cs.ParameterName;
import com.revolsys.geometry.cs.ParameterNames;
import com.revolsys.geometry.cs.ParameterValue;
import com.revolsys.geometry.cs.ParameterValueNumber;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.VerticalCoordinateSystem;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.cs.unit.LinearUnit;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.InputStreamResource;
import com.revolsys.spring.resource.NoSuchResourceException;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class UsgsGriddedElevationReader extends BaseObjectWithProperties
  implements GriddedElevationModelReader {

  private BoundingBox boundingBox = BoundingBox.empty();

  private final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

  private final byte[] bytes = new byte[1024];

  private ReadableByteChannel channel;

  private final double[] cornersX = new double[4];

  private final double[] cornersY = new double[4];

  private GeometryFactory geometryFactory = GeometryFactory.wgs84();

  private int gridHeight;

  private int gridWidth;

  private boolean initialized;

  private int resolutionX;

  private int resolutionY;

  private double resolutionZ;

  private final Resource resource;

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

  private ParameterValue fromDms(final double value) {
    final double degrees = Math.floor(value / 10000);
    final double minutes = Math.floor(Math.abs(value) % 10000 / 100);
    final double seconds = Math.abs(value) % 100;
    final double decimal = degrees + minutes / 60 + seconds / 3600;
    return new ParameterValueNumber(decimal);
  }

  @Override
  public BoundingBox getBoundingBox() {
    init();
    return this.boundingBox;
  }

  private byte getByte1() {
    final String string = getString(1);
    if (string.isEmpty()) {
      return 0;
    } else {
      return Byte.valueOf(string);
    }
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
            final GeometryFactory geometryFactory = GeometryFactory.floating3d(wkt);
            if (geometryFactory.isHasHorizontalCoordinateSystem()) {
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

  private double getDms() {
    final int degrees = getInteger(4);
    final double minutes = getInteger(2);
    final double seconds = getDoubleSci(7);
    final double fractional = minutes / 60 + seconds / 3600;
    if (degrees < 0) {
      return degrees - fractional;
    } else {
      return degrees + fractional;
    }
  }

  private Double getDoubleSci(final int length) {
    String string = getString(length);
    if (string.isEmpty()) {
      return null;
    } else {
      string = string.replace('D', 'E');
      return Double.valueOf(string);
    }
  }

  private int getGeographicCoordinateSystemId(final Integer horizontalDatum) {
    int geographicCoordinateSystemId;
    switch (horizontalDatum) {
      case 1: // NAD 27
        geographicCoordinateSystemId = 4267;
      break;
      case 2: // WGS 72
        geographicCoordinateSystemId = 4322;
      break;
      case 3: // WGS 84
        geographicCoordinateSystemId = 4326;
      break;
      case 4: // NAD 83
        geographicCoordinateSystemId = 4269;
      break;

      default:
        throw new IllegalArgumentException("horizontalDatum=" + horizontalDatum
          + " not currently supported for USGS DEM: " + this.resource);
    }
    return geographicCoordinateSystemId;
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

  private int getInteger(final int length) {
    final String string = getString(length);
    if (string.isEmpty()) {
      return 0;
    } else {
      return Integer.valueOf(string);
    }
  }

  private short getShort5() {
    final String string = getString(5);
    if (string.isEmpty()) {
      return 0;
    } else {
      return Short.valueOf(string);
    }
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

  private int getVerticalCoordinateSystemId(final Integer verticalDatum) {
    int verticalCoordinateSystemId;
    switch (verticalDatum) {
      case 1: // Mean Sea Level
        verticalCoordinateSystemId = 5714;
      break;
      case 2: // NGVD_1929
        verticalCoordinateSystemId = 5702;
      break;
      case 3: // NAVD_1988
        verticalCoordinateSystemId = 5703;
      break;

      default:
        throw new IllegalArgumentException("verticalDatum=" + verticalDatum
          + " not currently supported for USGS DEM: " + this.resource);
    }
    return verticalCoordinateSystemId;
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
      final UsgsGriddedElevationModel elevationModel = new UsgsGriddedElevationModel(
        this.geometryFactory, this.boundingBox, this.gridWidth, this.gridHeight, this.resolutionX);

      final double minY = this.boundingBox.getMinY();
      int gridHeight = 0;
      int columnCount = 0;
      final double yShift = this.resolutionY / 2.0;
      while (columnCount < this.gridWidth && readBuffer()) {
        final int rowIndex = getInteger(6) - 1;
        final int columnIndex = getInteger(6) - 1;

        final int rowCount = getInteger(6);

        final int colCount = getInteger(6);
        columnCount += colCount;

        final double x = getDoubleSci(24);
        final double y = getDoubleSci(24) + yShift;

        final int gridYMax = rowIndex + rowCount;
        if (gridYMax > gridHeight) {
          gridHeight = gridYMax;
        }

        final double zOffset = getDoubleSci(24);
        final double minZ = getDoubleSci(24);
        final double maxZ = getDoubleSci(24);

        for (int i = 0; i < colCount; i++) {
          final int[] elevations = new int[rowCount];
          final int gridX = columnIndex + i;
          for (int j = 0; j < rowCount; j++) {
            if (j > 145) {
              final int offset = (j - 146) % 170;
              if (offset == 0) {
                readBuffer();
              }
            }
            final int value = getInteger(6);
            final double elevation = zOffset + value * this.resolutionZ;
            int elevationInt;
            if (elevation > -32767) {
              elevationInt = this.geometryFactory.toIntZ(elevation);
            } else {
              elevationInt = Integer.MIN_VALUE;
            }
            elevations[j] = elevationInt;
          }
          final double deltaY = y - minY;
          final int gridY = (int)Math.floor(deltaY / this.resolutionY) - 1;
          elevationModel.setColumn(gridX, gridY, elevations);
        }
      }

      elevationModel.setResource(this.resource);

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

  @SuppressWarnings("unused")
  private void readHeader() {
    try {
      if (readBuffer()) {
        final String fileName = getString(40);
        final String descriptor = getString(40);
        skip(29); // Blank 81 - 109
        final double maxLon = getDms();
        final double minLat = getDms();
        final String processCode = getString(1);
        skip(1);// Blank 137
        final String sectionIndicator = getString(3);
        final String originCode = getString(4);
        final int demLevelCode = getInteger(6);
        final int elevationPattern = getInteger(6);
        final int planimetricReferenceSystem = getInteger(6);
        final int zone = getInteger(6);
        final double[] projectionParameters = new double[15];
        for (int i = 0; i < projectionParameters.length; i++) {
          projectionParameters[i] = getDoubleSci(24);
        }
        final int planimetricUom = getInteger(6);
        final int verticalUom = getInteger(6);
        final int cornerCount = getInteger(6);
        if (cornerCount != 4) {
          throw new IllegalArgumentException("Only for corners are supported");
        }
        for (int i = 0; i < 4; i++) {
          this.cornersX[i] = getDoubleSci(24);
          this.cornersY[i] = getDoubleSci(24);
        }
        double minX = Math.min(this.cornersX[0], this.cornersX[1]);
        double maxX = Math.max(this.cornersX[2], this.cornersX[3]);
        double minY = Math.min(this.cornersY[0], this.cornersY[3]);
        double maxY = Math.max(this.cornersY[1], this.cornersY[2]);

        final double minZ = getDoubleSci(24);
        final double maxZ = getDoubleSci(24);
        final double angle = getDoubleSci(24);
        if (angle != 0) {
          throw new IllegalArgumentException(
            "Angle=" + angle + " not currently supported for USGS DEM: " + this.resource);
        }
        final int verticalAccuracy = getInteger(6);
        final double resolutionX = getDoubleSci(12);
        final double resolutionY = getDoubleSci(12);
        if (resolutionX != resolutionY) {
          throw new IllegalArgumentException("resolutionX " + resolutionX + " != " + resolutionY
            + " resolutionY for USGS DEM: " + this.resource);
        }
        this.resolutionX = (int)resolutionX;
        this.resolutionY = (int)resolutionY;
        if (resolutionX != this.resolutionX) {
          throw new IllegalArgumentException("resolutionX " + resolutionX
            + " must currently be an integer for USGS DEM: " + this.resource);
        }
        this.resolutionZ = getDoubleSci(12);
        final int rasterRowCount = getInteger(6);
        this.gridWidth = getInteger(6);
        final Short largestContourInterval = getShort5();
        final Byte largestContourIntervalUnits = getByte1();
        final Short smallestContourInterval = getShort5();
        final Byte smallest = getByte1();
        final Integer sourceYear = getInteger(4);
        final Integer revisionYear = getInteger(4);
        final String inspectionFlag = getString(1);
        final String dataValidationFlag = getString(1);
        final Integer suspectAndVoidAreaFlag = getInteger(2);
        final Integer verticalDatum = getInteger(2);
        final Integer horizontalDatum = getInteger(2);
        final Integer dataEdition = getInteger(4);
        final Integer percentVoid = getInteger(4);
        final Integer edgeMatchWest = getInteger(2);
        final Integer edgeMatchNorth = getInteger(2);
        final Integer edgeMatchEast = getInteger(2);
        final Integer edgeMatchSouth = getInteger(2);
        final Double verticalDatumShift = getDoubleSci(7);

        LinearUnit linearUnit = null;
        if (planimetricUom == 1) {
          linearUnit = EpsgCoordinateSystems.getLinearUnit("foot");
        } else if (planimetricUom == 2) {
          linearUnit = EpsgCoordinateSystems.getLinearUnit("metre");
        }
        final int verticalCoordinateSystemId = getVerticalCoordinateSystemId(verticalDatum);
        final int geographicCoordinateSystemId = getGeographicCoordinateSystemId(horizontalDatum);

        final double scaleZ = 1.0 / this.resolutionZ;
        int horizontalCoordinateSystemId = 0;
        HorizontalCoordinateSystem horizontalCoordinateSystem = null;
        if (0 == planimetricReferenceSystem) {
          horizontalCoordinateSystemId = geographicCoordinateSystemId;
        } else if (1 == planimetricReferenceSystem) {
          // UTM Zones
          switch (horizontalDatum) {
            case 1: // NAD27
              horizontalCoordinateSystemId = 26700 + zone;
            break;
            case 2: // WGS 72
              horizontalCoordinateSystemId = 32200 + zone;
            break;
            case 3: // WGS 84
              horizontalCoordinateSystemId = 32600 + zone;
            break;
            case 4: // NAD 83
              horizontalCoordinateSystemId = 26900 + zone;
            break;

            default:
              throw new IllegalArgumentException("UTM horizontalDatum=" + horizontalDatum
                + " not currently supported for USGS DEM: " + this.resource);
          }

        } else if (2 == planimetricReferenceSystem) {
          throw new IllegalArgumentException(
            "planimetricReferenceSystem=" + planimetricReferenceSystem
              + " not currently supported for USGS DEM: " + this.resource);
        } else if (3 == planimetricReferenceSystem) {
          final Map<ParameterName, ParameterValue> parameters = new LinkedHashMap<>();
          parameters.put(ParameterNames.CENTRAL_MERIDIAN, fromDms(projectionParameters[4]));
          parameters.put(ParameterNames.STANDARD_PARALLEL_1, fromDms(projectionParameters[2]));
          parameters.put(ParameterNames.STANDARD_PARALLEL_2, fromDms(projectionParameters[3]));
          parameters.put(ParameterNames.LATITUDE_OF_ORIGIN, fromDms(projectionParameters[5]));
          parameters.put(ParameterNames.FALSE_EASTING,
            new ParameterValueNumber(projectionParameters[6]));
          parameters.put(ParameterNames.FALSE_NORTHING,
            new ParameterValueNumber(projectionParameters[7]));

          final CoordinateOperationMethod coordinateOperationMethod = new CoordinateOperationMethod(
            "Albers_Equal_Area");
          final GeographicCoordinateSystem geographicCoordinateSystem = EpsgCoordinateSystems
            .getCoordinateSystem(geographicCoordinateSystemId);
          final ProjectedCoordinateSystem projectedCoordinateSystem = new ProjectedCoordinateSystem(
            -1, "", geographicCoordinateSystem, coordinateOperationMethod, parameters, linearUnit);
          final ProjectedCoordinateSystem projectedCoordinateSystem2 = EpsgCoordinateSystems
            .getCoordinateSystem(projectedCoordinateSystem);
          if (projectedCoordinateSystem2 == projectedCoordinateSystem
            || projectedCoordinateSystem2 == null) {
            horizontalCoordinateSystem = projectedCoordinateSystem;
          } else {
            horizontalCoordinateSystemId = projectedCoordinateSystem2.getCoordinateSystemId();
          }
        } else {
          throw new IllegalArgumentException(
            "planimetricReferenceSystem=" + planimetricReferenceSystem
              + " not currently supported for USGS DEM: " + this.resource);
        }
        if (horizontalCoordinateSystemId > 0) {
          horizontalCoordinateSystem = EpsgCoordinateSystems
            .getCoordinateSystem(horizontalCoordinateSystemId);
        }
        if (horizontalCoordinateSystem == null) {
          throw new IllegalArgumentException("No coordinate system found: " + this.resource);
        } else {
          CoordinateSystem coordinateSystem;
          if (horizontalCoordinateSystemId > 0) {
            coordinateSystem = EpsgCoordinateSystems.getCompound(horizontalCoordinateSystemId,
              verticalCoordinateSystemId);
          } else {
            final VerticalCoordinateSystem verticalCoordinateSystem = EpsgCoordinateSystems
              .getCoordinateSystem(verticalCoordinateSystemId);
            coordinateSystem = new CompoundCoordinateSystem(horizontalCoordinateSystem,
              verticalCoordinateSystem);
          }
          this.geometryFactory = coordinateSystem.getGeometryFactoryFixed(3, 0.0, 0.0, scaleZ);
        }
        if (horizontalDatum == 3 || horizontalDatum == 4) {
        } else {
          minX = Math.floor(minX / resolutionX) * resolutionX + resolutionX / 2;
          maxX = Math.ceil(maxX / resolutionX) * resolutionX - resolutionX / 2;
          minY = Math.floor(minY / resolutionY) * resolutionY - resolutionY / 2;
          maxY = Math.ceil(maxY / resolutionY) * resolutionY + resolutionY / 2;
          this.gridHeight = (int)((maxY - minY) / resolutionY);
        }
        this.boundingBox = this.geometryFactory.newBoundingBox(3, minX, minY, minZ, maxX, maxY,
          maxZ);
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
    final GeometryFactory geometryFactory = GeometryFactory.floating3d(resource);
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
