package com.revolsys.geometry.model.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryCollection;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;

public abstract class AbstractGeometryCollectionEditor<GC extends Geometry, G extends Geometry, GE extends GeometryEditor<?>>
  extends AbstractGeometryEditor<GE> implements GeometryCollection {
  private static final long serialVersionUID = 1L;

  private GC geometry;

  private final List<GE> editors = new ArrayList<>();

  public AbstractGeometryCollectionEditor(final AbstractGeometryEditor<?> parentEditor,
    final GC geometry) {
    super(parentEditor, geometry);
    this.geometry = geometry;
    for (final Geometry part : geometry.geometries()) {
      @SuppressWarnings("unchecked")
      final GE editor = (GE)part.newGeometryEditor(parentEditor);
      this.editors.add(editor);
    }
  }

  public AbstractGeometryCollectionEditor(final AbstractGeometryEditor<?> parentEditor,
    final GeometryFactory geometryFactory, final GE[] editors) {
    super(parentEditor, geometryFactory);
    if (editors != null) {
      for (final GE editor : editors) {
        this.editors.add(editor);
      }
    }
  }

  public AbstractGeometryCollectionEditor(final GC geometry) {
    this(null, geometry);
  }

  public AbstractGeometryCollectionEditor(final GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

  public AbstractGeometryCollectionEditor(final GeometryFactory geometryFactory,
    final List<GE> editors) {
    super(geometryFactory);
    this.editors.addAll(editors);
  }

  protected void addEditor(final GE editor) {
    this.editors.add(editor);
  }

  @Override
  public GeometryEditor<?> appendVertex(final int[] geometryId, final Point point) {
    if (geometryId == null || geometryId.length < 1) {
    } else {
      final int partIndex = geometryId[0];
      final GE editor = getEditor(partIndex);
      if (editor != null) {
        final int[] childGeometryId = Arrays.copyOfRange(geometryId, 1, geometryId.length);
        final GeometryEditor<?> newEditor = editor.appendVertex(childGeometryId, point);
        if (newEditor != editor) {
          final List<GeometryEditor<?>> editors = new ArrayList<>(this.editors);
          editors.set(partIndex, newEditor);
          final GeometryFactory geometryFactory = getGeometryFactory();
          return new GeometryCollectionImplEditor(geometryFactory, editors);
        }
      }
    }
    return this;
  }

  @Override
  public GeometryCollection clone() {
    return (GeometryCollection)super.clone();
  }

  @Override
  public AbstractGeometryCollectionEditor<?, ?, ?> deleteVertex(final int[] vertexId) {
    if (vertexId == null || vertexId.length < 2) {
    } else {
      final int partIndex = vertexId[0];
      final GE editor = getEditor(partIndex);
      if (editor != null) {
        final int[] childVertexId = Arrays.copyOfRange(vertexId, 1, vertexId.length);
        editor.deleteVertex(childVertexId);
      }
    }
    return this;
  }

  @Override
  public Iterable<GE> editors() {
    return Collections.unmodifiableList(this.editors);
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

  public GE getEditor(final int partIndex) {
    if (0 <= partIndex && partIndex < this.editors.size()) {
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
  public GeometryEditor<?> insertVertex(final int[] vertexId, final Point point) {
    if (vertexId == null || vertexId.length < 1) {
    } else {
      final int partIndex = vertexId[0];
      final GE editor = getEditor(partIndex);
      if (editor != null) {
        final int[] childVertexId = Arrays.copyOfRange(vertexId, 1, vertexId.length);
        final GeometryEditor<?> newEditor = editor.insertVertex(childVertexId, point);
        if (newEditor != editor) {
          final List<GeometryEditor<?>> editors = new ArrayList<>(this.editors);
          editors.set(partIndex, newEditor);
          final GeometryFactory geometryFactory = getGeometryFactory();
          return new GeometryCollectionImplEditor(geometryFactory, editors);
        }
      }
    }
    return this;
  }

  @Override
  public boolean isEmpty() {
    return this.editors.isEmpty();
  }

  @Override
  public boolean isModified() {
    for (final GE editor : this.editors) {
      if (editor.isModified()) {
        return true;
      }
    }
    // TODO adding parts
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public GC newGeometry() {
    if (isModified()) {
      final List<G> geometries = new ArrayList<>();
      for (final GE editor : this.editors) {
        final G newGeometry = (G)editor.newGeometry();
        geometries.add(newGeometry);
      }
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.geometry(geometries);
    } else {
      return this.geometry;
    }
  }

  @Override
  public GeometryEditor<?> setAxisCount(final int axisCount) {
    final int oldAxisCount = getAxisCount();
    if (oldAxisCount != axisCount) {
      super.setAxisCount(axisCount);
      for (final GE editor : this.editors) {
        editor.setAxisCount(axisCount);
      }
    }
    return this;
  }

  @Override
  public AbstractGeometryCollectionEditor<?, ?, ?> setCoordinate(final int[] vertexId,
    final int axisIndex, final double coordinate) {
    if (vertexId == null || vertexId.length < 2) {
    } else {
      final int partIndex = vertexId[0];
      final GE editor = getEditor(partIndex);
      if (editor != null) {
        final int[] childVertexId = Arrays.copyOfRange(vertexId, 1, vertexId.length);
        editor.setCoordinate(childVertexId, axisIndex, coordinate);
      }
    }
    return this;
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
