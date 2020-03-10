package com.revolsys.geometry.model.editor;

import com.revolsys.geometry.model.Lineal;

public interface LinealEditor extends GeometryEditor<LineStringEditor>, Lineal {
  @Override
  Lineal newGeometry();

  void removeGeometry(int index);

}
