package com.revolsys.elevation.gridded.usgsdem;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.DoubleArrayGriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelReadFactory;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.LinearUnit;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.Projection;
import com.revolsys.geometry.cs.ProjectionParameterNames;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.WrappedException;

public class UsgsGriddedElevation extends AbstractIoFactoryWithCoordinateSystem
  implements GriddedElevationModelReadFactory {

  private static Byte getByte(final byte[] buffer, final int offset) {
    final String string = getString(buffer, offset, 1);
    if (string.length() == 0) {
      return null;
    } else {
      return Byte.valueOf(string);
    }
  }

  private static Double getDouble(final byte[] buffer, final int offset) {
    return getDouble(buffer, offset, 24);
  }

  private static Double getDouble(final byte[] buffer, final int offset, final int length) {
    final String string = getString(buffer, offset, length);
    if (string.length() == 0) {
      return null;
    } else {
      return Double.parseDouble(string);
    }
  }

  private static Double getDoubleSci(final byte[] buffer, final int offset) {
    String string = getString(buffer, offset, 24);
    if (string.length() == 0) {
      return null;
    } else {
      string = string.replace("D", "E");
      return Double.parseDouble(string);
    }
  }

  private static Float getFloat(final byte[] buffer, final int offset) {
    final String string = getString(buffer, offset, 12);
    if (string.length() == 0) {
      return null;
    } else {
      return Float.parseFloat(string);
    }
  }

  private static int getInteger(final byte[] buffer, final int offset) {
    return getInteger(buffer, offset, 6);
  }

  private static Integer getInteger(final byte[] buffer, final int offset, final int length) {
    final String string = getString(buffer, offset, length);
    if (string.length() == 0) {
      return null;
    } else {
      return Integer.valueOf(string);
    }
  }

  private static double[] getPolygonCoordinates(final byte[] buffer, int offset) {
    final int polygonSides = getInteger(buffer, offset);
    final double[] bounds = new double[polygonSides * 2 + 2];
    offset = 547;
    int i = 0;
    for (; i < bounds.length - 2; i++) {
      bounds[i] = getDouble(buffer, offset);
      offset += 24;
    }
    bounds[i++] = bounds[0];
    bounds[i++] = bounds[1];
    return bounds;
  }

  private static Short getShort(final byte[] buffer, final int offset) {
    final String string = getString(buffer, offset, 5);
    if (string.length() == 0) {
      return null;
    } else {
      return Short.valueOf(string);
    }
  }

  private static String getString(final byte[] buffer, final int offset, final int length) {
    return new String(buffer, offset - 1, length, StandardCharsets.US_ASCII).trim();
  }

  public UsgsGriddedElevation() {
    super("USGS DEM");
    addMediaTypeAndFileExtension("image/x-dem", "dem");
  }

  private double fromDms(final double value) {
    final double degrees = Math.floor(value / 10000);
    final double minutes = Math.floor(Math.abs(value) % 10000 / 100);
    final double seconds = Math.abs(value) % 100;
    final double decimal = degrees + minutes / 60 + seconds / 3600;
    return decimal;
  }

  @Override
  public GriddedElevationModel newGriddedElevationModel(final Resource resource,
    final Map<String, ? extends Object> properties) {
    final byte[] buffer = new byte[1024];
    try (
      InputStream in = resource.getInputStream()) {
      if (in.read(buffer, 0, 1024) != -1) {
        final String fileName = getString(buffer, 1, 40);
        final String descriptor = getString(buffer, 41, 40);
        // Blank 81 - 109
        final String seGeographic = getString(buffer, 110, 26);
        final String processCode = getString(buffer, 136, 1);
        // Blank 137
        final String sectionIndicator = getString(buffer, 138, 3);
        final String originCode = getString(buffer, 141, 4);
        final int demLevelCode = getInteger(buffer, 145);
        final int elevationPattern = getInteger(buffer, 151);
        final int planimetricReferenceSystem = getInteger(buffer, 157);
        final int zone = getInteger(buffer, 163);
        GeometryFactory geometryFactory = GeometryFactory.wgs84();
        final double[] projectionParameters = new double[15];
        for (int i = 0; i < projectionParameters.length; i++) {
          projectionParameters[i] = getDoubleSci(buffer, 169 + i * 24);
        }
        final int planimetricUom = getInteger(buffer, 529);
        final int verticalUom = getInteger(buffer, 535);
        final double[] polygonBounds = getPolygonCoordinates(buffer, 541);

        final double min = getDouble(buffer, 739);
        final double max = getDouble(buffer, 763);
        final double angle = getDouble(buffer, 787);
        final int verticalAccuracy = getInteger(buffer, 811);
        final float resolutionX = getFloat(buffer, 817);
        final float resolutionY = getFloat(buffer, 829);
        final float resolutionZ = getFloat(buffer, 841);
        int rasterRowCount = getInteger(buffer, 853);
        final int rasterColCount = getInteger(buffer, 859);
        final Short largestContourInterval = getShort(buffer, 865);
        final Byte largestContourIntervalUnits = getByte(buffer, 870);
        final Short smallestContourInterval = getShort(buffer, 871);
        final Byte smallest = getByte(buffer, 876);
        final Integer sourceYear = getInteger(buffer, 877, 4);
        final Integer revisionYear = getInteger(buffer, 881, 4);
        final String inspectionFlag = getString(buffer, 885, 1);
        final String dataValidationFlag = getString(buffer, 886, 1);
        final String suspectAndVoidAreaFlag = getString(buffer, 887, 1);
        final Integer verticalDatum = getInteger(buffer, 889, 2);
        final Integer horizontalDatum = getInteger(buffer, 891, 2);
        final Integer dataEdition = getInteger(buffer, 893, 4);
        final Integer percentVoid = getInteger(buffer, 897, 4);
        final Integer edgeMatchWest = getInteger(buffer, 901, 2);
        final Integer edgeMatchNorth = getInteger(buffer, 903, 2);
        final Integer edgeMatchEast = getInteger(buffer, 905, 2);
        final Integer edgeMatchSouth = getInteger(buffer, 907, 2);
        final Double verticalDatumShift = getDouble(buffer, 909, 7);

        LinearUnit linearUnit = null;
        if (planimetricUom == 1) {
          linearUnit = EpsgCoordinateSystems.getLinearUnit("foot");
        } else if (planimetricUom == 2) {
          linearUnit = EpsgCoordinateSystems.getLinearUnit("metre");
        }
        GeographicCoordinateSystem geographicCoordinateSystem = null;
        switch (horizontalDatum) {
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
          break;
        }
        if (1 == planimetricReferenceSystem) {
          final int coordinateSystemId = 26900 + zone;
          geometryFactory = GeometryFactory.floating(coordinateSystemId, 2);
        } else if (2 == planimetricReferenceSystem) {

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
            geometryFactory = GeometryFactory.floating(projectedCoordinateSystem2, 2);
          } else {
            final int coordinateSystemId = projectedCoordinateSystem2.getCoordinateSystemId();
            geometryFactory = GeometryFactory.floating(coordinateSystemId, 2);
          }
        }

        final float[][] raster = new float[rasterColCount][];
        while (in.read(buffer, 0, 1024) != -1) {
          final int rowIndex = getInteger(buffer, 1) - 1;
          int columnIndex = getInteger(buffer, 7) - 1;

          final int rowCount = getInteger(buffer, 13);
          final int colCount = getInteger(buffer, 19);
          float[] yElevations = raster[columnIndex];
          if (yElevations == null) {
            yElevations = new float[rowIndex + rowCount];
            raster[columnIndex] = yElevations;
            Arrays.fill(yElevations, 0, yElevations.length, Float.NaN);
          }

          final double x1 = getDouble(buffer, 25);
          final double y1 = getDouble(buffer, 49);
          final double z1 = getDouble(buffer, 73);
          final double minZ = getDouble(buffer, 97);
          final double maxZ = getDouble(buffer, 121);

          rasterRowCount = Math.max(rasterRowCount, rowIndex + rowCount);
          for (int i = 0; i < rowCount; i++) {
            final int value;
            if (i < 146) {
              value = getInteger(buffer, 145 + i * 6);
            } else {
              final int offset = (i - 146) % 170;
              if (offset == 0) {
                in.read(buffer, 0, 1024);
              }
              value = getInteger(buffer, 1 + offset * 6);
            }
            final float elevation = (float)(z1 + value * resolutionZ);
            if (elevation > -32767) {
              yElevations[rowIndex + i] = elevation;
            }
          }
          columnIndex++;
        }

        final Polygon polygon = geometryFactory
          .polygon(geometryFactory.linearRing(2, polygonBounds));
        final BoundingBox boundingBox = polygon.getBoundingBox();

        final DoubleArrayGriddedElevationModel elevationModel = new DoubleArrayGriddedElevationModel(
          geometryFactory, boundingBox.getMinX(), boundingBox.getMinY(), rasterColCount,
          rasterRowCount, (int)resolutionX);
        elevationModel.setResource(resource);
        elevationModel.clear();

        for (int cellX = 0; cellX < raster.length; cellX++) {
          final float[] yElevations = raster[cellX];
          for (int cellY = 0; cellY < yElevations.length; cellY++) {
            final float elevation = yElevations[cellY];
            if (!Float.isNaN(elevation)) {
              elevationModel.setElevation(cellX, cellY, elevation);
            }
          }
        }
        return elevationModel;
      }
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
    return null;
  }

}
