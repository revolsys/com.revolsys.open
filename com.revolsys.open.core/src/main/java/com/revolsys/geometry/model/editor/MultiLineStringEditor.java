package com.revolsys.geometry.model.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.MultiLineString;
import com.revolsys.util.Exceptions;
import com.revolsys.util.number.Doubles;

public class MultiLineStringEditor implements MultiLineString, LinealEditor {
  private static final long serialVersionUID = 1L;

  private final Lineal lineal;

  private LineStringEditor[] newLineStrings;

  private GeometryFactory newGeometryFactory;

  public MultiLineStringEditor(final Lineal lineal) {
    this.lineal = lineal;
    this.newGeometryFactory = lineal.getGeometryFactory();
  }

  @Override
  public Lineal clone() {
    try {
      return (Lineal)super.clone();
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
    if (this.newLineStrings == null) {
      return this.lineal.getGeometries();
    } else {
      return (List<V>)new ArrayList<>(Arrays.asList(this.newLineStrings));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V getGeometry(final int partIndex) {
    if (this.newLineStrings == null) {
      return this.lineal.getGeometry(partIndex);
    } else {
      return (V)this.newLineStrings[partIndex];
    }
  }

  @Override
  public int getGeometryCount() {
    if (this.newLineStrings == null) {
      return this.lineal.getGeometryCount();
    } else {
      return this.newLineStrings.length;
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
    return this.lineal.isEmpty();
  }

  @Override
  public Lineal newGeometry() {
    if (this.newLineStrings == null) {
      return this.lineal;
    } else {
      final LineString[] lines = new LineString[this.newLineStrings.length];
      for (int i = 0; i < this.newLineStrings.length; i++) {
        final LineStringEditor editor = this.newLineStrings[i];
        lines[i] = editor.newGeometry();
      }
      return this.lineal.newLineal(this.newGeometryFactory, lines);
    }
  }

  @Override
  public Lineal newLineal(final GeometryFactory geometryFactory, final LineString... lines) {
    return this.lineal.newLineal(geometryFactory, lines);
  }

  @Override
  public int setAxisCount(final int axisCount) {
    final int oldAxisCount = getAxisCount();
    if (oldAxisCount != axisCount) {
      final int geometryCount = getGeometryCount();
      if (this.newLineStrings == null) {
        final LineStringEditor[] editors = new LineStringEditor[geometryCount];
        for (int ringIndex = 0; ringIndex < geometryCount; ringIndex++) {
          final LineString line = getGeometry(ringIndex);
          final LineStringEditor editor = line.newGeometryEditor(axisCount);
          editors[ringIndex] = editor;
        }
        this.newLineStrings = editors;
      } else {
        for (int ringIndex = 0; ringIndex < geometryCount; ringIndex++) {
          final LineStringEditor editor = this.newLineStrings[ringIndex];
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
      final int vertexIndex = vertexId[1];
      return setCoordinate(partIndex, vertexIndex, axisIndex, coordinate);
    } else {
      return Double.NaN;
    }
  }

  @Override
  public double setCoordinate(final int partIndex, final int vertexIndex, final int axisIndex,
    final double coordinate) {
    final int geometryCount = getGeometryCount();
    if (partIndex >= 0 && partIndex < geometryCount) {
      final int axisCount = getAxisCount();
      if (axisIndex >= 0 && axisIndex < axisCount) {
        final double oldValue = this.lineal.getCoordinate(partIndex, vertexIndex, axisIndex);
        if (!Doubles.equal(coordinate, oldValue)) {
          if (this.newLineStrings == null) {
            final LineStringEditor[] editors = new LineStringEditor[geometryCount];
            for (int i = 0; i < geometryCount; i++) {
              final LineString line = getGeometry(i);
              final LineStringEditor editor = line.newGeometryEditor(axisCount);
              editors[i] = editor;
            }
            this.newLineStrings = editors;
          }
          final LineStringEditor editor = this.newLineStrings[partIndex];
          editor.setCoordinate(vertexIndex, axisIndex, coordinate);
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
