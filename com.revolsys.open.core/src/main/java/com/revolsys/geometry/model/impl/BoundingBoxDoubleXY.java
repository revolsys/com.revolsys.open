/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.geometry.model.impl;

import java.io.Serializable;

import com.revolsys.geometry.model.BoundingBox;

public class BoundingBoxDoubleXY implements Serializable, BoundingBox {
  /** The serialization version. */
  private static final long serialVersionUID = -1L;

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  private double maxX;

  private double maxY;

  private double minX;

  private double minY;

  public BoundingBoxDoubleXY() {
    this.minX = Double.NaN;
    this.minY = Double.NaN;
    this.maxX = Double.NaN;
    this.maxY = Double.NaN;
  }

  public BoundingBoxDoubleXY(final BoundingBox boundingBox) {
    this.minX = boundingBox.getMinX();
    this.minY = boundingBox.getMinY();
    this.maxX = boundingBox.getMaxX();
    this.maxY = boundingBox.getMaxY();
  }

  public BoundingBoxDoubleXY(final double... bounds) {
    this.minX = bounds[0];
    this.minY = bounds[1];
    this.maxX = bounds[2];
    this.maxY = bounds[3];
  }

  public BoundingBoxDoubleXY(final double x, final double y) {
    this.minX = x;
    this.minY = y;
    this.maxX = x;
    this.maxY = y;
  }

  public BoundingBoxDoubleXY(final double minX, final double maxX, final double minY,
    final double maxY) {
    this.minX = minX;
    this.maxX = maxX;
    this.minY = minY;
    this.maxY = maxY;
  }

  @Override
  public BoundingBoxDoubleXY clone() {
    try {
      return (BoundingBoxDoubleXY)super.clone();
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public int getAxisCount() {
    return 2;
  }

  @Override
  public double[] getBounds() {
    // TODO Auto-generated method stub
    return new double[] {
      this.minX, this.minY, this.maxX, this.maxY
    };
  }

  @Override
  public double getMax(final int i) {
    if (i == 0) {
      return this.maxX;
    } else if (i == 1) {
      return this.maxY;
    } else {
      return Double.NaN;
    }
  }

  @Override
  public double getMaxX() {
    return this.maxX;
  }

  @Override
  public double getMaxY() {
    return this.maxY;
  }

  @Override
  public double getMin(final int i) {
    if (i == 0) {
      return this.minX;
    } else if (i == 1) {
      return this.minY;
    } else {
      return Double.NaN;
    }
  }

  @Override
  public double getMinX() {
    return this.minX;
  }

  @Override
  public double getMinY() {
    return this.minY;
  }

  @Override
  public BoundingBox newBoundingBox(final double x, final double y) {
    return new BoundingBoxDoubleXY(x, y);
  }

  @Override
  public BoundingBox newBoundingBox(final double minX, final double minY, final double maxX,
    final double maxY) {
    return new BoundingBoxDoubleXY(minX, maxX, minY, maxY);
  }

  @Override
  public BoundingBox newBoundingBoxEmpty() {
    return new BoundingBoxDoubleXY();
  }

  protected void setMaxX(final double maxX) {
    this.maxX = maxX;
  }

  protected void setMaxY(final double maxY) {
    this.maxY = maxY;
  }

  protected void setMinX(final double minX) {
    this.minX = minX;
  }

  protected void setMinY(final double minY) {
    this.minY = minY;
  }

  @Override
  public String toString() {
    return BoundingBox.toString(this);
  }
}
