package com.revolsys.core.test.geometry.test.old.index;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.index.kdtree.KdNode;
import com.revolsys.geometry.index.kdtree.KdTree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.geometry.model.impl.PointDoubleXY;

public class KdTreeTest {

  @Test
  public void testSinglePoint() {
    final KdTree index = new KdTree();

    final KdNode node1 = index.insertPoint(1, 1);

    final KdNode node2 = index.insertPoint(new PointDoubleXY(1, 1));

    Assert.assertSame("Inserting 2 identical points should create one node", node1, node2);

    final BoundingBox queryEnv = new BoundingBoxDoubleXY(0, 0, 10, 10);

    final List<KdNode> result = index.getItems(queryEnv);
    Assert.assertEquals(1, result.size());

    final KdNode node = result.get(0);
    Assert.assertEquals(2, node.getCount());
    Assert.assertTrue(node.isRepeated());
  }
}
