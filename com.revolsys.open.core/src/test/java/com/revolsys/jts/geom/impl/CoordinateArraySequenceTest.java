package com.revolsys.jts.geom.impl;

import junit.textui.TestRunner;

import com.revolsys.jts.geom.CoordinateSequenceFactory;

/**
 * Test {@link CoordinateArraySequence}
 *
 * @version 1.7
 */
public class CoordinateArraySequenceTest extends CoordinateSequenceTestBase {
  public static void main(final String args[]) {
    TestRunner.run(CoordinateArraySequenceTest.class);
  }

  public CoordinateArraySequenceTest(final String name) {
    super(name);
  }

  @Override
  CoordinateSequenceFactory getCSFactory() {
    return CoordinateArraySequenceFactory.instance();
  }
}
