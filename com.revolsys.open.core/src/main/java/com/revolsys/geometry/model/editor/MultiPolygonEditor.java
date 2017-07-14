package com.revolsys.geometry.model.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.MultiPolygon;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;

public class MultiPolygonEditor extends AbstractGeometryEditor
  implements MultiPolygon, PolygonalEditor {
  private static final long serialVersionUID = 1L;

  private final Polygonal polygonal;

  private final List<PolygonEditor> editors = new ArrayList<>();

  public MultiPolygonEditor(final AbstractGeometryEditor parentEditor, final Polygonal polygonal) {
    super(parentEditor, polygonal);
    this.polygonal = polygonal;
    for (final Polygon polygon : polygonal.getPolygons()) {
      final PolygonEditor editor = new PolygonEditor(this, polygon);
      this.editors.add(editor);
    }
  }

  public MultiPolygonEditor(final Polygonal polygonal) {
    this(null, polygonal);
  }

  @Override
  public Polygonal clone() {
    return (Polygonal)super.clone();
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

  public PolygonEditor getEditor(final int partIndex) {
    if (0 <= partIndex && partIndex < this.editors.size()) {
      return null;
    } else {
      return this.editors.get(partIndex);
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
    return this.polygonal.isEmpty();
  }

  @Override
  public Polygonal newGeometry() {
    final int partCount = this.editors.size();
    final Polygon[] polygons = new Polygon[partCount];
    int partIndex = 0;
    for (final PolygonEditor editor : this.editors) {
      polygons[partIndex++] = editor.newGeometry();
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    return this.polygonal.newPolygonal(geometryFactory, polygons);
  }

  @Override
  public Polygonal newPolygonal(final GeometryFactory geometryFactory, final Polygon... polygons) {
    return this.polygonal.newPolygonal(geometryFactory, polygons);
  }

  @Override
  public Iterable<PolygonEditor> polygonEditors() {
    return Collections.unmodifiableList(this.editors);
  }

  @Override
  public int setAxisCount(final int axisCount) {
    for (final PolygonalEditor editor : this.editors) {
      editor.setAxisCount(axisCount);
    }
    return super.setAxisCount(axisCount);
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
    final PolygonEditor editor = getEditor(partIndex);
    if (editor == null) {
      return Double.NaN;
    } else {
      return editor.setCoordinate(ringIndex, vertexIndex, axisIndex, coordinate);
    }
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
