package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.util.MathUtil;

public class BaseBoundingBox implements BoundingBox {

  public BaseBoundingBox() {
    super();
  }

  @Override
  public BoundingBox clone() {
    try {
      return (BoundingBox)super.clone();
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof BoundingBox) {
      final BoundingBox boundingBox = (BoundingBox)other;
      return equals(boundingBox);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    if (isEmpty()) {
      return 0;
    } else {
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();
      int result = 17;
      result = 37 * result + MathUtil.hashCode(minX);
      result = 37 * result + MathUtil.hashCode(maxX);
      result = 37 * result + MathUtil.hashCode(minY);
      result = 37 * result + MathUtil.hashCode(maxY);
      return result;
    }
  }

  @Override
  public String toString() {
    return BoundingBox.toString(this);
  }
}
