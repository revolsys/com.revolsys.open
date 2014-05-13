package com.revolsys.gis.util;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;

public class NoOp {
  public static boolean equals(final Point coordinates1End,
    final double... coordinates) {
    if (coordinates1End.equals(coordinates)) {
      noOp();
      return true;
    } else {
      return false;
    }
  }

  public static void equals(final DataObject object, final double x,
    final double y) {
    equals(object.getGeometryValue(), x, y);
  }

  public static void equals(final DataObject object, final Double x,
    final Double y) {
    equals(object.getGeometryValue(), x, y);
  }

  public static boolean equals(final Geometry geometry, final double x,
    final double y) {
    final CoordinatesList points = CoordinatesListUtil.get(geometry);
    final DoubleCoordinates point = new DoubleCoordinates(x, y);
    if (points.equal(0, point, 2)) {
      noOp();
      return true;
    } else {
      return false;
    }
  }

  public static boolean equals(final LineString line, final double x1,
    final double y1, final double x2, final double y2) {
    final CoordinatesList points = CoordinatesListUtil.get(line);
    if (points.get(0).equals(x1, y1)
      && points.get(points.size() - 1).equals(x2, y2)) {
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

  public static void idNull(final DataObject object) {
    if (object.getIdValue() == null) {
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

  public static void modified(final DataObject object) {
    if (object.getState() == DataObjectState.Modified) {
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

  public static void typePath(final DataObject object, final String typePath) {
    final String typePath2 = object.getMetaData().getPath();
    equals(typePath2, typePath);
  }

  public static void typePath(final Edge<?> edge, final String typePath) {
    final String typePath2 = edge.getTypeName();
    equals(typePath2, typePath);
  }

  public static void zeroLegthLine(final LineString line) {
    if (line.getLength() == 0) {
      noOp();
    }
  }

}
