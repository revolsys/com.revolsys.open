package com.revolsys.swing.map.symbol;

import java.util.Map;

import javax.swing.Icon;

import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.marker.Marker;

public abstract class AbstractSymbol extends AbstractSymbolElement implements Symbol {
  private Icon icon;

  public AbstractSymbol() {
    super();
  }

  public AbstractSymbol(final Map<String, ? extends Object> properties) {
    super(properties);
  }

  public AbstractSymbol(final String name) {
    super(name);
  }

  public AbstractSymbol(final String name, final String title) {
    super(name, title);
  }

  @Override
  public Icon getIcon() {
    if (this.icon == null) {
      final Marker marker = newMarker();
      this.icon = marker.newIcon(new MarkerStyle());
    }
    return this.icon;
  }
}
