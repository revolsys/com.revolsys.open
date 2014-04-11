package com.revolsys.jts.testold.operation.union;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

import junit.framework.TestCase;

import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.operation.union.CascadedPolygonUnion;
import com.revolsys.jts.testold.junit.GeometryUtils;

/**
 * Large-scale tests of {@link CascadedPolygonUnion}
 * using data from files.
 * 
 * @author mbdavis
 *
 */
public class CascadedPolygonUnionFileTest extends TestCase {
  private static CascadedPolygonUnionTester tester = new CascadedPolygonUnionTester();

  public static void main(final String[] args) {
    junit.textui.TestRunner.run(CascadedPolygonUnionFileTest.class);
  }

  public CascadedPolygonUnionFileTest(final String name) {
    super(name);
  }

  private void runTestResource(final String resource,
    final double minimumMeasure) throws IOException, ParseException {
    final InputStream is = this.getClass().getResourceAsStream(resource);
    // don't bother if file is missing
    if (is == null) {
      return;
    }
    final Collection geoms = GeometryUtils.readWKTFile(new InputStreamReader(is));
    assertTrue(tester.test(geoms, minimumMeasure));
  }

  public void testAfrica2() throws Exception {
    runTestResource("../../../../../data/africa.wkt",
      CascadedPolygonUnionTester.MIN_SIMILARITY_MEAURE);
  }

  public void XtestEurope() throws Exception {
    runTestResource("../../../../../data/europe.wkt",
      CascadedPolygonUnionTester.MIN_SIMILARITY_MEAURE);
  }

}
