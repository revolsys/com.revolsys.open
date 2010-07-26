package com.revolsys.gis.util;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class NoOp {

  public static void equals(
    final Coordinate point,
    final double x,
    final double y) {
    final Coordinate coordinate2 = new Coordinate(x, y);
    if (point.equals2D(coordinate2)) {
      noOp();
    }
  }

  public static void equals(
    final Coordinates coordinates1End,
    final double... coordinates) {
    if (coordinates1End.equals(coordinates)) {
      noOp();
    }
  }

  public static void equals(
    final DataObject object,
    final double x,
    final double y) {
    equals(object.getGeometryValue(), x, y);
  }

  public static void equals(
    final Geometry geometry,
    final double x,
    final double y) {
    final Coordinate coordinate1 = geometry.getCoordinate();
    equals(coordinate1, x, y);
  }

  public static void equals(
    final Node<?> node,
    final double... coordinates) {
    equals(node.getCoordinates(), coordinates);
  }

  public static void equals(
    final Object object1,
    final Object object2) {
    if (object1.equals(object2)) {
      noOp();
    }
  }

  public static void idNull(
    final DataObject object) {
    if (object.getIdValue() == null) {
      noOp();
    }
  }

  public static void infinite(
    final double value) {
    if (Double.isInfinite(value)) {
      noOp();
    }
  }

  public static void invalidGeometry(
    final Geometry geometry) {
    if (!geometry.isValid()) {
      noOp();
    }
  }

  public static void isNull(
    final Object value) {
    if (value == null) {
      noOp();
    }
  }

  public static void modified(
    final DataObject object) {
    if (object.getState() == DataObjectState.Modified) {
      noOp();
    }
  }

  public static void noOp() {
  }

  public static void typeName(
    final DataObject object,
    final QName typeName) {
    final QName typeName2 = object.getMetaData().getName();
    equals(typeName2, typeName);
  }

  public static void typeName(
    final Edge<?> edge,
    final QName typeName) {
    final QName typeName2 = edge.getTypeName();
    equals(typeName2, typeName);
  }

  public static void zeroLegthLine(
    final LineString line) {
    if (line.getLength() == 0) {
      noOp();
    }
  }

  public static void equals(
    LineString line,
    double x1,
    double y1,
    double x2,
    double y2) {
    final CoordinatesList points = CoordinatesListUtil.get(line);
    if (points.get(0).equals(x1,y1) && points.get(points.size()-1).equals(x2,y2)) {
      noOp();
    }
  }

  public static void nan(
    double value) {
    if (Double.isNaN(value)) {
      noOp();
    }
  }

}
