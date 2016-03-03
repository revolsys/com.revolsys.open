package com.revolsys.swing.map.symbol;

import java.util.Map;

import com.revolsys.swing.map.layer.record.style.marker.Marker;
import com.revolsys.swing.map.layer.record.style.marker.SvgMarker;

public class SvgSymbol extends AbstractSymbol {

  public SvgSymbol(final Map<String, ? extends Object> properties) {
    super(properties);
  }

  public SvgSymbol(final String name) {
    super(name);
  }

  public SvgSymbol(final String name, final String title) {
    super(name, title);
  }

  @Override
  public String getTypeName() {
    return "symbolSvg";
  }

  @Override
  public Marker newMarker() {
    final String name = getName();
    return new SvgMarker(name);
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();

    return map;
  }
}
