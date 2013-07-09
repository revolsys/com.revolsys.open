package com.revolsys.swing.map.layer;

public abstract class AbstractLayerFactory<T extends Layer> implements
  LayerFactory<T> {
  private String description;

  private String typeName;

  public AbstractLayerFactory(final String typeName, final String description) {
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

  protected void setTypeName(final String typeName) {
    this.typeName = typeName;
  }

  @Override
  public String toString() {
    return description;
  }
}
