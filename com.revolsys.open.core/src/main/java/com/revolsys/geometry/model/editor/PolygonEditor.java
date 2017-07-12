package com.revolsys.geometry.model.editor;

import java.util.List;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.AbstractPolygon;
import com.revolsys.util.number.Doubles;

public class PolygonEditor extends AbstractPolygon implements PolygonalEditor {
  private static final long serialVersionUID = 1L;

  private final Polygon polygon;

  private LinearRingEditor[] newRings;

  private GeometryFactory newGeometryFactory;

  public PolygonEditor(final Polygon polygon) {
    this.polygon = polygon;
    this.newGeometryFactory = polygon.getGeometryFactory();
  }

  @Override
  public int getAxisCount() {
    return this.newGeometryFactory.getAxisCount();
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.newGeometryFactory;
  }

  @Override
  public LinearRing getRing(final int ringIndex) {
    if (this.newRings == null) {
      return this.polygon.getRing(ringIndex);
    } else if (ringIndex < 0 || ringIndex >= this.newRings.length) {
      return null;
    } else {
      return this.newRings[ringIndex];
    }
  }

  @Override
  public int getRingCount() {
    if (this.newRings == null) {
      return this.polygon.getRingCount();
    } else {
      return this.newRings.length;
    }
  }

  @Override
  public List<LinearRing> getRings() {
    if (this.newRings == null) {
      return this.polygon.getRings();
    } else {
      return Lists.newArray(this.newRings);
    }
  }

  @Override
  public boolean isEmpty() {
    return this.polygon.isEmpty();
  }

  @Override
  public Polygon newGeometry() {
    if (this.newRings == null) {
      return this.polygon;
    } else {
      final LinearRing[] rings = new LinearRing[this.newRings.length];
      for (int i = 0; i < this.newRings.length; i++) {
        final LinearRingEditor ringEditor = this.newRings[i];
        rings[i] = ringEditor.newGeometry();
      }
      return this.polygon.newPolygon(this.newGeometryFactory, rings);
    }
  }

  @Override
  public Polygon newPolygon(final GeometryFactory geometryFactory, final LinearRing... rings) {
    return this.polygon.newPolygon(geometryFactory, rings);
  }

  @Override
  public int setAxisCount(final int axisCount) {
    final int oldAxisCount = getAxisCount();
    if (oldAxisCount != axisCount) {
      final int ringCount = getRingCount();
      if (this.newRings == null) {
        final LinearRingEditor[] ringEditors = new LinearRingEditor[ringCount];
        for (int ringIndex = 0; ringIndex < ringCount; ringIndex++) {
          final LinearRing ring = getRing(ringIndex);
          final LinearRingEditor ringEditor = ring.newGeometryEditor(axisCount);
          ringEditors[ringIndex] = ringEditor;
        }
        this.newRings = ringEditors;
      } else {
        for (int ringIndex = 0; ringIndex < ringCount; ringIndex++) {
          final LinearRingEditor ringEditor = this.newRings[ringIndex];
          ringEditor.setAxisCount(axisCount);
        }

      }
      this.newGeometryFactory = this.newGeometryFactory.convertAxisCount(axisCount);
    }
    return oldAxisCount;
  }

  @Override
  public double setCoordinate(final int axisIndex, final double coordinate, final int... vertexId) {
    if (vertexId.length == 2) {
      final int ringIndex = vertexId[0];
      final int vertexIndex = vertexId[1];
      return setCoordinate(ringIndex, vertexIndex, axisIndex, coordinate);
    } else {
      return Double.NaN;
    }
  }

  public double setCoordinate(final int ringIndex, final int vertexIndex, final int axisIndex,
    final double coordinate) {
    final int ringCount = getRingCount();
    if (ringIndex >= 0 && ringIndex < ringCount) {
      final int axisCount = getAxisCount();
      if (axisIndex >= 0 && axisIndex < axisCount) {
        final double oldValue = this.polygon.getCoordinate(ringIndex, vertexIndex, axisIndex);
        if (!Doubles.equal(coordinate, oldValue)) {
          if (this.newRings == null) {
            final LinearRingEditor[] ringEditors = new LinearRingEditor[ringCount];
            for (int i = 0; i < ringCount; i++) {
              final LinearRing ring = getRing(i);
              final LinearRingEditor editor = ring.newGeometryEditor(axisCount);
              ringEditors[i] = editor;
            }
            this.newRings = ringEditors;
          }
          final LinearRingEditor ring = this.newRings[ringIndex];
          ring.setCoordinate(vertexIndex, axisIndex, coordinate);
        }
      }
    }
    return Double.NaN;
  }

  @Override
  public double setCoordinate(final int partIndex, final int ringIndex, final int vertexIndex,
    final int axisIndex, final double coordinate) {
    if (partIndex == 0) {
      return setCoordinate(ringIndex, vertexIndex, axisIndex, coordinate);
    } else {
      return Double.NaN;
    }
  }

  public double setM(final int ringIndex, final int vertexIndex, final double m) {
    return setCoordinate(ringIndex, vertexIndex, M, m);
  }

  public double setX(final int ringIndex, final int vertexIndex, final double x) {
    return setCoordinate(ringIndex, vertexIndex, X, x);
  }

  public double setY(final int ringIndex, final int vertexIndex, final double y) {
    return setCoordinate(ringIndex, vertexIndex, Y, y);
  }

  public double setZ(final int ringIndex, final int vertexIndex, final double z) {
    return setCoordinate(ringIndex, vertexIndex, Z, z);
  }
}
