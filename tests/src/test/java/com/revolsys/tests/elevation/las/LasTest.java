package com.revolsys.tests.elevation.las;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.jeometry.common.logging.Logs;
import org.jeometry.coordinatesystem.model.systems.EpsgId;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.elevation.cloud.las.LasPointCloudHeader;
import com.revolsys.elevation.cloud.las.LasPointCloudWriter;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.pointformat.LasPointFormat;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryWithOffsets;
import com.revolsys.util.Debug;
import com.revolsys.util.Property;
import com.revolsys.util.ServiceInitializer;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LasTest {

  private static final Random RANDOM = new Random(0);

  private static final double SCALE = 1000;

  private static final double OFFSET_Y = 5500000;

  private static final double OFFSET_X = 500000;

  private static final List<String> PROPERTY_NAMES_HEADER = Arrays.asList("boundingBox", "date",
    "dayOfYear", "fileSourceId", "globalEncoding", "pointCount", "pointFormat", "pointFormatId",
    "projectId", "recordLength", "systemIdentifier", "version", "year");

  private static final GeometryFactory GEOMETRY_FACTORY_OFFSET = GeometryFactoryWithOffsets
    .newWithOffsets(EpsgId.nad83Utm(10), //
      OFFSET_X, 100.0, //
      OFFSET_Y, 100.0, //
      0, 1000.0 //
    );

  private static final GeometryFactory GEOMETRY_FACTORY_0 = GeometryFactory
    .fixed3d(EpsgId.nad83Utm(10), SCALE, SCALE, SCALE);

  private static final Path DIR = Paths.get("target/test/elevation");

  private static final List<LasPointFormat> LAZ_TEST_FORMATS = Arrays.asList(//
    LasPointFormat.Core //
    , LasPointFormat.Rgb //
    , LasPointFormat.GpsTime //
    , LasPointFormat.GpsTimeRgb //
    , LasPointFormat.ExtendedGpsTime //
    , LasPointFormat.ExtendedGpsTimeRgb //
    , LasPointFormat.ExtendedGpsTimeRgbNir //
  );

  private static final List<String> FILE_EXTENSIONS = Arrays.asList("las", "laz");

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
      if (value > minValue) {
        final LasPoint point = addRandomPoint(cloud);
        action.accept(point, value);
      }
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
    final LasPoint point = cloud.addPoint(x, y, z);
    return point;
  }

  public static void addReturnPoints10(final LasPointCloud cloud) {
    for (byte returnCount = 1; returnCount <= 5; returnCount++) {
      for (byte returnIndex = 1; returnIndex <= returnCount; returnIndex++) {
        addScanDirAndEdgeFlightLinePoints(cloud, returnCount, returnIndex);
      }
    }
    for (int i = 0; i < 1000; i++) {
      byte returnCount = (byte)RANDOM.nextInt(5);
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

  public static void addReturnPoints14(final LasPointCloud cloud) {
    for (byte returnCount = 1; returnCount <= 15; returnCount++) {
      for (byte returnIndex = 1; returnIndex <= returnCount; returnIndex++) {
        addRandomPoint(cloud)//
          .setNumberOfReturns(returnCount) //
          .setReturnNumber(returnIndex) //
        ;
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
        final LasPoint point = addRandomPoint(cloud)//
          .setNumberOfReturns(returnCount) //
          .setReturnNumber(returnIndex) //
          .setEdgeOfFlightLine(edgeOfFlightLine) //
          .setScanDirectionFlag(scanDirectionFlag);
        ;
        if (point.getReturnNumber() != returnIndex) {
          Debug.noOp();
        }
        final byte numberOfReturns = point.getNumberOfReturns();
        if (numberOfReturns != returnCount) {
          Debug.noOp();
        }
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
    com.revolsys.io.file.Paths.createDirectories(DIR);
    ServiceInitializer.initializeServices();
    Debug.noOp();
  }

  private static double randomDouble() {
    return RANDOM.nextDouble();
  }

  private void addGpsTimePoints(final LasPointCloud cloud) {
    final int maxMilliSecondsInWeek = 604800 * 1000;
    long time = maxMilliSecondsInWeek / 2;
    for (int i = 0; i < 1000; i++) {
      time = (time + RANDOM.nextInt(999999)) % maxMilliSecondsInWeek;
      final double gpsTime = time / 100000.0;
      addRandomPoint(cloud) //
        .setGpsTime(gpsTime);
      ;
    }
  }

  private void addNirPoints(final LasPointCloud cloud) {
    for (final int nir : Arrays.asList(0, 1, 65534, 65535)) {
      addRandomPoint(cloud) //
        .setNir(nir) //
      ;
    }
    for (int i = 0; i < 1000; i++) {
      final int nir = RANDOM.nextInt(65535);
      addRandomPoint(cloud) //
        .setNir(nir) //
      ;
    }
  }

  private void addRandomPoints(final LasPointCloud cloud, final int count) {
    for (int i = 0; i < count; i++) {
      addRandomPoint(cloud);
    }
  }

  private void addRgbPoint(final LasPointCloud cloud, final int red, final int green,
    final int blue) {
    addRandomPoint(cloud) //
      .setRed(red) //
      .setGreen(green) //
      .setBlue(blue) //
    ;
  }

  private void addRgbPoints(final LasPointCloud cloud) {
    for (final int c : Arrays.asList(0, 1, 65534, 65535)) {
      addRgbPoint(cloud, c, c, c);

    }
    for (int i = 0; i < 1000; i++) {
      final int red = RANDOM.nextInt(65535);
      final int green = RANDOM.nextInt(65535);
      final int blue = RANDOM.nextInt(65535);
      addRgbPoint(cloud, red, green, blue);
    }
  }

  private void assertPointCloudEqual(final String label, final LasPointCloud cloud1,
    final LasPointCloud cloud2) {
    final LasPointCloudHeader header1 = cloud1.getHeader();
    final LasPointCloudHeader header2 = cloud2.getHeader();
    assertPointCloudHeaderEqual(header1, header2);

    final List<LasPoint> points1 = cloud1.getPoints();
    assertPointsEqual(label, points1, cloud2);
  }

  private void assertPointCloudHeaderEqual(final LasPointCloudHeader header1,
    final LasPointCloudHeader header2) {
    for (final String propertyName : PROPERTY_NAMES_HEADER) {
      assertPropertyEqual(header1, header2, propertyName);
    }
  }

  private void assertPointEqual(final LasPoint point1, final LasPoint point2) {
    Assert.assertEquals("pointFormat", point1.getPointFormat(), point2.getPointFormat());
    Assert.assertEquals("xInt", point1.getXInt(), point2.getXInt());
    Assert.assertEquals("yInt", point1.getYInt(), point2.getYInt());
    Assert.assertEquals("zInt", point1.getZInt(), point2.getZInt());
    Assert.assertEquals("intensity", point1.getIntensity(), point2.getIntensity());
    Assert.assertEquals("returnNumber", point1.getReturnNumber(), point2.getReturnNumber());
    Assert.assertEquals("numberOfReturns", point1.getNumberOfReturns(),
      point2.getNumberOfReturns());
    Assert.assertEquals("scanDirectionFlag", point1.isScanDirectionFlag(),
      point2.isScanDirectionFlag());
    Assert.assertEquals("edgeOfFlightLine", point1.isEdgeOfFlightLine(),
      point2.isEdgeOfFlightLine());
    Assert.assertEquals("classification", point1.getClassification(), point2.getClassification());
    Assert.assertEquals("synthetic", point1.isSynthetic(), point2.isSynthetic());
    Assert.assertEquals("keyPoint", point1.isKeyPoint(), point2.isKeyPoint());
    Assert.assertEquals("withheld", point1.isWithheld(), point2.isWithheld());
    Assert.assertEquals("scanAngleDegrees", point1.getScanAngleDegrees(),
      point2.getScanAngleDegrees(), 0);
    Assert.assertEquals("userData", point1.getUserData(), point2.getUserData());
    Assert.assertEquals("pointSourceID", point1.getPointSourceID(), point2.getPointSourceID());
    Assert.assertEquals("gpsTime", point1.getGpsTime(), point2.getGpsTime(), 0);
    Assert.assertEquals("red", point1.getRed(), point2.getRed());
    Assert.assertEquals("green", point1.getGreen(), point2.getGreen());
    Assert.assertEquals("blue", point1.getBlue(), point2.getBlue());
    Assert.assertEquals("scannerChannel", point1.getScannerChannel(), point2.getScannerChannel());

  }

  public void assertPointsEqual(final String label, final List<LasPoint> points1,
    final LasPointCloud cloud2) {
    final Iterator<LasPoint> iterator1 = points1.iterator();
    final AtomicInteger index = new AtomicInteger();
    cloud2.forEachPoint(point2 -> {
      final LasPoint point1 = iterator1.next();
      final int i = index.getAndIncrement();

      try {
        assertPointEqual(point1, point2);
      } catch (final AssertionError e) {
        Logs.error(this, label + "-" + i, e);
        throw e;
      }
    });
  }

  private void assertPropertyEqual(final Object object1, final Object object2,
    final String propertyName) {
    final Object value1 = Property.getProperty(object1, propertyName);
    final Object value2 = Property.getProperty(object2, propertyName);
    Assert.assertEquals(propertyName, value1, value2);

  }

  private void assertRead(final String label, final Path file, final LasPointCloud cloud) {
    try (
      LasPointCloud cloud2 = PointCloud.newPointCloud(file)) {
      assertPointCloudEqual(label, cloud, cloud2);
    }
  }

  private void assertRead(final String label, final Path file, final List<LasPoint> points) {
    try (
      LasPointCloud cloud2 = PointCloud.newPointCloud(file)) {
      assertPointsEqual(label, points, cloud2);
    }
  }

  public void assertWriter(final String prefix,
    final BiFunction<LasPointCloudWriter, Integer, List<LasPoint>> writerAction) {
    for (final int pointCount : Arrays.asList(1, 2, 3, 49000, 100001)) {
      for (final String fileFormat : FILE_EXTENSIONS) {
        for (final LasPointFormat recordFormat : LAZ_TEST_FORMATS) {
          final String prefix2 = prefix + "-" + Integer.toString(pointCount);
          final Map<String, MapEx> writeVariations = getWriteVariations(recordFormat, fileFormat);
          for (final Entry<String, MapEx> entry : writeVariations.entrySet()) {
            final String suffix = entry.getKey();
            final MapEx writeProperties = entry.getValue();
            final String label = prefix2 + "_" + recordFormat.name() + "_" + suffix;
            final String filePath = fileFormat + "/" + prefix2 + "/" + label + "." + fileFormat;
            final Path file = DIR.resolve(filePath);
            com.revolsys.io.file.Paths.createParentDirectories(file);
            System.out.println(filePath + "\t" + pointCount);
            final List<LasPoint> points;
            try (
              LasPointCloudWriter writer = LasPointCloud.newWriter(recordFormat,
                GEOMETRY_FACTORY_OFFSET, file, writeProperties)) {
              points = writerAction.apply(writer, pointCount);
            }
            assertRead(label, file, points);
            System.out.println();
          }
        }
      }
    }
  }

  public void asssertWriteRead(final String prefix, final LasPointFormat pointFormat,
    final GeometryFactory geometryFactory, final Consumer<LasPointCloud> cloudAction,
    final String fileExtension) {
    final Map<String, MapEx> writeVariations = getWriteVariations(pointFormat, fileExtension);
    try (
      LasPointCloud cloud = new LasPointCloud(pointFormat, geometryFactory)) {
      cloudAction.accept(cloud);
      for (final Entry<String, MapEx> entry : writeVariations.entrySet()) {
        final String suffix = entry.getKey();
        final MapEx writeProperties = entry.getValue();
        final String label = prefix + "_" + pointFormat.name() + "_" + suffix;
        final String filePath = fileExtension + "/" + prefix + "/" + label + "." + fileExtension;
        final Path file = DIR.resolve(filePath);
        com.revolsys.io.file.Paths.createParentDirectories(file);
        System.out.println(filePath + "\t" + cloud.getPointCount());
        cloud.writePointCloud(file, writeProperties);
        System.out.println();
        assertRead(label, file, cloud);
        System.out.println();
      }
    }
  }

  public void asssertWriteRead(final String prefix, final LasPointFormat recordFormat,
    final GeometryFactory geometryFactory, final String fileExtension) {
    final Consumer<LasPointCloud> cloudAction = cloud -> {
      if (recordFormat.name().contains("Nir")) {
        addNirPoints(cloud);
      }
      if (recordFormat.name().contains("GpsTime")) {
        addGpsTimePoints(cloud);
      }
      if (recordFormat.name().contains("Rgb")) {
        addRgbPoints(cloud);
      }
      addRandomPoints(cloud, 1000);
      addIntensityPoints(cloud);
      if (recordFormat.getId() < 6) {
        addReturnPoints10(cloud);
      } else {
        addReturnPoints14(cloud);
      }
      addClassificationAndFlags(cloud);
      if (recordFormat.getId() < 6) {
        addScanAngleRankPoints(cloud);
      }
      addUserDataPoints(cloud);
      addPointSourceIDPoints(cloud);
    };
    asssertWriteRead(prefix, recordFormat, geometryFactory, cloudAction, fileExtension);
  }

  public Map<String, MapEx> getWriteVariations(final LasPointFormat pointFormat,
    final String fileExtension) {
    final Map<String, MapEx> writeVariations = new LinkedHashMap<>();
    if ("laz".equals(fileExtension)) {
      switch (pointFormat) {
        case Core:
        case Rgb:
        case GpsTime:
        case GpsTimeRgb:
          writeVariations.put("v1", JsonObject.hash("lasZipVersion", 1));
          writeVariations.put("v2", JsonObject.hash("lasZipVersion", 2));
        break;

        case ExtendedGpsTime:
        case ExtendedGpsTimeRgb:
        case ExtendedGpsTimeRgbNir:
          writeVariations.put("v3", JsonObject.hash("lasZipVersion", 3));
          writeVariations.put("v4", JsonObject.hash("lasZipVersion", 4));
        break;
        case GpsTimeWavePackets:
        case GpsTimeRgbWavePackets:
        case ExtendedGpsTimeWavePackets:
        case ExtendedGpsTimeRgbNirWavePackets:
          // TODO
          throw new UnsupportedOperationException();
        default:
      }
    } else {
      writeVariations.put("default", JsonObject.hash());
    }
    return writeVariations;
  }

  private Consumer<LasPointCloud> newPointsAction(final int pointCount) {
    return cloud -> {
      if (pointCount > 0) {
        cloud.addPoint(OFFSET_X + 0.01, OFFSET_Y + 0.01, 0.001);
        if (pointCount > 1) {
          cloud.addPoint(OFFSET_X - 999.99, OFFSET_Y + 999.99, 999.999);
          if (pointCount > 2) {
            if (pointCount > 3) {
              final double range = 1900;
              double x = OFFSET_X - 999.99;
              double y = OFFSET_Y - 999.99;
              double z = -999.999;
              final double step = range / (pointCount - 3);
              for (int i = 0; i < pointCount - 3; i++) {
                cloud.addPoint(x, y, z);
                x += step;
                y += step;
                z += step;
              }
            }
            cloud.addPoint(OFFSET_X + 999.99, OFFSET_Y + 999.99, 999.999);
          }

        }
      }
    };
  }

  @Test
  public void testManyLas() {
    for (final LasPointFormat recordFormat : LasPointFormat.values()) {
      asssertWriteRead("many", recordFormat, GEOMETRY_FACTORY_0, "las");
    }
  }

  @Test
  public void testManyLaz() {
    for (final LasPointFormat recordFormat : LAZ_TEST_FORMATS) {
      asssertWriteRead("many", recordFormat, GEOMETRY_FACTORY_0, "laz");
    }
  }

  @Test
  public void testNPoints() {
    for (final int pointCount : Arrays.asList(1, 2, 3, 49000, 100001)) {
      final Consumer<LasPointCloud> cloudAction = newPointsAction(pointCount);
      for (final String fileFormat : FILE_EXTENSIONS) {
        for (final LasPointFormat recordFormat : LAZ_TEST_FORMATS) {
          final String prefix = Integer.toString(pointCount);
          asssertWriteRead(prefix, recordFormat, GEOMETRY_FACTORY_OFFSET, cloudAction, fileFormat);
        }
      }
    }
  }

  @Test
  public void testWriter() {
    final BiFunction<LasPointCloudWriter, Integer, List<LasPoint>> writerAction = (writer,
      pointCount) -> {
      final List<LasPoint> points = new ArrayList<>();
      for (int i = 0; i < pointCount; i++) {
        final double size = 10000;
        final double x = Math.round(randomDouble() * size * SCALE) / SCALE;
        final double y = Math.round(randomDouble() * size * SCALE) / SCALE;
        final double z = Math.round(randomDouble() * 3500 * SCALE) / SCALE;
        points.add(writer.writeNewLasPoint(x, y, z, point -> {
        }));
      }
      return points;
    };
    assertWriter("writer", writerAction);
  }

  @Test
  public void testWriterSamePoint() {
    final BiFunction<LasPointCloudWriter, Integer, List<LasPoint>> writerAction = (writer,
      pointCount) -> {
      final int maxUnsignedShort = 65535;

      final LasPointFormat pointFormat = writer.getPointFormat();
      final String formatName = pointFormat.name();

      final double gpsTime;
      if (formatName.contains("Gps")) {
        gpsTime = 60 * 60 * 24 * 7 + 0.123456;
      } else {
        gpsTime = 0;
      }
      final byte scannerChannel;
      if (pointFormat.getId() > 5) {
        scannerChannel = 3;
      } else {
        scannerChannel = 0;
      }
      final boolean rgb = formatName.contains("Rgb");
      final boolean nir = formatName.contains("Nir");
      final List<LasPoint> points = new ArrayList<>();
      for (int i = 0; i < pointCount; i++) {
        final double x = OFFSET_X + 12345.67;
        final double y = OFFSET_Y + 76543.21;
        final double z = 3456.321;
        points.add(writer.writeNewLasPoint(x, y, z, point -> {
          point//
            .setIntensity(maxUnsignedShort) //
            .setReturnNumber((byte)5)//
            .setNumberOfReturns((byte)5) //
            .setScanDirectionFlag(true) //
            .setEdgeOfFlightLine(true) //
            .setClassification((short)1) //
            .setScanAngleRank((byte)-90) //
            .setUserData(Byte.MIN_VALUE) //
            .setPointSourceID(maxUnsignedShort) //
          ;
          if (gpsTime != 0) {
            point.setGpsTime(gpsTime);
          }
          if (scannerChannel != 0) {
            point.setScannerChannel(scannerChannel);
          }
          if (rgb) {
            point.setRed(maxUnsignedShort);
            point.setGreen(maxUnsignedShort);
            point.setBlue(maxUnsignedShort);
          }
          if (nir) {
            point.setNir(maxUnsignedShort);
          }
        }));
      }
      return points;
    };
    assertWriter("writer", writerAction);
  }

  // @Test
  // public void testZ() {
  // final String fileFormat = "laz";
  // final LasPointFormat recordFormat = LasPointFormat.ExtendedGpsTime;
  // final int pointCount = 2;
  // final Consumer<LasPointCloud> cloudAction = newPointsAction(pointCount);
  // final String prefix = Integer.toString(pointCount);
  // asssertWriteRead(prefix, recordFormat, GEOMETRY_FACTORY_OFFSET,
  // cloudAction, fileFormat);
  // }
}
