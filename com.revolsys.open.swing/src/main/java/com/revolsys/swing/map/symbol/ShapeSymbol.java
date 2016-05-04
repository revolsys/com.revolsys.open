package com.revolsys.swing.map.symbol;

import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.swing.map.layer.record.style.marker.Marker;
import com.revolsys.swing.map.layer.record.style.marker.ShapeMarker;

public class ShapeSymbol extends AbstractSymbol {
  public ShapeSymbol(final Map<String, ? extends Object> properties) {
    super(properties);
  }

  public ShapeSymbol(final String name) {
    super(name);
  }

  public ShapeSymbol(final String name, final String title) {
    super(name, title);
  }

  @Override
  public String getTypeName() {
    return "symbolShape";
  }

  @Override
  public Marker newMarker() {
    final String name = getName();
    return new ShapeMarker(name);
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();

    return map;
  }
}
