package com.revolsys.jts.geom.impl;

import junit.textui.TestRunner;

import com.revolsys.jts.geom.CoordinateSequenceFactory;

/**
 * Test {@link PackedCoordinateSequence}
 * @version 1.7
 */
public class PackedCoordinateSequenceTest extends CoordinateSequenceTestBase {
  public static void main(final String args[]) {
    TestRunner.run(PackedCoordinateSequenceTest.class);
  }

  public PackedCoordinateSequenceTest(final String name) {
    super(name);
  }

  @Override
  CoordinateSequenceFactory getCSFactory() {
    return new PackedCoordinateSequenceFactory();
  }

}
