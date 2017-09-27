package com.revolsys.geometry.model.editor;

import com.revolsys.geometry.model.Polygonal;

public interface PolygonalEditor extends GeometryEditor, Polygonal {
  @Override
  Polygonal newGeometry();

  Iterable<PolygonEditor> polygonEditors();
}
