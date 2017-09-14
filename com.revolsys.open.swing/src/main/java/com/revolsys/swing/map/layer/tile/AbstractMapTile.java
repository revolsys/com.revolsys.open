package com.revolsys.swing.map.layer.tile;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;

public abstract class AbstractMapTile<D> implements GeometryFactoryProxy {
  private final BoundingBox boundingBox;

  private final int height;

  private final double resolution;

  private final int width;

  private D data;

  public AbstractMapTile(final BoundingBox boundingBox, final int width, final int height,
    final double resolution) {
    this.boundingBox = boundingBox;
    this.width = width;
    this.height = height;
    this.resolution = resolution;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof AbstractMapTile) {
      final AbstractMapTile<?> tile = (AbstractMapTile<?>)obj;
      return tile.getBoundingBox().equals(this.boundingBox);
    }
    return false;
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public D getData() {
    if (this.data == null) {
      this.data = loadData();
    }
    return this.data;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.boundingBox.getGeometryFactory();
  }

  public int getHeight() {
    return this.height;
  }

  public double getResolution() {
    return this.resolution;
  }

  public int getWidth() {
    return this.width;
  }

  protected abstract D loadData();

  protected D loadData(final GeometryFactory geometryFactory) {
    return loadData();
  }

}
