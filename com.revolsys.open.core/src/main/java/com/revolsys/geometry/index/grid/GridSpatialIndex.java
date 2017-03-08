package com.revolsys.geometry.index.grid;

import java.util.function.Consumer;

import com.revolsys.geometry.index.SpatialIndex;
import com.revolsys.geometry.model.BoundingBox;

public class GridSpatialIndex<V> implements SpatialIndex<V> {

  @Override
  public void forEach(final Consumer<? super V> action) {
    // TODO Auto-generated method stub

  }

  @Override
  public void forEach(final double x, final double y, final Consumer<? super V> action) {
    // TODO Auto-generated method stub

  }

  @Override
  public void forEach(final double minX, final double minY, final double maxX, final double maxY,
    final Consumer<? super V> action) {
    // TODO Auto-generated method stub

  }

  @Override
  public int getSize() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void insertItem(final BoundingBox boundingBox, final V item) {

  }

  @Override
  public boolean removeItem(final BoundingBox boundingBox, final V item) {
    return false;
  }

}
