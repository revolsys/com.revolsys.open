package com.revolsys.gis.model.geometry.base;

import java.util.AbstractList;

import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.GeometryCollection;

public class BaseGeometryCollection<T extends Geometry> extends AbstractList<T>
  implements GeometryCollection<T> {
  private T[] geometries;

  public double distance(
    Geometry geometry) {
    if (geometry instanceof GeometryCollection<?>) {
      GeometryCollection<?> geometryCollection = (GeometryCollection<?>)geometry;
      return distance(geometryCollection);
    } else {
      return geometry.distance(this);
    }
  }

  public double distance(
    GeometryCollection<?> geometryCollection) {
    double minDistance = Double.MAX_VALUE;
    for (Geometry geometry : geometryCollection) {
      double distance = geometry.distance(this);
      if (distance < minDistance) {
        minDistance = distance;
      }
    }
    return minDistance;
  }


  @Override
  public T get(
    int index) {
    if (index < 0 || index >= size()) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
        + size());
    } else {
      return geometries[index];
    }
  }

  @Override
  public int size() {
    return geometries.length;
  }

}
