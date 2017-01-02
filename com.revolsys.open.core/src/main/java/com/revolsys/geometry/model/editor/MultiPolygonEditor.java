package com.revolsys.geometry.model.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.MultiPolygon;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.util.Exceptions;
import com.revolsys.util.number.Doubles;

public class MultiPolygonEditor implements MultiPolygon, PolygonalEditor {
  private static final long serialVersionUID = 1L;

  private final Polygonal polygonal;

  private PolygonEditor[] newPolygons;

  private GeometryFactory newGeometryFactory;

  public MultiPolygonEditor(final Polygonal polygonal) {
    this.polygonal = polygonal;
    this.newGeometryFactory = polygonal.getGeometryFactory();
  }

  @Override
  public Polygonal clone() {
    try {
      return (Polygonal)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof Geometry) {
      final Geometry geometry = (Geometry)other;
      return equals(2, geometry);
    } else {
      return false;
    }
  }

  @Override
  public int getAxisCount() {
    return this.newGeometryFactory.getAxisCount();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> List<V> getGeometries() {
    if (this.newPolygons == null) {
      return this.polygonal.getGeometries();
    } else {
      return (List<V>)new ArrayList<>(Arrays.asList(this.newPolygons));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V getGeometry(final int partIndex) {
    if (this.newPolygons == null) {
      return this.polygonal.getGeometry(partIndex);
    } else {
      return (V)this.newPolygons[partIndex];
    }
  }

  @Override
  public int getGeometryCount() {
    if (this.newPolygons == null) {
      return this.polygonal.getGeometryCount();
    } else {
      return this.newPolygons.length;
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.newGeometryFactory;
  }

  @Override
  public int hashCode() {
    return getBoundingBox().hashCode();
  }

  @Override
  public boolean isEmpty() {
    return this.polygonal.isEmpty();
  }

  @Override
  public Polygonal newGeometry() {
    if (this.newPolygons == null) {
      return this.polygonal;
    } else {
      final Polygon[] polygons = new Polygon[this.newPolygons.length];
      for (int i = 0; i < this.newPolygons.length; i++) {
        final PolygonEditor editor = this.newPolygons[i];
        polygons[i] = editor.newGeometry();
      }
      return this.polygonal.newPolygonal(this.newGeometryFactory, polygons);
    }
  }

  @Override
  public Polygonal newPolygonal(final GeometryFactory geometryFactory, final Polygon... polygons) {
    return this.polygonal.newPolygonal(geometryFactory, polygons);
  }

  @Override
  public int setAxisCount(final int axisCount) {
    final int oldAxisCount = getAxisCount();
    if (oldAxisCount != axisCount) {
      final int geometryCount = getGeometryCount();
      if (this.newPolygons == null) {
        final PolygonEditor[] editors = new PolygonEditor[geometryCount];
        for (int ringIndex = 0; ringIndex < geometryCount; ringIndex++) {
          final Polygon polygon = getGeometry(ringIndex);
          final PolygonEditor editor = polygon.newGeometryEditor(axisCount);
          editors[ringIndex] = editor;
        }
        this.newPolygons = editors;
      } else {
        for (int ringIndex = 0; ringIndex < geometryCount; ringIndex++) {
          final PolygonEditor editor = this.newPolygons[ringIndex];
          editor.setAxisCount(axisCount);
        }

      }
      this.newGeometryFactory = this.newGeometryFactory.convertAxisCount(axisCount);
    }
    return oldAxisCount;
  }

  @Override
  public double setCoordinate(final int axisIndex, final double coordinate, final int... vertexId) {
    if (vertexId.length == 2) {
      final int partIndex = vertexId[0];
      final int ringIndex = vertexId[1];
      final int vertexIndex = vertexId[2];
      return setCoordinate(partIndex, ringIndex, vertexIndex, axisIndex, coordinate);
    } else {
      return Double.NaN;
    }
  }

  @Override
  public double setCoordinate(final int partIndex, final int ringIndex, final int vertexIndex,
    final int axisIndex, final double coordinate) {
    final int geometryCount = getGeometryCount();
    if (partIndex >= 0 && partIndex < geometryCount) {
      final int axisCount = getAxisCount();
      if (axisIndex >= 0 && axisIndex < axisCount) {
        final double oldValue = this.polygonal.getCoordinate(partIndex, ringIndex, vertexIndex,
          axisIndex);
        if (!Doubles.equal(coordinate, oldValue)) {
          if (this.newPolygons == null) {
            final PolygonEditor[] editors = new PolygonEditor[geometryCount];
            for (int i = 0; i < geometryCount; i++) {
              final Polygon polygon = getGeometry(i);
              final PolygonEditor editor = polygon.newGeometryEditor(axisCount);
              editors[i] = editor;
            }
            this.newPolygons = editors;
          }
          final PolygonEditor editor = this.newPolygons[partIndex];
          editor.setCoordinate(ringIndex, vertexIndex, axisIndex, coordinate);
        }
      }
    }
    return Double.NaN;
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
