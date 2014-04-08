package com.revolsys.jts.index.kdtree;

import java.util.List;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Envelope;

public class KdTreeTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(KdTreeTest.class);
  }

  public KdTreeTest(final String name) {
    super(name);
  }

  public void testSinglePoint() {
    final KdTree index = new KdTree(.001);

    final KdNode node1 = index.insert(new Coordinate(1, 1));

    final KdNode node2 = index.insert(new Coordinate(1, 1));

    assertTrue("Inserting 2 identical points should create one node",
      node1 == node2);

    final Envelope queryEnv = new Envelope(0, 10, 0, 10);

    final List result = index.query(queryEnv);
    assertTrue(result.size() == 1);

    final KdNode node = (KdNode)result.get(0);
    assertTrue(node.getCount() == 2);
    assertTrue(node.isRepeated());
  }
}
