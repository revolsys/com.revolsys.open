package com.revolsys.gis.model.geometry;

import java.util.Collection;
import java.util.List;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;

public interface GeometryFactory extends CoordinatesPrecisionModel {
  /**
   * Get the geometry converted to this geometry factory. If the factory is this
   * factory then the geometry will be returned, otherwise a new geometry will
   * be created. If the coordinate system is different the coordinates will be
   * converted to the correct coordinate system.
   * 
   * @param geometry
   * @return
   */
  <G extends Geometry> G getGeometry(Geometry geometry);

  <G extends Geometry> G createGeometry(
    Collection<? extends Geometry> geometries);

  <G extends Geometry> G createGeometry(Geometry geometry);

  GeometryCollection createGeometryCollection();

  <G extends GeometryCollection> G createGeometryCollection(
    Collection<? extends Geometry> geometries);

  LinearRing createLinearRing(CoordinatesList points);

  LinearRing createLinearRing(LineString lineString);

  LinearRing createLinearRing(Object points);

  Geometry createLineString();

  LineString createLineString(CoordinatesList points);

  LineString createLineString(LineString lineString);

  LineString createLineString(Object points);

  MultiLineString createMultiLineString(List<?> lines);

  Point createPoint(Coordinates coordinates);

  Point createPoint(double... coordinates);

  Point createPoint(Point point);

  Polygon createPolygon(final CoordinatesList... rings);

  Polygon createPolygon(final List<?> rings);

  Polygon createPolygon(final Polygon polygon);

  CoordinateSystem getCoordinateSystem();

  int getSrid();
}
