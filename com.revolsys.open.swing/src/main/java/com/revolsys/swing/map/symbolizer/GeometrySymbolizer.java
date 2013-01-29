package com.revolsys.swing.map.symbolizer;

public interface GeometrySymbolizer extends Symbolizer {
  CharSequence getGeometryPropertyName();

  void setGeometryPropertyName(CharSequence geometryName);
}
