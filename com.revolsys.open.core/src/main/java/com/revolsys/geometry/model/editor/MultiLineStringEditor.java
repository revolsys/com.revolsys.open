package com.revolsys.geometry.model.editor;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.MultiLineString;

public class MultiLineStringEditor extends AbstractGeometryEditor
  implements MultiLineString, LinealEditor {
  private static final long serialVersionUID = 1L;

  private final Lineal lineal;

  private final List<LineStringEditor> editors = new ArrayList<>();

  public MultiLineStringEditor(final AbstractGeometryEditor geometryEditor, final Lineal lineal) {
    super(geometryEditor, lineal);
    this.lineal = lineal;
  }

  public MultiLineStringEditor(final Lineal lineal) {
    this(null, lineal);
  }

  @Override
  public Lineal clone() {
    return (Lineal)super.clone();
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

  public LineStringEditor getEditor(final int partIndex) {
    if (0 <= partIndex && partIndex <= this.editors.size()) {
      return this.editors.get(partIndex);
    } else {
      return null;
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
    return (V)getEditor(partIndex);
  }

  @Override
  public int getGeometryCount() {
    return this.editors.size();
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
    final int partCount = this.editors.size();
    final LineString[] lines = new LineString[partCount];
    int partIndex = 0;
    for (final LineStringEditor editor : this.editors) {
      final LineString lineString = editor.newGeometry();
      lines[partIndex++] = lineString;
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    return this.lineal.newLineal(geometryFactory, lines);
  }

  @Override
  public Lineal newLineal(final GeometryFactory geometryFactory, final LineString... lines) {
    return this.lineal.newLineal(geometryFactory, lines);
  }

  @Override
  public int setAxisCount(final int axisCount) {
    for (final LineStringEditor editor : this.editors) {
      editor.setAxisCount(axisCount);
    }
    return super.setAxisCount(axisCount);
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
    final LineStringEditor editor = getEditor(partIndex);
    if (editor == null) {
      return Double.NaN;
    } else {
      return editor.setCoordinate(vertexIndex, axisIndex, coordinate);
    }
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
