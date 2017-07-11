package com.revolsys.geometry.model.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.MultiPoint;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.util.Exceptions;
import com.revolsys.util.number.Doubles;

public class MultiPointEditor implements MultiPoint, GeometryEditor, PunctualEditor {
  private static final long serialVersionUID = 1L;

  private final Punctual punctual;

  private PointEditor[] newPoints;

  private GeometryFactory newGeometryFactory;

  public MultiPointEditor(final Punctual punctual) {
    this.punctual = punctual;
    this.newGeometryFactory = punctual.getGeometryFactory();
  }

  @Override
  public Punctual clone() {
    try {
      return (Punctual)super.clone();
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
    if (this.newPoints == null) {
      return this.punctual.getGeometries();
    } else {
      return (List<V>)new ArrayList<>(Arrays.asList(this.newPoints));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V getGeometry(final int partIndex) {
    if (this.newPoints == null) {
      return this.punctual.getGeometry(partIndex);
    } else {
      return (V)this.newPoints[partIndex];
    }
  }

  @Override
  public int getGeometryCount() {
    if (this.newPoints == null) {
      return this.punctual.getGeometryCount();
    } else {
      return this.newPoints.length;
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
    return this.punctual.isEmpty();
  }

  /*
   * (non-Javadoc)
   * @see com.revolsys.geometry.model.editor.PunctualEditor#newGeometry()
   */
  @Override
  public Punctual newGeometry() {
    if (this.newPoints == null) {
      return this.punctual;
    } else {
      final Point[] points = new Point[this.newPoints.length];
      for (int i = 0; i < this.newPoints.length; i++) {
        final PointEditor editor = this.newPoints[i];
        points[i] = editor.newGeometry();
      }
      return this.punctual.newPunctual(this.newGeometryFactory, points);
    }
  }

  @Override
  public Punctual newPunctual(final GeometryFactory geometryFactory, final Point... points) {
    return this.punctual.newPunctual(geometryFactory, points);
  }

  /*
   * (non-Javadoc)
   * @see com.revolsys.geometry.model.editor.PunctualEditor#setAxisCount(int)
   */
  @Override
  public int setAxisCount(final int axisCount) {
    final int oldAxisCount = getAxisCount();
    if (oldAxisCount != axisCount) {
      final int geometryCount = getGeometryCount();
      if (this.newPoints == null) {
        final PointEditor[] editors = new PointEditor[geometryCount];
        for (int partIndex = 0; partIndex < geometryCount; partIndex++) {
          final Point point = getGeometry(partIndex);
          final PointEditor editor = point.newGeometryEditor(axisCount);
          editors[partIndex] = editor;
        }
        this.newPoints = editors;
      } else {
        for (int partIndex = 0; partIndex < geometryCount; partIndex++) {
          final PointEditor editor = this.newPoints[partIndex];
          editor.setAxisCount(axisCount);
        }

      }
      this.newGeometryFactory = this.newGeometryFactory.convertAxisCount(axisCount);
    }
    return oldAxisCount;
  }

  @Override
  public double setCoordinate(final int axisIndex, final double coordinate, final int... vertexId) {
    if (vertexId.length == 1) {
      final int partIndex = vertexId[0];
      return setCoordinate(partIndex, axisIndex, coordinate);
    } else {
      return Double.NaN;
    }
  }

  @Override
  public double setCoordinate(final int partIndex, final int axisIndex, final double coordinate) {
    final int geometryCount = getGeometryCount();
    if (partIndex >= 0 && partIndex < geometryCount) {
      final int axisCount = getAxisCount();
      if (axisIndex >= 0 && axisIndex < axisCount) {
        final double oldValue = this.punctual.getCoordinate(partIndex, axisIndex);
        if (!Doubles.equal(coordinate, oldValue)) {
          if (this.newPoints == null) {
            final PointEditor[] editors = new PointEditor[geometryCount];
            for (int i = 0; i < geometryCount; i++) {
              final Point point = getGeometry(i);
              final PointEditor editor = point.newGeometryEditor(axisCount);
              editors[i] = editor;
            }
            this.newPoints = editors;
          }
          final PointEditor editor = this.newPoints[partIndex];
          editor.setCoordinate(axisIndex, coordinate);
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
