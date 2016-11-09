package com.revolsys.geometry.test.old.index.kdtree;

import java.util.List;

import com.revolsys.geometry.index.kdtree.KdNode;
import com.revolsys.geometry.index.kdtree.KdTree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.geometry.model.impl.PointDoubleXY;

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
    final KdTree index = new KdTree(.001);

    final KdNode node1 = index.insert(new PointDoubleXY(1, 1));

    final KdNode node2 = index.insert(new PointDoubleXY(1, 1));

    assertTrue("Inserting 2 identical points should create one node", node1 == node2);

    final BoundingBox queryEnv = new BoundingBoxDoubleXY(0, 0, 10, 10);

    final List result = index.query(queryEnv);
    assertTrue(result.size() == 1);

    final KdNode node = (KdNode)result.get(0);
    assertTrue(node.getCount() == 2);
    assertTrue(node.isRepeated());
  }
}
