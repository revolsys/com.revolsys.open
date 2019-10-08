package com.revolsys.util;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.GeometryFactory;

public abstract class AbstractMapTile<D> implements BoundingBoxProxy {
  private final BoundingBox boundingBox;

  private final int heightPixels;

  private final int widthPixels;

  private D data;

  public AbstractMapTile(final BoundingBox boundingBox, final int width, final int height) {
    this.boundingBox = boundingBox;
    this.widthPixels = width;
    this.heightPixels = height;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof AbstractMapTile) {
      final AbstractMapTile<?> tile = (AbstractMapTile<?>)obj;
      return tile.getBoundingBox().equals(this.boundingBox);
    }
    return false;
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public D getData() {
    return this.data;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.boundingBox.getGeometryFactory();
  }

  public int getHeightPixels() {
    return this.heightPixels;
  }

  public int getWidthPixels() {
    return this.widthPixels;
  }

  @Override
  public int hashCode() {
    return this.boundingBox.hashCode();
  }

  public final D loadData() {
    final D data = loadDataDo();
    this.data = data;
    return data;
  }

  protected abstract D loadDataDo();

  protected void setData(final D data) {
    this.data = data;
  }
}
