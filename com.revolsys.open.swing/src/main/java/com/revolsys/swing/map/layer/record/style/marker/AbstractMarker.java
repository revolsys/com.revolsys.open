package com.revolsys.swing.map.layer.record.style.marker;

import java.util.Map;

import javax.swing.Icon;

import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.util.Property;

public abstract class AbstractMarker extends AbstractMarkerGroupElement implements Marker {
  private Icon icon;

  public AbstractMarker() {
    super();
  }

  public AbstractMarker(final Map<String, ? extends Object> properties) {
    super(properties);
  }

  public AbstractMarker(final String name) {
    super(name);
  }

  public AbstractMarker(final String name, final String title) {
    super(name, title);
  }

  @Override
  public Icon getIcon() {
    if (this.icon == null) {
      this.icon = newIcon(new MarkerStyle());
    }
    return this.icon;
  }

  @Override
  public String getName() {
    final String name = super.getName();
    if (Property.hasValue(name)) {
      return name;
    } else {
      return "unknown";
    }
  }

  @Override
  public String toString() {
    final String name = getName();
    if (Property.hasValue(name)) {
      return name;
    } else {
      return super.toString();
    }
  }
}
