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
import java.util.Iterator;
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
public class SIRtree extends AbstractSTRtree {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final Comparator comparator = new Comparator() {
    @Override
    public int compare(final Object o1, final Object o2) {
      return compareDoubles(((Interval)((Boundable)o1).getBounds()).getCentre(),
        ((Interval)((Boundable)o2).getBounds()).getCentre());
    }
  };

  private final IntersectsOp intersectsOp = new IntersectsOp() {
    @Override
    public boolean intersects(final Object aBounds, final Object bBounds) {
      return ((Interval)aBounds).intersects((Interval)bBounds);
    }
  };

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
  protected Comparator getComparator() {
    return this.comparator;
  }

  @Override
  protected IntersectsOp getIntersectsOp() {
    return this.intersectsOp;
  }

  /**
   * Inserts an item having the given bounds into the tree.
   */
  public void insert(final double x1, final double x2, final Object item) {
    super.insert(new Interval(Math.min(x1, x2), Math.max(x1, x2)), item);
  }

  @Override
  protected AbstractNode newNode(final int level) {
    return new AbstractNode(level) {
      /**
       *
       */
      private static final long serialVersionUID = 1L;

      @Override
      protected Object computeBounds() {
        Interval bounds = null;
        for (final Iterator i = getChildBoundables().iterator(); i.hasNext();) {
          final Boundable childBoundable = (Boundable)i.next();
          if (bounds == null) {
            bounds = new Interval((Interval)childBoundable.getBounds());
          } else {
            bounds.expandToInclude((Interval)childBoundable.getBounds());
          }
        }
        return bounds;
      }
    };
  }

  /**
   * Returns items whose bounds intersect the given value.
   */
  public List query(final double x) {
    return query(x, x);
  }

  /**
   * Returns items whose bounds intersect the given bounds.
   * @param x1 possibly equal to x2
   */
  public List query(final double x1, final double x2) {
    return super.query(new Interval(Math.min(x1, x2), Math.max(x1, x2)));
  }

}
