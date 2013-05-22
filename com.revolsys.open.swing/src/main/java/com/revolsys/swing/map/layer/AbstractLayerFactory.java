package com.revolsys.swing.map.layer;

import javax.swing.JTabbedPane;

public abstract class AbstractLayerFactory<T extends Layer> implements
  LayerFactory<T> {
  private String description;

  private String typeName;

  public AbstractLayerFactory(String typeName, String description) {
    this.typeName = typeName;
    this.description = description;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getTypeName() {
    return typeName;
  }

  protected void setDescription(final String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return description;
  }
  

  protected void setTypeName(final String typeName) {
    this.typeName = typeName;
  }
}
