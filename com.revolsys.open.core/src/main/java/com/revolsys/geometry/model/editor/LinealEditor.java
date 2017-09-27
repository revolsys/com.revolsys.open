package com.revolsys.geometry.model.editor;

import com.revolsys.geometry.model.Lineal;

public interface LinealEditor extends GeometryEditor, Lineal {
  @Override
  Lineal newGeometry();

}
