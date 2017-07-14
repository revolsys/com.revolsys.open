package com.revolsys.geometry.model.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Polygon;

public class PolygonEditor extends AbstractGeometryEditor implements Polygon, PolygonalEditor {
  private static final long serialVersionUID = 1L;

  private final Polygon polygon;

  private final List<LinearRingEditor> editors = new ArrayList<>();

  public PolygonEditor(final AbstractGeometryEditor parentEditor, final Polygon polygon) {
    super(parentEditor, polygon);
    this.polygon = polygon;
    for (final LinearRing ring : polygon.rings()) {
      final LinearRingEditor editor = new LinearRingEditor(this, ring);
      this.editors.add(editor);
    }
  }

  public PolygonEditor(final Polygon polygon) {
    this(null, polygon);
  }

  public LinearRingEditor addRing() {
    final LinearRingEditor editor = new LinearRingEditor(this);
    this.editors.add(editor);
    return editor;
  }

  public LinearRingEditor addRing(final int index) {
    final LinearRingEditor editor = new LinearRingEditor(this);
    this.editors.add(index, editor);
    return editor;
  }

  public LinearRingEditor addRing(final int index, final LinearRing ring) {
    final LinearRingEditor editor = new LinearRingEditor(this, ring);
    this.editors.add(index, editor);
    return editor;
  }

  public LinearRingEditor addRing(final LinearRing ring) {
    final LinearRingEditor editor = new LinearRingEditor(this, ring);
    this.editors.add(editor);
    return editor;
  }

  @Override
  public Polygon clone() {
    return (Polygon)super.clone();
  }

  public LinearRingEditor getEditor(final int ringIndex) {
    if (ringIndex < 0 || ringIndex >= this.editors.size()) {
      return null;
    } else {
      return this.editors.get(ringIndex);
    }
  }

  @Override
  public LinearRing getRing(final int ringIndex) {
    return getEditor(ringIndex);
  }

  @Override
  public int getRingCount() {
    return this.editors.size();
  }

  @Override
  public List<LinearRing> getRings() {
    return Lists.toArray(this.editors);
  }

  @Override
  public int hashCode() {
    return getBoundingBox().hashCode();
  }

  @Override
  public boolean isEmpty() {
    return this.polygon.isEmpty();
  }

  @Override
  public Polygon newGeometry() {
    final int ringCount = this.editors.size();
    final LinearRing[] rings = new LinearRing[ringCount];
    int ringIndex = 0;
    for (final LinearRingEditor editor : this.editors) {
      rings[ringIndex++] = editor.newGeometry();
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    return this.polygon.newPolygon(geometryFactory, rings);
  }

  @Override
  public Polygon newPolygon(final GeometryFactory geometryFactory, final LinearRing... rings) {
    return this.polygon.newPolygon(geometryFactory, rings);
  }

  @Override
  public Iterable<PolygonEditor> polygonEditors() {
    return Collections.singleton(this);
  }

  public void removeRing(final int index) {
    this.editors.remove(index);
  }

  public Iterable<LinearRingEditor> ringEditors() {
    return Collections.unmodifiableList(this.editors);
  }

  @Override
  public int setAxisCount(final int axisCount) {
    for (final LinearRingEditor editor : this.editors) {
      editor.setAxisCount(axisCount);
    }
    return super.setAxisCount(axisCount);
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
    final LinearRingEditor editor = getEditor(ringIndex);
    if (editor == null) {
      return Double.NaN;
    } else {
      return editor.setCoordinate(vertexIndex, axisIndex, coordinate);
    }
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
