package com.revolsys.core.test.geometry.test.old.operation;

import java.io.IOException;
import java.util.List;

import com.revolsys.core.test.geometry.test.old.algorithm.InteriorPointTest;
import com.revolsys.geometry.operation.union.CascadedPolygonUnion;
import com.revolsys.geometry.wkb.ParseException;

import junit.framework.TestCase;

/**
 * Large-scale tests of {@link CascadedPolygonUnion}
 * using data from files.
 *
 * @author mbdavis
 *
 */
public class CascadedPolygonUnionFileTest extends TestCase {
  private static CascadedPolygonUnionTester tester = new CascadedPolygonUnionTester();

  public CascadedPolygonUnionFileTest(final String name) {
    super(name);
  }

  private void runTestResource(final String file, final double minimumMeasure)
    throws IOException, ParseException {
    final List geometries = InteriorPointTest.getTestGeometries(file);
    assertTrue(tester.test(geometries, minimumMeasure));
  }

  public void testAfrica2() throws Exception {
    runTestResource("africa.wkt", CascadedPolygonUnionTester.MIN_SIMILARITY_MEAURE);
  }

  public void XtestEurope() throws Exception {
    runTestResource("europe.wkt", CascadedPolygonUnionTester.MIN_SIMILARITY_MEAURE);
  }

}
