package com.revolsys.swing.map.layer.record.style.marker;

import java.util.Map;

import com.revolsys.io.map.MapSerializer;
import com.revolsys.properties.AbstractNameTitle;
import com.revolsys.record.io.format.json.JsonObject;

public abstract class AbstractMarkerGroupElement extends AbstractNameTitle
  implements MapSerializer {

  private MarkerGroup parent;

  public AbstractMarkerGroupElement() {
  }

  public AbstractMarkerGroupElement(final Map<String, ? extends Object> properties) {
    setProperties(properties);
  }

  public AbstractMarkerGroupElement(final String name) {
    super(name);
  }

  public AbstractMarkerGroupElement(final String name, final String title) {
    super(name, title);
  }

  public MarkerLibrary getMarkerLibrary() {
    final MarkerGroup parent = getParent();
    if (parent == null) {
      return null;
    } else {
      return parent.getMarkerLibrary();
    }
  }

  public MarkerGroup getParent() {
    return this.parent;
  }

  public abstract String getTypeName();

  public void setParent(final MarkerGroup parent) {
    this.parent = parent;
  }

  @Override
  public JsonObject toMap() {
    final String typeName = getTypeName();
    final JsonObject map = newTypeMap(typeName);
    map.put("name", getName());
    map.put("title", getTitle());
    return map;
  }

  @Override
  public String toString() {
    return getTitle();
  }
}
