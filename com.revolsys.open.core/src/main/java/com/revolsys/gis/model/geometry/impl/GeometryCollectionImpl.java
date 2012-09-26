package com.revolsys.gis.model.geometry.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.GeometryCollection;
import com.vividsolutions.jts.geom.Dimension;

public class GeometryCollectionImpl extends GeometryImpl implements
  GeometryCollection {
  private final List<? extends Geometry> geometries;

  protected GeometryCollectionImpl(final GeometryFactoryImpl geometryFactory,
    final List<? extends Geometry> geometries) {
    super(geometryFactory);
    this.geometries = Collections.unmodifiableList(geometries);
  }

  protected GeometryCollectionImpl(final GeometryFactoryImpl geometryFactory,
    final Class<?> geometryClass,
    final Collection<? extends Geometry> geometries) {
    super(geometryFactory);
    List<Geometry> geometryList = new ArrayList<Geometry>();
    for (Geometry geometry : geometries) {
      Class<? extends Geometry> clazz = geometry.getClass();
      if (!geometryClass.isAssignableFrom(clazz)) {
        throw new IllegalArgumentException("Expecting instance of class "
          + geometryClass + " not" + clazz);
      }
      geometryList.add(geometry);
    }
    this.geometries = Collections.unmodifiableList(geometryList);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <G extends Geometry> G getGeometry(int i) {
    return (G)geometries.get(i);
  }

  @Override
  public double getArea() {
    double area = 0;
    for (Geometry geometry : getGeometries()) {
      area += geometry.getArea();
    }
    return area;
  }

  public double getLength() {
    double length = 0;
    for (Geometry geometry : getGeometries()) {
      length += geometry.getLength();
    }
    return length;
  }

  @Override
  public boolean isEmpty() {
    return getGeometries().isEmpty();
  }

  public int getBoundaryDimension() {
    int dimension = Dimension.FALSE;
    for (Geometry geometry : getGeometries()) {
      dimension = Math.max(dimension, geometry.getBoundaryDimension());
    }
    return dimension;
  }

  @SuppressWarnings("unchecked")
  public <G extends Geometry> List<G> getGeometries() {
    return (List<G>)geometries;
  }

  @Override
  public GeometryCollectionImpl clone() {
    return (GeometryCollectionImpl)super.clone();
  }

  @Override
  public List<CoordinatesList> getCoordinatesLists() {
    List<CoordinatesList> pointsList = new ArrayList<CoordinatesList>();
    for (Geometry geometry : getGeometries()) {
      List<CoordinatesList> lists = geometry.getCoordinatesLists();
      pointsList.addAll(lists);
    }
    return pointsList;
  }

  public int getDimension() {
    int dimension = -1;
    for (Geometry geometry : getGeometries()) {
      dimension = Math.max(dimension, geometry.getDimension());
    }
    return dimension;
  }
}
