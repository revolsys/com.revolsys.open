package com.revolsys.gis.algorithm.index.quadtree;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.index.quadtree.DoubleBits;

/**
 * A Key is a unique identifier for a node in a quadtree.
 * It contains a lower-left point and a level number. The level number
 * is the power of two for the size of the node envelope
 *
 * @version 1.7
 */
public class Key {

  public static int computeQuadLevel(final BoundingBox env) {
    final double dx = env.getWidth();
    final double dy = env.getHeight();
    final double dMax = dx > dy ? dx : dy;
    final int level = DoubleBits.exponent(dMax) + 1;
    return level;
  }

  // the fields which make up the key
  private final Coordinates pt = new Coordinate();

  private int level = 0;

  // auxiliary data which is derived from the key for use in computation
  private BoundingBox env = null;

  public Key(final BoundingBox itemEnv) {
    computeKey(itemEnv);
  }

  /**
   * return a square envelope containing the argument envelope,
   * whose extent is a power of two and which is based at a power of 2
   */
  private void computeKey(final BoundingBox itemEnv) {
    level = computeQuadLevel(itemEnv);
    env = new Envelope();
    computeKey(level, itemEnv);
    // MD - would be nice to have a non-iterative form of this algorithm
    while (!env.contains(itemEnv)) {
      level += 1;
      computeKey(level, itemEnv);
    }
  }

  private void computeKey(final int level, final BoundingBox itemEnv) {
    final double quadSize = DoubleBits.powerOf2(level);
    pt.setX(Math.floor(itemEnv.getMinX() / quadSize) * quadSize);
    pt.setY(Math.floor(itemEnv.getMinY() / quadSize) * quadSize);
    final double x1 = pt.getX();
    final double y1 = pt.getY();
    final double x2 = x1 + quadSize;
    final double y2 = y1 + quadSize;
    env = new Envelope(2, x1, y1, x2, y2);
  }

  public Coordinates getCentre() {
    final double minX = env.getMinX();
    final double maxX = env.getMaxX();
    final double minY = env.getMinY();
    final double maxY = env.getMaxY();
    return new Coordinate((minX + maxX) / 2, (minY + maxY) / 2,
      Coordinates.NULL_ORDINATE);
  }

  public BoundingBox getEnvelope() {
    return env;
  }

  public int getLevel() {
    return level;
  }

  public Coordinates getPoint() {
    return pt;
  }
}
