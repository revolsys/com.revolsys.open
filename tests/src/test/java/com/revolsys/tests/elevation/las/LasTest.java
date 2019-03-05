package com.revolsys.tests.elevation.las;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.elevation.cloud.las.LasPointCloudHeader;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.pointformat.LasPointFormat;
import com.revolsys.geometry.cs.epsg.EpsgId;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryWithOffsets;
import com.revolsys.util.Debug;
import com.revolsys.util.Property;

public class LasTest {

  private static final Random RANDOM = new Random(0);

  private static final double SCALE = 1000;

  private static final double OFFSET_Y = 5500000;

  private static final double OFFSET_X = 500000;

  private static final List<String> PROPERTY_NAMES_HEADER = Arrays.asList("boundingBox", "date",
    "dayOfYear", "fileSourceId", "globalEncoding", "pointCount", "pointFormat", "pointFormatId",
    "projectId", "recordLength", "systemIdentifier", "version", "year");

  private static final List<String> PROPERTY_NAMES_POINT = Arrays.asList("pointFormatId", "x", "y",
    "z", "intensity", "returnNumber", "numberOfReturns", "scanDirectionFlag", "edgeOfFlightLine",
    "classification", "synthetic", "keyPoint", "withheld", "scanAngleDegrees", "userData",
    "pointSourceID", "gpsTime", "red", "green", "blue", "scannerChannel");

  private static final GeometryFactory GEOMETRY_FACTORY = GeometryFactoryWithOffsets.newWithOffsets(
    EpsgId.nad83Utm(10), //
    OFFSET_X, SCALE, //
    OFFSET_Y, SCALE, //
    0, SCALE //
  );

  private static final GeometryFactory GEOMETRY_FACTORY_0 = GeometryFactory
    .fixed3d(EpsgId.nad83Utm(10), SCALE, SCALE, SCALE);

  private static final Path DIR = Paths.get("target/test/elevation/las");

  public static void addClassificationAndFlags(final LasPointCloud cloud) {
    for (byte classification = 0; classification <= 31; classification++) {
      addClassificationSyntheticKeyPointWithheldFlagsPoints(cloud, classification);
    }
    for (int i = 0; i < 1000; i++) {
      final short classification = (short)RANDOM.nextInt(31);
      addClassificationSyntheticKeyPointWithheldFlagsPoints(cloud, classification);

    }
  }

  private static void addClassificationSyntheticKeyPointWithheldFlagsPoints(
    final LasPointCloud cloud, final short classification) {
    for (final boolean synthetic : Arrays.asList(false, true)) {
      for (final boolean keyPoint : Arrays.asList(false, true)) {
        for (final boolean withheld : Arrays.asList(false, true)) {
          addRandomPoint(cloud)//
            .setClassification(classification) //
            .setSynthetic(synthetic) //
            .setKeyPoint(keyPoint) //
            .setWithheld(withheld);
          ;
        }
      }
    }
  }

  public static void addIntensityPoints(final LasPointCloud cloud) {
    final BiConsumer<LasPoint, Integer> action = (point, intensity) -> {
      point.setIntensity(intensity);
    };
    addPointShortField(cloud, action, 0);
  }

  private static void addPointByteField(final LasPointCloud cloud,
    final BiConsumer<LasPoint, Short> action) {
    for (short value = 0; value <= 255; value++) {
      final LasPoint point = addRandomPoint(cloud);
      action.accept(point, value);
    }
  }

  private static void addPointShortField(final LasPointCloud cloud,
    final BiConsumer<LasPoint, Integer> action, final int minValue) {
    for (final int value : Arrays.asList(minValue, 1, 3, 254, 255, 256, 257, 255, 65533, 65533,
      65535)) {
      final LasPoint point = addRandomPoint(cloud);
      action.accept(point, value);
    }
    for (int i = 0; i < 500; i++) {
      final int value = RANDOM.nextInt(65535);
      final LasPoint point = addRandomPoint(cloud);
      action.accept(point, value);
    }
  }

  public static void addPointSourceIDPoints(final LasPointCloud cloud) {
    final BiConsumer<LasPoint, Integer> action = (point, pointSourceID) -> {
      point.setPointSourceID(pointSourceID);
    };
    addPointShortField(cloud, action, 1);
  }

  public static LasPoint addRandomPoint(final LasPointCloud cloud) {
    final double size = 10000;
    final double x = Math.round(randomDouble() * size * SCALE) / SCALE;
    final double y = Math.round(randomDouble() * size * SCALE) / SCALE;
    final double z = Math.round(randomDouble() * 3500 * SCALE) / SCALE;
    if (x < 0 || y < 0 || z < 0) {
      Debug.noOp();
    }
    final LasPoint point = cloud.addPoint(x, y, z);
    if (Math.abs(x - point.getX()) >= 0.001) {
      Debug.noOp();
    } else if (Math.abs(y - point.getY()) >= 0.001) {
      Debug.noOp();
    } else if (Math.abs(z - point.getZ()) >= 0.001) {
      Debug.noOp();
    }
    return point;
  }

  public static void addReturnPoints15(final LasPointCloud cloud) {
    for (byte returnCount = 1; returnCount <= 15; returnCount++) {
      for (byte returnIndex = 1; returnIndex <= returnCount; returnIndex++) {
        addScanDirAndEdgeFlightLinePoints(cloud, returnCount, returnIndex);
      }
    }
    for (int i = 0; i < 1000; i++) {
      byte returnCount = (byte)RANDOM.nextInt(15);
      if (returnCount == 0) {
        returnCount = 1;
      }
      byte returnIndex = (byte)RANDOM.nextInt(returnCount);
      if (returnIndex == 0) {
        returnIndex = 1;
      }
      addScanDirAndEdgeFlightLinePoints(cloud, returnCount, returnIndex);
    }
  }

  public static void addReturnPoints31(final LasPointCloud cloud) {
    for (byte returnCount = 1; returnCount <= 31; returnCount++) {
      for (byte returnIndex = 1; returnIndex <= returnCount; returnIndex++) {
        addRandomPoint(cloud)//
          .setNumberOfReturns(returnCount) //
          .setReturnNumber(returnIndex) //
        ;
      }
    }
    for (int i = 0; i < 1000; i++) {
      byte returnCount = (byte)RANDOM.nextInt(31);
      if (returnCount == 0) {
        returnCount = 1;
      }
      byte returnIndex = (byte)RANDOM.nextInt(returnCount);
      if (returnIndex == 0) {
        returnIndex = 1;
      }
      addRandomPoint(cloud)//
        .setNumberOfReturns(returnCount) //
        .setReturnNumber(returnIndex) //
      ;
    }
  }

  public static void addScanAngleRankPoints(final LasPointCloud cloud) {
    final BiConsumer<LasPoint, Short> action = (point, scanAngleRank) -> {
      point.setScanAngleRank(scanAngleRank.byteValue());
    };
    addPointByteField(cloud, action);
  }

  private static void addScanDirAndEdgeFlightLinePoints(final LasPointCloud cloud,
    final byte returnCount, final byte returnIndex) {
    for (final boolean edgeOfFlightLine : Arrays.asList(false, true)) {
      for (final boolean scanDirectionFlag : Arrays.asList(false, true)) {
        addRandomPoint(cloud)//
          .setNumberOfReturns(returnCount) //
          .setReturnNumber(returnIndex) //
          .setEdgeOfFlightLine(edgeOfFlightLine) //
          .setScanDirectionFlag(scanDirectionFlag);
        ;
      }
    }
  }

  public static void addUserDataPoints(final LasPointCloud cloud) {
    final BiConsumer<LasPoint, Short> action = (point, userData) -> {
      point.setUserData(userData);
    };
    addPointByteField(cloud, action);
  }

  @BeforeClass
  public static void init() {
    com.revolsys.io.file.Paths.deleteDirectories(DIR);
    com.revolsys.io.file.Paths.createDirectories(DIR);
  }

  private static double randomDouble() {
    return RANDOM.nextDouble();
  }

  private void addRandomPoints(final LasPointCloud cloud, final int count) {
    for (int i = 0; i < count; i++) {
      addRandomPoint(cloud);
    }
  }

  private void assertPointCloudEqual(final String label, final LasPointCloud cloud1,
    final LasPointCloud cloud2) {
    final LasPointCloudHeader header1 = cloud1.getHeader();
    final LasPointCloudHeader header2 = cloud2.getHeader();
    assertPointCloudHeaderEqual(header1, header2);

    final AtomicInteger index = new AtomicInteger();
    final List<LasPoint> points1 = cloud1.getPoints();
    final Iterator<LasPoint> iterator1 = points1.iterator();
    cloud2.forEachPoint(point2 -> {
      final LasPoint point1 = iterator1.next();
      final int i = index.getAndIncrement();

      assertPointEqual(label + "-" + i, point1, point2);
    });
  }

  private void assertPointCloudHeaderEqual(final LasPointCloudHeader header1,
    final LasPointCloudHeader header2) {
    for (final String propertyName : PROPERTY_NAMES_HEADER) {
      assertPropertyEqual(header1, header2, propertyName);
    }
  }

  private void assertPointEqual(final String label, final LasPoint point1, final LasPoint point2) {
    for (final String propertyName : PROPERTY_NAMES_POINT) {
      assertPropertyEqual(label, point1, point2, propertyName);
    }
  }

  private void assertPropertyEqual(final Object object1, final Object object2,
    final String propertyName) {
    final Object value1 = Property.getProperty(object1, propertyName);
    final Object value2 = Property.getProperty(object2, propertyName);
    Assert.assertEquals(propertyName, value1, value2);

  }

  private void assertPropertyEqual(final String label, final Object object1, final Object object2,
    final String propertyName) {
    final Object value1 = Property.getProperty(object1, propertyName);
    final Object value2 = Property.getProperty(object2, propertyName);
    Assert.assertEquals(label + " " + propertyName, value1, value2);

  }

  private void assertRead(final String label, final Path file, final LasPointCloud cloud) {
    try (
      LasPointCloud cloud2 = PointCloud.newPointCloud(file)) {
      assertPointCloudEqual(label, cloud, cloud2);
    }
  }

  public void asssertWriteRead(final String prefix, final LasPointFormat pointFormat,
    final Consumer<LasPointCloud> cloudAction, final String fileExtension) {
    final Map<String, MapEx> writeVariations = new LinkedHashMap<>();
    if ("laz".equals(fileExtension)) {
      switch (pointFormat) {
        case Core:
        case GpsTime:
        case Rgb:
        case GpsTimeRgb:
          for (int version = 1; version <= 2; version++) {
            writeVariations.put("v" + version, new LinkedHashMapEx("lasZipVersion", version));
          }
        break;
        case GpsTimeWavePackets:
          writeVariations.put("v1", new LinkedHashMapEx("lasZipVersion", 1));
        break;
        case ExtendedGpsTime:
          // TODO
          throw new UnsupportedOperationException();
        case ExtendedGpsTimeRgb:
          // TODO
          throw new UnsupportedOperationException();
        case ExtendedGpsTimeRgbNir:
          // TODO
          throw new UnsupportedOperationException();
        case ExtendedGpsTimeWavePackets:
          // TODO
          throw new UnsupportedOperationException();
        case ExtendedGpsTimeRgbNirWavePackets:
          // TODO
          throw new UnsupportedOperationException();
        default:
      }
    } else {
      writeVariations.put("default", new LinkedHashMapEx());
    }
    try (
      LasPointCloud cloud = new LasPointCloud(pointFormat, GEOMETRY_FACTORY_0)) {
      cloudAction.accept(cloud);
      for (final Entry<String, MapEx> entry : writeVariations.entrySet()) {
        final String suffix = entry.getKey();
        final MapEx writeProperties = entry.getValue();
        final String label = prefix + "_" + pointFormat.name() + "_" + suffix;
        final Path file = DIR.resolve(label + "." + fileExtension);
        cloud.writePointCloud(file, writeProperties);
        assertRead(label, file, cloud);
      }
    }
  }

  public void asssertWriteRead(final String prefix, final LasPointFormat recordFormat,
    final String fileExtension) {
    final Consumer<LasPointCloud> cloudAction = cloud -> {
      addRandomPoints(cloud, 1000);
      addIntensityPoints(cloud);
      if (recordFormat.getId() < 6) {
        addReturnPoints15(cloud);
      } else {
        addReturnPoints31(cloud);
      }
      addClassificationAndFlags(cloud);
      if (recordFormat.getId() < 6) {
        addScanAngleRankPoints(cloud);
      }
      addUserDataPoints(cloud);
      addPointSourceIDPoints(cloud);
    };
    asssertWriteRead(prefix, recordFormat, cloudAction, fileExtension);
  }

  @Test
  public void readWrite() {
    for (final LasPointFormat recordFormat : LasPointFormat.values()) {
      asssertWriteRead("readWriteLas", recordFormat, "las");
    }
  }

  @Test
  public void readWriteLaz1Point() {
    final Consumer<LasPointCloud> cloudAction = cloud -> {
      cloud.addPoint(OFFSET_X, OFFSET_Y, 0);
    };
    asssertWriteRead("readWriteLaz1Point", LasPointFormat.Core, cloudAction, "laz");
  }

  @Test
  public void readWriteLaz2Points() {
    final Consumer<LasPointCloud> cloudAction = cloud -> {
      cloud.addPoint(OFFSET_X, OFFSET_Y, 0);
      cloud.addPoint(OFFSET_X + 999, OFFSET_Y + 998, 996);
    };
    asssertWriteRead("readWriteLaz2Points", LasPointFormat.Core, cloudAction, "laz");
  }

  @Test
  public void readWriteLaz3Points() {
    final Consumer<LasPointCloud> cloudAction = cloud -> {
      cloud.addPoint(OFFSET_X, OFFSET_Y, 0);
      cloud.addPoint(OFFSET_X + 999, OFFSET_Y + 998, 996);
      cloud.addPoint(OFFSET_X - 999, OFFSET_Y - 998, -996);
    };
    asssertWriteRead("readWriteLaz3Points", LasPointFormat.Core, cloudAction, "laz");
  }

  @Test
  public void readWriteLazManyPoints() {
    asssertWriteRead("readWriteLazManyPoints", LasPointFormat.Core, "laz");
  }
}
