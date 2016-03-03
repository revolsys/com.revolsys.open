package com.revolsys.swing.map.symbol;

import javax.swing.Icon;

import com.revolsys.swing.map.layer.record.style.marker.Marker;

public interface Symbol {
  Icon getIcon();

  String getName();

  String getTitle();

  Marker newMarker();
}
