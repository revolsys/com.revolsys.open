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
package com.revolsys.jts.index.quadtree;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.impl.AbstractPoint;

/**
 * A Key is a unique identifier for a node in a quadtree.
 * It contains a lower-left point and a level number. The level number
 * is the power of two for the size of the node envelope
 *
 * @version 1.7
 */
public class Key extends AbstractPoint {

  public static int computeQuadLevel(final BoundingBox env) {
    final double dx = env.getWidth();
    final double dy = env.getHeight();
    final double dMax = dx > dy ? dx : dy;
    final int level = DoubleBits.exponent(dMax) + 1;
    return level;
  }

  private int level = 0;

  private BoundingBox env = null;

  private double x;

  private double y;

  public Key(final BoundingBox itemEnv) {
    computeKey(itemEnv);
  }

  /**
   * return a square envelope containing the argument envelope,
   * whose extent is a power of two and which is based at a power of 2
   */
  public void computeKey(final BoundingBox itemEnv) {
    level = computeQuadLevel(itemEnv);
    env = null;
    computeKey(level, itemEnv);
    // MD - would be nice to have a non-iterative form of this algorithm
    while (!getKeyEnvelope().covers(itemEnv)) {
      level += 1;
      env = null;
      computeKey(level, itemEnv);
    }
  }

  private void computeKey(final int level, final BoundingBox itemEnv) {
    final double quadSize = DoubleBits.powerOf2(level);
    this.x = Math.floor(itemEnv.getMinX() / quadSize) * quadSize;
    this.y = Math.floor(itemEnv.getMinY() / quadSize) * quadSize;
  }

  public Point getCentre() {
    final BoundingBox envelope = getKeyEnvelope();
    return new Coordinate((envelope.getMinX() + envelope.getMaxX()) / 2,
      (envelope.getMinY() + envelope.getMaxY()) / 2);
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    switch (axisIndex) {
      case 0:
        return x;
      case 1:
        return y;
      default:
        return Double.NaN;
    }
  }

  public BoundingBox getKeyEnvelope() {
    if (env == null) {
      final double quadSize = DoubleBits.powerOf2(level);
      final double x2 = x + quadSize;
      final double y2 = y + quadSize;
      env = new Envelope(2, x, y, x2, y2);
    }
    return env;
  }

  public int getLevel() {
    return level;
  }

}
