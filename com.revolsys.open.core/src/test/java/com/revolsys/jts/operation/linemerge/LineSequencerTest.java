package com.revolsys.jts.operation.linemerge;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKTReader;

/**
 * Test LineSequencer
 *
 * @version 1.7
 */
public class LineSequencerTest extends TestCase {
  private static WKTReader rdr = new WKTReader();

  public static void main(final String[] args) {
    junit.textui.TestRunner.run(LineSequencerTest.class);
  }

  public LineSequencerTest(final String name) {
    super(name);
  }

  List fromWKT(final String[] wkts) {
    final List geomList = new ArrayList();
    for (final String wkt : wkts) {
      try {
        geomList.add(rdr.read(wkt));
      } catch (final Exception ex) {
        ex.printStackTrace();
      }
    }
    return geomList;
  }

  private void runIsSequenced(final String inputWKT, final boolean expected)
    throws ParseException {
    final Geometry g = rdr.read(inputWKT);
    final boolean isSequenced = LineSequencer.isSequenced(g);
    assertTrue(isSequenced == expected);
  }

  private void runLineSequencer(final String[] inputWKT,
    final String expectedWKT) throws ParseException {
    final List inputGeoms = fromWKT(inputWKT);
    final LineSequencer sequencer = new LineSequencer();
    sequencer.add(inputGeoms);

    final boolean isCorrect = false;
    if (!sequencer.isSequenceable()) {
      assertTrue(expectedWKT == null);
    } else {
      final Geometry expected = rdr.read(expectedWKT);
      final Geometry result = sequencer.getSequencedLineStrings();
      final boolean isOK = expected.equalsNorm(result);
      if (!isOK) {
        System.out.println("ERROR - Expected: " + expected);
        System.out.println("          Actual: " + result);
      }
      assertTrue(isOK);

      final boolean isSequenced = LineSequencer.isSequenced(result);
      assertTrue(isSequenced);
    }
  }

  public void test2SimpleLoops() throws Exception {
    final String[] wkt = {
      "LINESTRING ( 0 0, 0 10 )", "LINESTRING ( 0 10, 0 0 )",
      "LINESTRING ( 0 0, 0 20 )", "LINESTRING ( 0 20, 0 0 )",
    };
    final String result = "MULTILINESTRING ((0 10, 0 0), (0 0, 0 20), (0 20, 0 0), (0 0, 0 10))";
    runLineSequencer(wkt, result);
  }

  public void testBadLineSequence() throws Exception {
    final String wkt = "MULTILINESTRING ((0 0, 0 1), (0 2, 0 3), (0 1, 0 4) )";
    runIsSequenced(wkt, false);
  }

  public void testLineSequence() throws Exception {
    final String wkt = "LINESTRING ( 0 0, 0 10 )";
    runIsSequenced(wkt, true);
  }

  public void testLineWithRing() throws Exception {
    final String[] wkt = {
      "LINESTRING ( 0 0, 0 10 )", "LINESTRING ( 0 10, 10 10, 10 20, 0 10 )",
      "LINESTRING ( 0 30, 0 20 )", "LINESTRING ( 0 20, 0 10 )",
    };
    final String result = "MULTILINESTRING ((0 0, 0 10), (0 10, 10 10, 10 20, 0 10), (0 10, 0 20), (0 20, 0 30))";
    runLineSequencer(wkt, result);
  }

  public void testMultipleGraphsWithMultipeRings() throws Exception {
    final String[] wkt = {
      "LINESTRING ( 0 0, 0 10 )", "LINESTRING ( 0 10, 10 10, 10 20, 0 10 )",
      "LINESTRING ( 0 10, 40 40, 40 20, 0 10 )", "LINESTRING ( 0 30, 0 20 )",
      "LINESTRING ( 0 20, 0 10 )", "LINESTRING ( 0 60, 0 50 )",
      "LINESTRING ( 0 40, 0 50 )",
    };
    final String result = "MULTILINESTRING ((0 0, 0 10), (0 10, 40 40, 40 20, 0 10), (0 10, 10 10, 10 20, 0 10), (0 10, 0 20), (0 20, 0 30), (0 40, 0 50), (0 50, 0 60))";
    runLineSequencer(wkt, result);
  }

  public void testMultipleGraphsWithRing() throws Exception {
    final String[] wkt = {
      "LINESTRING ( 0 0, 0 10 )", "LINESTRING ( 0 10, 10 10, 10 20, 0 10 )",
      "LINESTRING ( 0 30, 0 20 )", "LINESTRING ( 0 20, 0 10 )",
      "LINESTRING ( 0 60, 0 50 )", "LINESTRING ( 0 40, 0 50 )",
    };
    final String result = "MULTILINESTRING ((0 0, 0 10), (0 10, 10 10, 10 20, 0 10), (0 10, 0 20), (0 20, 0 30), (0 40, 0 50), (0 50, 0 60))";
    runLineSequencer(wkt, result);
  }

  // isSequenced tests
  // ==========================================================

  public void testSimple() throws Exception {
    final String[] wkt = {
      "LINESTRING ( 0 0, 0 10 )", "LINESTRING ( 0 20, 0 30 )",
      "LINESTRING ( 0 10, 0 20 )"
    };
    final String result = "MULTILINESTRING ((0 0, 0 10), (0 10, 0 20), (0 20, 0 30))";
    runLineSequencer(wkt, result);
  }

  public void testSimpleBigLoop() throws Exception {
    final String[] wkt = {
      "LINESTRING ( 0 0, 0 10 )", "LINESTRING ( 0 20, 0 30 )",
      "LINESTRING ( 0 30, 0 00 )", "LINESTRING ( 0 10, 0 20 )",
    };
    final String result = "MULTILINESTRING ((0 0, 0 10), (0 10, 0 20), (0 20, 0 30), (0 30, 0 0))";
    runLineSequencer(wkt, result);
  }

  public void testSimpleLoop() throws Exception {
    final String[] wkt = {
      "LINESTRING ( 0 0, 0 10 )", "LINESTRING ( 0 10, 0 0 )",
    };
    final String result = "MULTILINESTRING ((0 0, 0 10), (0 10, 0 0))";
    runLineSequencer(wkt, result);
  }

  // ==========================================================

  public void testSimpleLoopWithTail() throws Exception {
    final String[] wkt = {
      "LINESTRING ( 0 0, 0 10 )", "LINESTRING ( 0 10, 10 10 )",
      "LINESTRING ( 10 10, 10 20, 0 10 )",
    };
    final String result = "MULTILINESTRING ((0 0, 0 10), (0 10, 10 10), (10 10, 10 20, 0 10))";
    runLineSequencer(wkt, result);
  }

  public void testSplitLineSequence() throws Exception {
    final String wkt = "MULTILINESTRING ((0 0, 0 1), (0 2, 0 3), (0 3, 0 4) )";
    runIsSequenced(wkt, true);
  }

  public void testWide8WithTail() throws Exception {
    final String[] wkt = {
      "LINESTRING ( 0 0, 0 10 )", "LINESTRING ( 10 0, 10 10 )",
      "LINESTRING ( 0 0, 10 0 )", "LINESTRING ( 0 10, 10 10 )",
      "LINESTRING ( 0 10, 0 20 )", "LINESTRING ( 10 10, 10 20 )",
      "LINESTRING ( 0 20, 10 20 )",

      "LINESTRING ( 10 20, 30 30 )",
    };
    final String result = null;
    runLineSequencer(wkt, result);
  }

}
