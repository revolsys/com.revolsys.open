package com.revolsys.gis.util;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordState;
import com.revolsys.gis.graph.Edge;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;

public class Debug {
  public static boolean equals(final Geometry geometry, final double x,
    final double y) {
    final Point firstPoint = geometry.getPoint();
    if (firstPoint.equals(x, y)) {
      noOp();
      return true;
    } else {
      return false;
    }
  }

  public static boolean equals(final LineString line, final double x1,
    final double y1, final double x2, final double y2) {
    final LineString points = line;
    if (points.getPoint(0).equals(x1, y1)
      && points.getPoint(points.getVertexCount() - 1).equals(x2, y2)) {
      noOp();
      return true;
    } else {
      return false;
    }
  }

  public static void equals(final Object object1, final Object object2) {
    if (object1.equals(object2)) {
      noOp();
    }
  }

  public static boolean equals(final Point coordinates1End,
    final double... coordinates) {
    if (coordinates1End.equals(coordinates)) {
      noOp();
      return true;
    } else {
      return false;
    }
  }

  public static void equals(final Record object, final double x, final double y) {
    equals(object.getGeometryValue(), x, y);
  }

  public static void equals(final Record object, final Double x, final Double y) {
    equals(object.getGeometryValue(), x, y);
  }

  public static void idNull(final Record object) {
    if (object.getIdentifier() == null) {
      noOp();
    }
  }

  public static void infinite(final double value) {
    if (Double.isInfinite(value)) {
      noOp();
    }
  }

  public static void invalidGeometry(final Geometry geometry) {
    if (!geometry.isValid()) {
      noOp();
    }
  }

  public static void isNull(final Object value) {
    if (value == null) {
      noOp();
    }
  }

  public static void modified(final Record object) {
    if (object.getState() == RecordState.Modified) {
      noOp();
    }
  }

  public static void nan(final double value) {
    if (Double.isNaN(value)) {
      noOp();
    }
  }

  public static void noOp() {
  }

  public static void println(final Object object) {
    System.out.println(object);
  }

  public static void typePath(final Edge<?> edge, final String typePath) {
    final String typePath2 = edge.getTypeName();
    equals(typePath2, typePath);
  }

  public static void typePath(final Record object, final String typePath) {
    final String typePath2 = object.getRecordDefinition().getPath();
    equals(typePath2, typePath);
  }

  public static void zeroLegthLine(final LineString line) {
    if (line.getLength() == 0) {
      noOp();
    }
  }

}
