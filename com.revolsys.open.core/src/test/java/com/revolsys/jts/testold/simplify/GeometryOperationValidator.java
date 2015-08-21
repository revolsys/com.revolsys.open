package com.revolsys.jts.testold.simplify;

import com.revolsys.geometry.io.WKTReader;
import com.revolsys.geometry.model.Geometry;

import junit.framework.Assert;

/**
 * Runs various validation tests on a the results of a geometry operation
 */
public class GeometryOperationValidator {
  private static WKTReader rdr = new WKTReader();

  private final Geometry[] ioGeometry;

  private boolean expectedSameStructure = false;

  private String wktExpected = null;

  public GeometryOperationValidator(final Geometry[] ioGeometry) {
    this.ioGeometry = ioGeometry;
  }

  public boolean isAllTestsPassed() {
    try {
      test();
    } catch (final Throwable e) {
      return false;
    }
    return true;
  }

  public GeometryOperationValidator setExpectedResult(final String wktExpected) {
    this.wktExpected = wktExpected;
    return this;
  }

  public GeometryOperationValidator setExpectedSameStructure() {
    this.expectedSameStructure = true;
    return this;
  }

  /**
   * Tests if the result is valid.
   * Throws an exception if result is not valid.
   * This allows chaining multiple tests together.
   *
   * @throws Exception if the result is not valid.
   */
  public void test() throws Exception {
    testSameStructure();
    testValid();
    testExpectedResult();
  }

  public GeometryOperationValidator testEmpty(final boolean isEmpty) throws Exception {
    final String failureCondition = isEmpty ? "not empty" : "empty";
    Assert.assertTrue("simplified geometry is " + failureCondition,
      this.ioGeometry[1].isEmpty() == isEmpty);
    return this;
  }

  private void testExpectedResult() throws Exception {
    if (this.wktExpected == null) {
      return;
    }
    final Geometry expectedGeom = rdr.read(this.wktExpected);
    Assert.assertTrue("Expected result not found", expectedGeom.equals(2, this.ioGeometry[1]));

  }

  public GeometryOperationValidator testSameStructure() throws Exception {
    if (!this.expectedSameStructure) {
      return this;
    }
    Assert.assertTrue("simplified geometry has different structure than input",
      SameStructureTester.isSameStructure(this.ioGeometry[0], this.ioGeometry[1]));
    return this;
  }

  public GeometryOperationValidator testValid() throws Exception {
    Assert.assertTrue("simplified geometry is not valid", this.ioGeometry[1].isValid());
    return this;
  }
}
