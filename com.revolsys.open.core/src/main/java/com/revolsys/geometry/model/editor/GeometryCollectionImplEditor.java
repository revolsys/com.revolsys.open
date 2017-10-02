package com.revolsys.geometry.model.editor;

import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.GeometryCollectionImpl;

public class GeometryCollectionImplEditor
  extends AbstractGeometryCollectionEditor<GeometryCollectionImpl, Geometry, GeometryEditor<?>> {
  private static final long serialVersionUID = 1L;

  public GeometryCollectionImplEditor(
    final AbstractGeometryCollectionEditor<?, ?, ?> geometryEditor,
    final GeometryCollectionImpl geometryCollection) {
    super(geometryEditor, geometryCollection);
  }

  public GeometryCollectionImplEditor(final GeometryCollectionImpl geometryCollection) {
    this(null, geometryCollection);
  }

  public GeometryCollectionImplEditor(final GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

  public GeometryCollectionImplEditor(final GeometryFactory geometryFactory,
    final List<GeometryEditor<?>> editors) {
    super(geometryFactory, editors);
  }

  @Override
  public GeometryCollectionImplEditor clone() {
    return (GeometryCollectionImplEditor)super.clone();
  }

  @Override
  public Geometry prepare() {
    return this;
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
