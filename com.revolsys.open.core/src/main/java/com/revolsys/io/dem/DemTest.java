package com.revolsys.io.dem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Polygon;

public class DemTest {

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

  private static Polygon getPolygon(final byte[] buffer, final GeometryFactory geometryFactory) {
    final int polygonSides = getInteger(buffer, 541);
    final double[] bounds = new double[polygonSides * 2 + 2];
    int offset = 547;
    int i = 0;
    for (; i < bounds.length - 2; i++) {
      bounds[i] = getDouble(buffer, offset);
      offset += 24;
    }
    bounds[i++] = bounds[0];
    bounds[i++] = bounds[1];
    final Polygon polygon = geometryFactory.polygon(geometryFactory.linearRing(2, bounds));
    return polygon;
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

  public static void main(final String[] args) throws FileNotFoundException, IOException {
    final byte[] buffer = new byte[1024];
    try (
      InputStream in = new FileInputStream(new File(
        "/apps/gba/data/dem/bc_093a096_grid10m_utm10_2013.dem"))) {
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
        if (1 == planimetricReferenceSystem) {
          final int srid = 26900 + zone;
          geometryFactory = GeometryFactory.floating(srid, 2);
        }
        final String projectionParameters = getString(buffer, 169, 360);
        final int planimetricUom = getInteger(buffer, 529);
        final int verticalUom = getInteger(buffer, 535);

        final Polygon polygon = getPolygon(buffer, geometryFactory);
        System.out.println(polygon);

        final double min = getDouble(buffer, 739);
        final double max = getDouble(buffer, 763);
        final double angle = getDouble(buffer, 787);
        final int verticalAccuracy = getInteger(buffer, 811);
        final float resolutionX = getFloat(buffer, 817);
        final float resolutionY = getFloat(buffer, 829);
        final float resolutionZ = getFloat(buffer, 841);
        final int rows = getInteger(buffer, 853);
        final int cols = getInteger(buffer, 859);
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
        while (in.read(buffer, 0, 1024) != -1) {
          final int rowId = getInteger(buffer, 1);
          final int colId = getInteger(buffer, 7);

          final int rowCount = getInteger(buffer, 13);
          final int colCount = getInteger(buffer, 19);

          final double x1 = getDouble(buffer, 25);
          final double y1 = getDouble(buffer, 49);
          final double z1 = getDouble(buffer, 73);
          final double minZ = getDouble(buffer, 97);
          final double maxZ = getDouble(buffer, 121);
          final int block = 0;
          final double[] elevations = new double[rowCount * colCount];
          for (int i = 0; i < elevations.length; i++) {
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
            final double elevation = z1 + value * resolutionZ;
            elevations[i] = elevation;
          }

        }
      }

    }

  }
}
