package com.revolsys.core.test.geometry.test.old.index.kdtree;

import java.util.List;

import com.revolsys.geometry.index.kdtree.KdNode;
import com.revolsys.geometry.index.kdtree.KdTree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.geometry.model.impl.PointDouble;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class KdTreeTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(KdTreeTest.class);
  }

  public KdTreeTest(final String name) {
    super(name);
  }

  public void testSinglePoint() {
    final KdTree index = new KdTree(GeometryFactory.fixed2d(0, 1000, 1000));

    final KdNode node1 = index.insertPoint(new PointDouble(1, 1));

    final KdNode node2 = index.insertPoint(new PointDouble(1, 1));

    assertTrue("Inserting 2 identical points should create one node", node1 == node2);

    final BoundingBox queryEnv = new BoundingBoxDoubleXY(0, 0, 10, 10);

    final List result = index.getItems(queryEnv);
    assertTrue(result.size() == 1);

    final KdNode node = (KdNode)result.get(0);
    assertTrue(node.getCount() == 2);
    assertTrue(node.isRepeated());
  }
}
