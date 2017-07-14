package com.revolsys.geometry.model.editor;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.MultiPoint;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Punctual;

public class MultiPointEditor extends AbstractGeometryEditor implements MultiPoint, PunctualEditor {
  private static final long serialVersionUID = 1L;

  private final Punctual punctual;

  private final List<PointEditor> editors = new ArrayList<>();

  public MultiPointEditor(final AbstractGeometryEditor parentEditor, final Punctual punctual) {
    super(parentEditor, punctual);
    this.punctual = punctual;
    for (final Point point : punctual.points()) {
      final PointEditor editor = new PointEditor(this, point);
      this.editors.add(editor);
    }
  }

  public MultiPointEditor(final Punctual punctual) {
    this(null, punctual);
  }

  @Override
  public Punctual clone() {
    return (Punctual)super.clone();
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

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> List<V> getGeometries() {
    return (List<V>)Lists.toArray(this.editors);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V getGeometry(final int partIndex) {
    return (V)getPointEditor(partIndex);
  }

  @Override
  public int getGeometryCount() {
    return this.editors.size();
  }

  public PointEditor getPointEditor(final int partIndex) {
    if (0 <= partIndex && partIndex < this.editors.size()) {
      return this.editors.get(partIndex);
    } else {
      return null;
    }
  }

  @Override
  public int hashCode() {
    return getBoundingBox().hashCode();
  }

  @Override
  public boolean isEmpty() {
    return this.punctual.isEmpty();
  }

  @Override
  public Punctual newGeometry() {
    final int pointCount = this.editors.size();
    final Point[] points = new Point[pointCount];
    int pointIndex = 0;
    for (final PointEditor editor : this.editors) {
      points[pointIndex++] = editor.newGeometry();
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    return this.punctual.newPunctual(geometryFactory, points);
  }

  @Override
  public Punctual newPunctual(final GeometryFactory geometryFactory, final Point... points) {
    return this.punctual.newPunctual(geometryFactory, points);
  }

  @Override
  public int setAxisCount(final int axisCount) {
    for (final PointEditor editor : this.editors) {
      editor.setAxisCount(axisCount);
    }
    return super.setAxisCount(axisCount);
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
    final PointEditor editor = getPointEditor(partIndex);
    if (editor == null) {
      return Double.NaN;
    } else {
      return editor.setCoordinate(axisIndex, coordinate);
    }
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
