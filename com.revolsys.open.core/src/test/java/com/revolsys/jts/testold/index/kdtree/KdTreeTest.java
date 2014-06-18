package com.revolsys.jts.testold.index.kdtree;

import java.util.List;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.index.kdtree.KdNode;
import com.revolsys.jts.index.kdtree.KdTree;

public class KdTreeTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(KdTreeTest.class);
  }

  public KdTreeTest(final String name) {
    super(name);
  }

  public void testSinglePoint() {
    final KdTree index = new KdTree(.001);

    final KdNode node1 = index.insert(new PointDouble((double)1, 1, Point.NULL_ORDINATE));

    final KdNode node2 = index.insert(new PointDouble((double)1, 1, Point.NULL_ORDINATE));

    assertTrue("Inserting 2 identical points should create one node",
      node1 == node2);

    final BoundingBox queryEnv = new BoundingBoxDoubleGf(2, 0, 0, 10, 10);

    final List result = index.query(queryEnv);
    assertTrue(result.size() == 1);

    final KdNode node = (KdNode)result.get(0);
    assertTrue(node.getCount() == 2);
    assertTrue(node.isRepeated());
  }
}
