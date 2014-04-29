package com.revolsys.jts.testold.geom;

import junit.textui.TestRunner;

import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesListFactory;
import com.revolsys.jts.geom.CoordinateSequenceFactory;

/**
 * Test {@link DoubleCoordinatesList}
 *
 * @version 1.7
 */
public class DoubleCoordinatesListTest extends CoordinateSequenceTestBase {
  public static void main(final String args[]) {
    TestRunner.run(DoubleCoordinatesListTest.class);
  }

  public DoubleCoordinatesListTest(final String name) {
    super(name);
  }

  @Override
  CoordinateSequenceFactory getCSFactory() {
    return new DoubleCoordinatesListFactory();
  }
}
