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
package com.revolsys.geometry.test.old.index;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.geometry.index.strtree.Boundable;
import com.revolsys.geometry.index.strtree.BoundingBoxNode;
import com.revolsys.geometry.index.strtree.STRtree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.PointDouble;

/**
 * @version 1.7
 */
public class STRtreeDemo {

  public static class TestTree extends STRtree<Object> {
    private static final long serialVersionUID = 1L;

    public TestTree(final int nodeCapacity) {
      super(nodeCapacity);
    }

    @Override
    public List boundablesAtLevel(final int level) {
      return super.boundablesAtLevel(level);
    }

    @Override
    public BoundingBoxNode<Object> getRoot() {
      return this.root;
    }

    @Override
    public List newParentBoundables(final List verticalSlice, final int newLevel) {
      return super.newParentBoundables(verticalSlice, newLevel);
    }

    @Override
    public List newParentBoundablesFromVerticalSlice(final List childBoundables,
      final int newLevel) {
      return super.newParentBoundablesFromVerticalSlice(childBoundables, newLevel);
    }

    @Override
    public List<List<Boundable<BoundingBox, Object>>> verticalSlices(final List childBoundables,
      final int size) {
      return super.verticalSlices(childBoundables, size);
    }
  }

  private static final double EXTENT = 100;

  private static GeometryFactory factory = GeometryFactory.DEFAULT;

  private static final int ITEM_COUNT = 20;

  private static final double MAX_ITEM_EXTENT = 15;

  private static final double MIN_ITEM_EXTENT = 3;

  private static final int NODE_CAPACITY = 4;

  private static BoundingBox envelope(final Boundable b) {
    return (BoundingBox)b.getBounds();
  }

  private static void initTree(final TestTree t, final List sourceEnvelopes) {
    for (final Iterator i = sourceEnvelopes.iterator(); i.hasNext();) {
      final BoundingBox sourceEnvelope = (BoundingBox)i.next();
      t.insertItem(sourceEnvelope, sourceEnvelope);
    }
    t.build();
  }

  public static void main(final String[] args) throws Exception {
    final List envelopes = sourceData();
    final TestTree t = new TestTree(NODE_CAPACITY);
    initTree(t, envelopes);
    final PrintStream printStream = System.out;
    printSourceData(envelopes, printStream);
    printLevels(t, printStream);
  }

  public static void printBoundables(final List boundables, final String title,
    final PrintStream out) {
    out.println("============ " + title + " ============\n");
    out.print("GEOMETRYCOLLECTION(");
    boolean first = true;
    for (final Iterator i = boundables.iterator(); i.hasNext();) {
      final Boundable boundable = (Boundable)i.next();
      if (first) {
        first = false;
      } else {
        out.print(",");
      }
      out.print(toString(boundable));
    }
    out.println(")\n");
  }

  public static void printLevels(final TestTree t, final PrintStream out) {
    for (int i = 0; i <= t.getRoot().getLevel(); i++) {
      printBoundables(t.boundablesAtLevel(i), "Level " + i, out);
    }
  }

  public static void printSourceData(final List sourceEnvelopes, final PrintStream out) {
    out.println("============ Source Data ============\n");
    out.print("GEOMETRYCOLLECTION(");
    boolean first = true;
    for (final Iterator i = sourceEnvelopes.iterator(); i.hasNext();) {
      final BoundingBox e = (BoundingBox)i.next();
      final Geometry g = factory.polygon(factory.linearRing(new Point[] {
        new PointDouble(e.getMinX(), e.getMinY(), Geometry.NULL_ORDINATE),
        new PointDouble(e.getMinX(), e.getMaxY(), Geometry.NULL_ORDINATE),
        new PointDouble(e.getMaxX(), e.getMaxY(), Geometry.NULL_ORDINATE),
        new PointDouble(e.getMaxX(), e.getMinY(), Geometry.NULL_ORDINATE),
        new PointDouble(e.getMinX(), e.getMinY(), Geometry.NULL_ORDINATE)
      }));
      if (first) {
        first = false;
      } else {
        out.print(",");
      }
      out.print(g);
    }
    out.println(")\n");
  }

  private static Polygon randomRectangle() {
    final double width = MIN_ITEM_EXTENT + (MAX_ITEM_EXTENT - MIN_ITEM_EXTENT) * Math.random();
    final double height = MIN_ITEM_EXTENT + (MAX_ITEM_EXTENT - MIN_ITEM_EXTENT) * Math.random();
    final double bottom = EXTENT * Math.random();
    final double left = EXTENT * Math.random();
    final double top = bottom + height;
    final double right = left + width;
    return factory.polygon(factory.linearRing(new Point[] {
      new PointDouble(left, bottom, Geometry.NULL_ORDINATE),
      new PointDouble(right, bottom, Geometry.NULL_ORDINATE),
      new PointDouble(right, top, Geometry.NULL_ORDINATE),
      new PointDouble(left, top, Geometry.NULL_ORDINATE),
      new PointDouble(left, bottom, Geometry.NULL_ORDINATE)
    }));
  }

  private static List sourceData() {
    final ArrayList envelopes = new ArrayList();
    for (int i = 0; i < ITEM_COUNT; i++) {
      envelopes.add(randomRectangle().getBoundingBox());
    }
    return envelopes;
  }

  private static String toString(final Boundable b) {
    return "POLYGON((" + envelope(b).getMinX() + " " + envelope(b).getMinY() + ", "
      + envelope(b).getMinX() + " " + envelope(b).getMaxY() + ", " + envelope(b).getMaxX() + " "
      + envelope(b).getMaxY() + ", " + envelope(b).getMaxX() + " " + envelope(b).getMinY() + ","
      + envelope(b).getMinX() + " " + envelope(b).getMinY() + "))";
  }

  public STRtreeDemo() {
  }

}
