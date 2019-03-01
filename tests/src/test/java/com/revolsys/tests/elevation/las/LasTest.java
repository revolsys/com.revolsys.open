package com.revolsys.tests.elevation.las;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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
import com.revolsys.util.Property;

public class LasTest {
  private static final List<String> PROPERTY_NAMES_HEADER = Arrays.asList("boundingBox", "date",
    "dayOfYear", "fileSourceId", "globalEncoding", "pointCount", "pointFormat", "pointFormatId",
    "projectId", "recordLength", "systemIdentifier", "version", "year");

  private static final List<String> PROPERTY_NAMES_POINT = Arrays.asList("pointFormatId", "x", "y",
    "z", "intensity", "returnNumber", "numberOfReturns", "scanDirectionFlag", "edgeOfFlightLine",
    "classification", "synthetic", "keyPoint", "withheld", "scanAngleDegrees", "userData",
    "pointSourceID", "gpsTime", "red", "green", "blue", "scannerChannel");

  private static final GeometryFactory GEOMETRY_FACTORY = GeometryFactoryWithOffsets
    .newWithOffsets(EpsgId.nad83Utm(10), 1000, 1000, 1000, 1000, 1000, 1000);

  private static final Path DIR = Paths.get("target/test/elevation/las");

  @BeforeClass
  public static void init() {
    com.revolsys.io.file.Paths.deleteDirectories(DIR);
    com.revolsys.io.file.Paths.createDirectories(DIR);
  }

  private void assertPointCloudEqual(final LasPointCloud cloud1, final LasPointCloud cloud2) {
    final LasPointCloudHeader header1 = cloud1.getHeader();
    final LasPointCloudHeader header2 = cloud2.getHeader();
    assertPointCloudHeaderEqual(header1, header2);

    final List<LasPoint> points1 = cloud1.getPoints();
    final List<LasPoint> points2 = cloud2.getPoints();
    final Iterator<LasPoint> iterator2 = points2.iterator();
    for (final LasPoint point1 : points1) {
      final LasPoint point2 = iterator2.next();
      assertPointEqual(point1, point2);
    }
  }

  private void assertPointCloudHeaderEqual(final LasPointCloudHeader header1,
    final LasPointCloudHeader header2) {
    for (final String propertyName : PROPERTY_NAMES_HEADER) {
      assertPropertyEqual(header1, header2, propertyName);
    }
  }

  private void assertPointEqual(final LasPoint point1, final LasPoint point2) {
    for (final String propertyName : PROPERTY_NAMES_POINT) {
      assertPropertyEqual(point1, point2, propertyName);
    }
  }

  private void assertPropertyEqual(final Object object1, final Object object2,
    final String propertyName) {
    final Object value1 = Property.getProperty(object1, propertyName);
    final Object value2 = Property.getProperty(object2, propertyName);
    Assert.assertEquals(propertyName, value1, value2);

  }

  public void asssertWriteRead(final String prefix, final LasPointFormat recordFormat,
    final Consumer<LasPointCloud> cloudAction, final String fileExtension) {
    final MapEx writeProperties = new LinkedHashMapEx();
    final Path file = DIR.resolve(prefix + "_" + recordFormat.name() + "." + fileExtension);
    try (
      LasPointCloud cloud = new LasPointCloud(recordFormat, GEOMETRY_FACTORY)) {
      cloudAction.accept(cloud);
      cloud.writePointCloud(file, writeProperties);
      try (
        LasPointCloud cloud2 = PointCloud.newPointCloud(file)) {
        assertPointCloudEqual(cloud, cloud2);
      }
    }
  }

  @Test
  public void readWrite() {
    final Consumer<LasPointCloud> cloudAction = cloud -> {
      cloud.addPoint(1000, 1000, 1000);
    };
    for (final LasPointFormat recordFormat : LasPointFormat.values()) {
      asssertWriteRead("readWrite", recordFormat, cloudAction, "las");
    }
  }

  @Test
  public void readWriteLaz() {
    final Consumer<LasPointCloud> cloudAction = cloud -> {
      cloud.addPoint(1000, 1000, 1000);
    };
    asssertWriteRead("readWrite", LasPointFormat.Core, cloudAction, "laz");
  }

}
