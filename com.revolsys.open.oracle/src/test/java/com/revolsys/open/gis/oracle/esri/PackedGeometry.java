package com.revolsys.open.gis.oracle.esri;

import java.util.Collections;
import java.util.List;

import com.revolsys.data.equals.GeometryEqualsExact3d;
import com.revolsys.format.wkt.WktWriter;
import com.revolsys.gis.oracle.esri.ArcSdeConstants;
import com.revolsys.gis.oracle.esri.ArcSdeStGeometryFieldDefinition;
import com.revolsys.gis.oracle.esri.PackedCoordinateUtil;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;

public class PackedGeometry {
  public static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.fixed(3005, 1.0, 1.0);

  public static void checkGeometry(final Geometry geometry) {
    final GeometryFactory geometryFactory = geometry.getGeometryFactory();
    final Double xOffset = 0.0;
    final Double yOffset = 0.0;
    final Double xyScale = geometryFactory.getScaleXY();
    final Double zOffset = 0.0;
    final Double zScale = geometryFactory.getScaleZ();
    final boolean hasZ = zScale > 0;
    final boolean hasM = false;
    final Double mScale = null;
    final Double mOffset = null;

    final List<List<Geometry>> parts = ArcSdeStGeometryFieldDefinition.getParts(geometry, false);

    final int numPoints = PackedCoordinateUtil.getNumPoints(parts);

    final byte[] data = PackedCoordinateUtil.getPackedBytes(xOffset, yOffset, xyScale, hasZ,
      zOffset, zScale, hasM, mScale, mOffset, parts);

    final int geometryType = ArcSdeConstants.getStGeometryType(geometry);
    final Geometry geometry2 = PackedCoordinateUtil.getGeometry(data, geometryFactory, geometryType,
      numPoints, xOffset, yOffset, xyScale, zOffset, zScale, mOffset, mScale);
    System.out.println(WktWriter.toString(geometry));
    if (!new GeometryEqualsExact3d().equals(geometry, geometry2,
      Collections.<String> emptyList())) {
      System.err.println(WktWriter.toString(geometry2));
      throw new RuntimeException("Geometry not equal");
    }
  }

  public static void checkGeometry(final String wkt) {
    final Geometry geometry = GEOMETRY_FACTORY.geometry(wkt);
    checkGeometry(geometry);
  }

  public static void main(final String[] args) {
    checkGeometry("POINT Z(100 200 3)");
    checkGeometry("MULTIPOINT Z((100 200 3),(400 500 6))");
    checkGeometry("LINESTRING Z(100 200 3,110 220 13)");
    checkGeometry("MULTILINESTRING Z((100 200 3,110 220 13),(400 500 6,410 520 16))");
    checkGeometry("POLYGON Z((100 100 1,100 200 2,200 200 3,200 100 4,100 100 5))");
    checkGeometry(
      "POLYGON Z((100 100 1,100 200 2,200 200 3,200 100 4,100 100 5),(50 50 1,50 70 2,70 70 3,70 50 4,50 50 5))");
    checkGeometry(
      "MULTIPOLYGON Z(((100 100 1,100 200 2,200 200 3,200 100 4,100 100 5),(50 50 1,50 70 2,70 70 3,70 50 4,50 50 5)),((300 300 1,300 400 2,400 400 3,400 300 4,300 300 5)))");
    // checkGeometry("MULTILINESTRING Z((100 200 3,110 220 13),(400 500 6,410
    // 520 16))");
  }
}
