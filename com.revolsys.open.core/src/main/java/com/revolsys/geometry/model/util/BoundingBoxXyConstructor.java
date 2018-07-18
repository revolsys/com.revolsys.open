package com.revolsys.geometry.model.util;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXYGeometryFactory;
import com.revolsys.util.function.BiConsumerDouble;

public class BoundingBoxXyConstructor extends BoundingBoxDoubleXY implements BiConsumerDouble {

  private double minX = Double.POSITIVE_INFINITY;

  private double minY = Double.POSITIVE_INFINITY;

  private double maxX = Double.NEGATIVE_INFINITY;

  private double maxY = Double.NEGATIVE_INFINITY;

  private GeometryFactory geometryFactory;

  public BoundingBoxXyConstructor() {
  }

  public BoundingBoxXyConstructor(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  @Override
  public void accept(final double x, final double y) {
    if (x < this.minX) {
      this.minX = x;
    }
    if (x > this.maxX) {
      this.maxX = x;
    }
    if (y < this.minY) {
      this.minY = y;
    }
    if (y > this.maxY) {
      this.maxY = y;
    }
  }

  @Override
  public void expand(final BoundingBox boundingBox) {
    final double minX = boundingBox.getMinX();
    final double minY = boundingBox.getMinY();
    final double maxX = boundingBox.getMaxX();
    final double maxY = boundingBox.getMaxY();
    expand(minX, minY, maxX, maxY);
  }

  @Override
  public void expand(final BoundingBoxProxy boundingBoxProxy) {
    final BoundingBox boundingBox = boundingBoxProxy.getBoundingBox();
    expand(boundingBox);
  }

  @Override
  public void expand(final double minX, final double minY, final double maxX, final double maxY) {
    if (minX < this.minX) {
      this.minX = minX;
    }
    if (minY < this.minY) {
      this.minY = minY;
    }
    if (maxX > this.maxX) {
      this.maxX = maxX;
    }
    if (maxY > this.maxY) {
      this.maxY = maxY;
    }
  }

  public void expandPoint(final double x, final double y) {
    if (x < this.minX) {
      this.minX = x;
    }
    if (x > this.maxX) {
      this.maxX = x;
    }
    if (y < this.minY) {
      this.minY = y;
    }
    if (y > this.maxY) {
      this.maxY = y;
    }
  }

  public BoundingBox newBoundingBox() {
    if (this.geometryFactory == null) {
      return new BoundingBoxDoubleXY(this.minX, this.minY, this.maxX, this.maxY);
    } else {
      return new BoundingBoxDoubleXYGeometryFactory(this.geometryFactory, this.minX, this.minY,
        this.maxX, this.maxY);
    }
  }
}
