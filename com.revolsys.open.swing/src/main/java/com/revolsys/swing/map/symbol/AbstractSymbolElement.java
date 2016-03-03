package com.revolsys.swing.map.symbol;

import java.util.Map;

import com.revolsys.io.map.MapSerializer;
import com.revolsys.properties.AbstractNameTitle;

public abstract class AbstractSymbolElement extends AbstractNameTitle implements MapSerializer {

  private SymbolGroup parent;

  public AbstractSymbolElement() {
  }

  public AbstractSymbolElement(final Map<String, ? extends Object> properties) {
    setProperties(properties);
  }

  public AbstractSymbolElement(final String name) {
    super(name);
  }

  public AbstractSymbolElement(final String name, final String title) {
    super(name, title);
  }

  @Override
  public abstract String getTypeName();

  @Override
  public Map<String, Object> toMap() {
    final String typeName = getTypeName();
    final Map<String, Object> map = newTypeMap(typeName);
    map.put("name", getName());
    map.put("title", getTitle());
    return map;
  }

  @Override
  public String toString() {
    return getTitle();
  }

  public SymbolGroup getParent() {
    return this.parent;
  }

  public SymbolLibrary getSymbolLibrary() {
    final SymbolGroup parent = getParent();
    if (parent == null) {
      return null;
    } else {
      return parent.getSymbolLibrary();
    }
  }

  public void setParent(final SymbolGroup parent) {
    this.parent = parent;
  }
}
