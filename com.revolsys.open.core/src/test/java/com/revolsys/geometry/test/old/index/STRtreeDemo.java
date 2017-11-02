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
import java.util.List;

import com.revolsys.geometry.index.strtree.Boundable;
import com.revolsys.geometry.index.strtree.StrTree;
import com.revolsys.geometry.index.strtree.StrTreeNode;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.PointDoubleXY;

/**
 * @version 1.7
 */
public class STRtreeDemo {

  public static class TestTree extends StrTree<Object> {
    private static final long serialVersionUID = 1L;

    public TestTree(final int nodeCapacity) {
      super(nodeCapacity);
    }

    @Override
    public List<Boundable<Object>> boundablesAtLevel(final int level) {
      return super.boundablesAtLevel(level);
    }

    @Override
    public StrTreeNode<Object> getRoot() {
      return this.root;
    }

    @Override
    public List<StrTreeNode<Object>> newParentBoundables(
      final List<? extends Boundable<Object>> verticalSlice, final int newLevel) {
      return super.newParentBoundables(verticalSlice, newLevel);
    }

    @Override
    public List<StrTreeNode<Object>> newParentBoundablesFromVerticalSlice(
      final List<? extends Boundable<Object>> childBoundables, final int newLevel) {
      return super.newParentBoundablesFromVerticalSlice(childBoundables, newLevel);
    }

    @Override
    public List<List<Boundable<Object>>> verticalSlices(
      final List<Boundable<Object>> childBoundables, final int size) {
      return super.verticalSlices(childBoundables, size);
    }
  }

  private static final double EXTENT = 100;

  private static GeometryFactory factory = GeometryFactory.DEFAULT_3D;

  private static final int ITEM_COUNT = 20;

  private static final double MAX_ITEM_EXTENT = 15;

  private static final double MIN_ITEM_EXTENT = 3;

  private static final int NODE_CAPACITY = 4;

  private static void initTree(final TestTree t, final List<BoundingBox> sourceEnvelopes) {
    for (final BoundingBox sourceEnvelope : sourceEnvelopes) {
      t.insertItem(sourceEnvelope, sourceEnvelope);
    }
    t.build();
  }

  public static void main(final String[] args) throws Exception {
    final List<BoundingBox> envelopes = sourceData();
    final TestTree t = new TestTree(NODE_CAPACITY);
    initTree(t, envelopes);
    final PrintStream printStream = System.out;
    printSourceData(envelopes, printStream);
    printLevels(t, printStream);
  }

  public static void printBoundables(final List<Boundable<Object>> boundables, final String title,
    final PrintStream out) {
    out.println("============ " + title + " ============\n");
    out.print("GEOMETRYCOLLECTION(");
    boolean first = true;
    for (final Boundable<Object> boundable : boundables) {
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

  public static void printSourceData(final List<BoundingBox> sourceEnvelopes,
    final PrintStream out) {
    out.println("============ Source Data ============\n");
    out.print("GEOMETRYCOLLECTION(");
    boolean first = true;
    for (final BoundingBox e : sourceEnvelopes) {
      final Geometry g = factory.polygon(factory.linearRing(new Point[] {
        new PointDoubleXY(e.getMinX(), e.getMinY()), new PointDoubleXY(e.getMinX(), e.getMaxY()),
        new PointDoubleXY(e.getMaxX(), e.getMaxY()), new PointDoubleXY(e.getMaxX(), e.getMinY()),
        new PointDoubleXY(e.getMinX(), e.getMinY())
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
      new PointDoubleXY(left, bottom), new PointDoubleXY(right, bottom),
      new PointDoubleXY(right, top), new PointDoubleXY(left, top), new PointDoubleXY(left, bottom)
    }));
  }

  private static List<BoundingBox> sourceData() {
    final List<BoundingBox> envelopes = new ArrayList<>();
    for (int i = 0; i < ITEM_COUNT; i++) {
      envelopes.add(randomRectangle().getBoundingBox());
    }
    return envelopes;
  }

  private static String toString(final Boundable<?> b) {
    return "POLYGON((" + b.getMinX() + " " + b.getMinY() + ", " + b.getMinX() + " " + b.getMaxY()
      + ", " + b.getMaxX() + " " + b.getMaxY() + ", " + b.getMaxX() + " " + b.getMinY() + ","
      + b.getMinX() + " " + b.getMinY() + "))";
  }

  public STRtreeDemo() {
  }

}
