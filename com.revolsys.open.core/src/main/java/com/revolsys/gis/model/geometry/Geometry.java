package com.revolsys.gis.model.geometry;

import java.util.List;

import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.geometry.impl.BoundingBox;
import com.revolsys.io.ObjectWithProperties;
import com.vividsolutions.jts.geom.IntersectionMatrix;

public interface Geometry extends Cloneable, ObjectWithProperties {
  Geometry buffer(double value);

  Object clone();

  <G extends Geometry> G cloneGeometry();

  boolean contains(Geometry geometry);

  boolean coveredBy(Geometry geometry);

  boolean covers(Geometry geometry);

  boolean crosses(Geometry geometry);

  boolean disjoint(Geometry geometry);

  double getArea();

  int getBoundaryDimension();

  BoundingBox getBoundingBox();

  List<CoordinatesList> getCoordinatesLists();

  int getDimension();

  <G extends Geometry> List<G> getGeometries();

  <G extends Geometry> G getGeometry(int i);

  int getGeometryCount();

  <F extends GeometryFactory> F getGeometryFactory();

  double getLength();

  byte getNumAxis();

  int getSrid();

  Geometry intersection(Geometry geometry);

  boolean isEmpty();

  boolean isValid();

  IntersectionMatrix relate(Geometry geometry);

  boolean touches(Geometry geometry);

  boolean within(Geometry geometry);
}
