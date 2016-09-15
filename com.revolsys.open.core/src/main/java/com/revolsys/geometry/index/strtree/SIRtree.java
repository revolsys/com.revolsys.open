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
package com.revolsys.geometry.index.strtree;

import java.util.Comparator;
import java.util.List;

/**
 * One-dimensional version of an STR-packed R-tree. SIR stands for
 * "Sort-Interval-Recursive". STR-packed R-trees are described in:
 * P. Rigaux, Michel Scholl and Agnes Voisard. Spatial Databases With
 * Application To GIS. Morgan Kaufmann, San Francisco, 2002.
 * @see STRtree
 *
 * @version 1.7
 */
public class SIRtree<I> extends AbstractSTRtree<Interval, I, IntervalNode<I>>
  implements Comparator<Boundable<Interval, I>> {
  private static final long serialVersionUID = 1L;

  /**
   * Constructs an SIRtree with the default node capacity.
   */
  public SIRtree() {
    this(10);
  }

  /**
   * Constructs an SIRtree with the given maximum number of child nodes that
   * a node may have
   */
  public SIRtree(final int nodeCapacity) {
    super(nodeCapacity);
  }

  @Override
  public int compare(final Boundable<Interval, I> o1, final Boundable<Interval, I> o2) {
    final Interval bounds1 = o1.getBounds();
    final Interval bounds2 = o2.getBounds();

    final double centre1 = bounds1.getCentre();
    final double centre2 = bounds2.getCentre();
    return compareDoubles(centre1, centre2);
  }

  @Override
  protected Comparator<Boundable<Interval, I>> getComparator() {
    return this;
  }

  /**
   * Inserts an item having the given bounds into the tree.
   */
  public void insert(final double x1, final double x2, final I item) {
    double min;
    double max;
    if (x1 < x2) {
      min = x1;
      max = x2;
    } else {
      min = x2;
      max = x1;
    }
    final Interval interval = new Interval(min, max);
    super.insert(interval, item);
  }

  @Override
  public boolean intersects(final Interval aBounds, final Interval bBounds) {
    return aBounds.intersects(bBounds);
  }

  @Override
  protected IntervalNode<I> newNode(final int level) {
    return new IntervalNode<I>(level);
  }

  /**
   * Returns items whose bounds intersect the given value.
   */
  public List<I> query(final double x) {
    return query(x, x);
  }

  /**
   * Returns items whose bounds intersect the given bounds.
   * @param x1 possibly equal to x2
   */
  public List<I> query(final double x1, final double x2) {
    double min;
    double max;
    if (x1 < x2) {
      min = x1;
      max = x2;
    } else {
      min = x2;
      max = x1;
    }
    final Interval interval = new Interval(min, max);
    return query(interval);
  }
}
