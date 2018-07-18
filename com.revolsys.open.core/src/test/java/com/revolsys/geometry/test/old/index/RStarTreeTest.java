package com.revolsys.geometry.test.old.index;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.index.rstartree.RStarTree;

public class RStarTreeTest {

  @Test
  public void testSpatialIndex() throws Exception {
    final SpatialIndexTester tester = new SpatialIndexTester();
    tester.setSpatialIndex(new RStarTree<>());
    tester.init();
    tester.run();
    Assert.assertTrue(tester.isSuccess());
  }

}
