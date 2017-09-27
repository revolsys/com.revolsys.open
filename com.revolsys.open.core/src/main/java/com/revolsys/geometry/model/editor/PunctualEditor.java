package com.revolsys.geometry.model.editor;

import com.revolsys.geometry.model.Punctual;

public interface PunctualEditor extends GeometryEditor, Punctual {

  @Override
  Punctual newGeometry();
}
