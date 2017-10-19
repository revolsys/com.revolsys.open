package com.revolsys.geometry.model.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.util.function.BiConsumerDouble;

public class PolygonEditor extends AbstractGeometryEditor<PolygonEditor>
  implements Polygon, PolygonalEditor {
  private static final long serialVersionUID = 1L;

  private Polygon polygon;

  private final List<LinearRingEditor> editors = new ArrayList<>();

  public PolygonEditor(final AbstractGeometryCollectionEditor<?, ?, ?> parentEditor,
    final Polygon polygon) {
    super(parentEditor, polygon);
    this.polygon = polygon;
    for (final LinearRing ring : polygon.rings()) {
      final LinearRingEditor editor = new LinearRingEditor(this, ring);
      this.editors.add(editor);
    }
  }

  public PolygonEditor(final GeometryFactory geometryFactory) {
    super(geometryFactory);
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

  public PolygonalEditor appendVertex(final int ringIndex, final Point point) {
    final LinearRingEditor editor = getEditor(ringIndex);
    if (editor != null) {
      editor.appendVertex(point);
    }
    return this;
  }

  @Override
  public PolygonalEditor appendVertex(final int[] geometryId, final Point point) {
    if (geometryId == null || geometryId.length != 1) {
    } else {
      final int ringIndex = geometryId[0];
      appendVertex(ringIndex, point);
    }
    return this;
  }

  @Override
  public Polygon clone() {
    return (Polygon)super.clone();
  }

  @Override
  public PolygonalEditor deleteVertex(final int[] vertexId) {
    if (vertexId == null || vertexId.length < 2) {
    } else {
      final int partIndex = vertexId[0];
      final LinearRingEditor editor = getEditor(partIndex);
      if (editor != null) {
        final int[] childVertexId = Arrays.copyOfRange(vertexId, 1, vertexId.length);
        editor.deleteVertex(childVertexId);
      }
    }
    return this;
  }

  @Override
  public Iterable<PolygonEditor> editors() {
    return Collections.singleton(this);
  }

  @Override
  public boolean equalsVertex(final int axisCount, final int[] geometryId, final int vertexIndex,
    final Point point) {
    final GeometryEditor<?> geometryEditor = getGeometryEditor(geometryId, 0);
    if (geometryEditor == null) {
      return false;
    } else {
      return geometryEditor.equalsVertex(axisCount, vertexIndex, point);
    }
  }

  @Override
  public void forEachGeometry(final Consumer<Geometry> action) {
    for (final LinearRingEditor editor : this.editors) {
      action.accept(editor);
    }
  }

  @Override
  public void forEachVertex(final BiConsumerDouble action) {
    for (final LinearRingEditor editor : this.editors) {
      editor.forEachVertex(action);
    }
  }

  public LinearRingEditor getEditor(final int ringIndex) {
    if (ringIndex < 0 || ringIndex >= this.editors.size()) {
      return null;
    } else {
      return this.editors.get(ringIndex);
    }
  }

  @Override
  public int[] getFirstGeometryId() {
    return new int[] {
      0
    };
  }

  @Override
  public GeometryEditor<?> getGeometryEditor(final int[] geometryId, final int idOffset,
    final int idLength) {
    if (geometryId != null && idOffset == idLength - 1) {
      final int partIndex = geometryId[idOffset];
      final LineStringEditor geometryEditor = getEditor(partIndex);
      if (geometryEditor != null) {
        return geometryEditor;
      }
    }
    return null;
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
  public int getVertexCount(final int[] geometryId, final int idLength) {
    if (geometryId == null || idLength < 1) {
      return 0;
    } else {
      final int partIndex = geometryId[0];
      final LinearRingEditor editor = getEditor(partIndex);
      if (editor != null) {
        final int[] childGeometryId = Arrays.copyOfRange(geometryId, 1, idLength);
        return editor.getVertexCount(childGeometryId);
      }
    }
    return 0;
  }

  @Override
  public int hashCode() {
    return getBoundingBox().hashCode();
  }

  @Override
  public GeometryEditor<?> insertVertex(final int[] vertexId, final Point point) {
    if (vertexId == null || vertexId.length < 1) {
    } else {
      final int ringIndex = vertexId[0];
      final LinearRingEditor editor = getEditor(ringIndex);
      if (editor != null) {
        final int[] childVertexId = Arrays.copyOfRange(vertexId, 1, vertexId.length);
        editor.insertVertex(childVertexId, point);
      }
    }
    return this;
  }

  @Override
  public boolean isEmpty() {
    return this.editors.isEmpty();
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

  public void removeRing(final int index) {
    this.editors.remove(index);
  }

  public Iterable<LinearRingEditor> ringEditors() {
    return Collections.unmodifiableList(this.editors);
  }

  @Override
  public PolygonEditor setAxisCount(final int axisCount) {
    final int oldAxisCount = getAxisCount();
    if (oldAxisCount != axisCount) {
      super.setAxisCount(axisCount);
      for (final LinearRingEditor editor : this.editors) {
        editor.setAxisCount(axisCount);
      }
    }
    return this;
  }

  @Override
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

  @Override
  public PolygonEditor setCoordinate(final int[] vertexId, final int axisIndex,
    final double coordinate) {
    if (vertexId == null || vertexId.length != 2) {
      throw new IllegalArgumentException("Invalid vertex Id");
    } else {
      final int ringIndex = vertexId[0];
      final int vertexIndex = vertexId[1];
      setCoordinate(ringIndex, vertexIndex, axisIndex, coordinate);
    }
    return this;
  }
}
