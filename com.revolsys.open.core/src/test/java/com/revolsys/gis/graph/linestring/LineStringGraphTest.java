package com.revolsys.gis.graph.linestring;

import java.util.List;

import junit.framework.Assert;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.jts.geom.LineString;

public class LineStringGraphTest {
  GeometryFactory geometryFactory = GeometryFactory.getFactory(3005, 2, 1000.0,
    1);

  public void checkLines(final List<LineString> actualLines,
    final String... expectedLines) {
    Assert.assertEquals("Number of lines", expectedLines.length,
      actualLines.size());
    for (int i = 0; i < expectedLines.length; i++) {
      final LineString expectedLine = this.geometryFactory.createGeometry(expectedLines[i]);
      final LineString actualLine = actualLines.get(i);
      Assert.assertEquals("Line not equal", expectedLine, actualLine);
    }
  }

  public void testCleanupEndOfSegmentOverlap() {
    final LineString line = this.geometryFactory.createGeometry("LINESTRING(844000 1343000,844010 1343000,844020 1343000,844010 1343000)");
    final LineStringGraph graph = new LineStringGraph(line);
    final List<LineString> lines = graph.getLines();
    checkLines(lines,
      "LINESTRING(844000 1343000,844010 1343000,844020 1343000)");
  }

  public void testCleanupFigure8WithOverlap() {
    final LineString line = this.geometryFactory.createGeometry("LINESTRING(844000 1343000,844100 1343000,844200 1343000,844300 1343000,844300 1343100,844200 1343100,844200 1343000,844100 1343000,844100 1343100,844000 1343100,844000 1343000)");
    final LineStringGraph graph = new LineStringGraph(line);
    graph.splitEdgesCloseToNodes();
    final List<LineString> lines = graph.getLines();
    checkLines(
      lines,
      "LINESTRING(844000 1343000,844100 1343000)",
      "LINESTRING(844100 1343000,844200 1343000)",
      "LINESTRING(844200 1343000,844300 1343000,844300 1343100,844200 1343100,844200 1343000)",
      "LINESTRING(844100 1343000,844100 1343100,844000 1343100,844000 1343000)");
  }

  public void testCleanupLoopSegmentOverlap() {
    final LineString line = this.geometryFactory.createGeometry("LINESTRING(844000 1343000,844010 1343000,844020 1343000,844020 1343010,844010 1343010,844010 1343000,844020 1343000,844030 1343000)");
    final LineStringGraph graph = new LineStringGraph(line);
    final List<LineString> lines = graph.getLines();

    checkLines(
      lines,
      "LINESTRING(844000 1343000,844010 1343000)",
      "LINESTRING(844010 1343000,844020 1343000)",
      "LINESTRING(844020 1343000,844020 1343010,844010 1343010,844010 1343000)",
      "LINESTRING(844020 1343000,844030 1343000)");

  }

  public void testCleanupMidSegmentOverlap() {
    final LineString line = this.geometryFactory.createGeometry("LINESTRING(844000 1343000,844010 1343000,844020 1343000,844010 1343000,844020 1343000,844030 1343000)");
    final LineStringGraph graph = new LineStringGraph(line);
    final List<LineString> lines = graph.getLines();
    checkLines(lines,
      "LINESTRING(844000 1343000,844010 1343000,844020 1343000,844030 1343000)");

  }

  public void testCleanupOverlapSpike() {
    final LineString line = this.geometryFactory.createGeometry("LINESTRING(844000 1343000,844010 1343000,844010 1343010,844010 1343000,844100 1343000)");
    final LineStringGraph graph = new LineStringGraph(line);
    final List<LineString> lines = graph.getLines();
    checkLines(lines, "LINESTRING(844000 1343000,844010 1343000)",
      "LINESTRING(844010 1343000,844010 1343010)",
      "LINESTRING(844010 1343000,844100 1343000)");
  }

  /**
   * +---+ | | ===S---+
   */

  public void testCleanupPOverlapNotStart() {
    final LineString line = this.geometryFactory.createGeometry("LINESTRING(844100 1343000,844000 1343000,844100 1343000,844200 1343000,844200 1343100,844100 1343100,844100 1343000)");
    final LineStringGraph lineGraph = new LineStringGraph(line);
    lineGraph.splitEdgesCloseToNodes();
    lineGraph.splitCrossingEdges();
    final List<LineString> lines = lineGraph.getLines();
    checkLines(
      lines,
      "LINESTRING(844100 1343000,844000 1343000)",
      "LINESTRING(844100 1343000,844200 1343000,844200 1343100,844100 1343100,844100 1343000)");
  }

  public void testCleanupSplitCrossingEdgesMiddle() {
    final LineString line = this.geometryFactory.createGeometry("LINESTRING(844000 1343000,844100 1343100,844000 1343100,844100 1343000)");
    final LineStringGraph graph = new LineStringGraph(line);
    graph.splitCrossingEdges();
    final List<LineString> lines = graph.getLines();
    checkLines(
      lines,
      "LINESTRING(844000 1343000,844050 1343050)",
      "LINESTRING(844050 1343050,844100 1343100,844000 1343100,844050 1343050)",
      "LINESTRING(844050 1343050,844100 1343000)");
  }

  public void testCleanupSplitEdgesCloseToNodes() {
    final LineString line = this.geometryFactory.createGeometry("LINESTRING(844000 1343000.001,844100 1343000,844100 1343010,844050 1343000.001)");
    final LineStringGraph graph = new LineStringGraph(line);
    graph.splitEdgesCloseToNodes();
    final List<LineString> lines = graph.getLines();
    checkLines(
      lines,
      "LINESTRING(844000 1343000.001,844050 1343000.001)",
      "LINESTRING(844050 1343000.001,844100 1343000,844100 1343010,844050 1343000.001)");
  }

  public void testCleanupStartOverlapWithPShapeAtEnd() {
    final LineString line = this.geometryFactory.createGeometry("LINESTRING(844000 1343000,844100 1343000,844200 1343000,844200 1343100,844100 1343100,844100 1343000)");
    final LineStringGraph graph = new LineStringGraph(line);
    graph.splitEdgesCloseToNodes();
    final List<LineString> lines = graph.getLines();
    checkLines(
      lines,
      "LINESTRING(844000 1343000,844100 1343000)",
      "LINESTRING(844100 1343000,844200 1343000,844200 1343100,844100 1343100,844100 1343000)");
  }

  public void testCleanupWholeSegmentOverlap() {
    final LineString line = this.geometryFactory.createGeometry("LINESTRING(844000 1343000,844100 1343000,844000 1343000)");
    final LineStringGraph graph = new LineStringGraph(line);
    final List<LineString> lines = graph.getLines();
    checkLines(lines, "LINESTRING(844000 1343000,844100 1343000)");
  }

  public void testIntersectionFalseEndEnd() {
    final LineString line1 = this.geometryFactory.createGeometry("LINESTRING(844000 1343000,844100 1343000,844200 1343000)");
    final LineStringGraph graph = new LineStringGraph(line1);
    final LineString line2 = this.geometryFactory.createGeometry("LINESTRING(844400 1343000,844300 1343000,844200 1343000)");

    final boolean intersects = graph.intersects(line2);
    Assert.assertEquals("Intersects incorrect", false, intersects);
  }

  public void testIntersectionFalseEndStart() {
    final LineString line1 = this.geometryFactory.createGeometry("LINESTRING(844000 1343000,844100 1343000,844200 1343000)");
    final LineStringGraph graph = new LineStringGraph(line1);
    final LineString line2 = this.geometryFactory.createGeometry("LINESTRING(844200 1343000,844300 1343000,844400 1343000)");

    final boolean intersects = graph.intersects(line2);
    Assert.assertEquals("Intersects incorrect", false, intersects);
  }

  public void testIntersectionFalseLoopEndLoop() {
    final LineString line1 = this.geometryFactory.createGeometry("LINESTRING(1190705.094 390263.56,1190787.013 390248.01,1190811.675 390258.037,1190810.052 390224.994,1190801.125 390198.639,1190799.709 390184.59,1190796.247 390171.454,1190786.65 390161.059,1190772.755 390157.479,1190737.214 390156.946)");
    final LineStringGraph graph = new LineStringGraph(line1);
    final LineString line2 = this.geometryFactory.createGeometry("LINESTRING(1190737.214 390156.946,1190735.656 390156.923,1190734.175 390153.953,1190737.214 390156.946)");

    final boolean intersects = graph.intersects(line2);
    Assert.assertEquals("Intersects incorrect", false, intersects);
  }

  public void testIntersectionFalseStartEnd() {
    final LineString line1 = this.geometryFactory.createGeometry("LINESTRING(844200 1343000,844300 1343000,844400 1343000)");
    final LineStringGraph graph = new LineStringGraph(line1);
    final LineString line2 = this.geometryFactory.createGeometry("LINESTRING(844000 1343000,844100 1343000,844200 1343000)");

    final boolean intersects = graph.intersects(line2);
    Assert.assertEquals("Intersects incorrect", false, intersects);
  }

  //
  //
  // public void testIntersectionTrueCloseMiddleMiddle() {
  // final LineString line1 =
  // geometryFactory.createGeometry("LINESTRING(800000 1000000.001,800010 1000000.0005,800020 1000000)");
  // final GeometryGraph graph = new GeometryGraph(line1);
  // final LineString line2 =
  // geometryFactory.createGeometry("LINESTRING(800000 1000000,800010 1000000,800020 1000000)");
  //
  // final boolean intersects = graph.intersects(line2);
  // Assert.assertEquals("Intersects incorrect", true, intersects);
  // }

  public void testIntersectionFalseStartEndEndStart() {
    final LineString line1 = this.geometryFactory.createGeometry("LINESTRING(800000 1000000,800010 1000000,800010 1000010)");
    final LineStringGraph graph = new LineStringGraph(line1);
    final LineString line2 = this.geometryFactory.createGeometry("LINESTRING(800010 1000010,800000 1000010,800000 1000000)");

    final boolean intersects = graph.intersects(line2);
    Assert.assertEquals("Intersects incorrect", false, intersects);
  }

  public void testIntersectionFalseStartLoop() {
    final LineString line1 = this.geometryFactory.createGeometry("LINESTRING(800000 1000010,800010 1000010,800010 1000020,800000 1000020,800000 1000010)");
    final LineStringGraph graph = new LineStringGraph(line1);
    final LineString line2 = this.geometryFactory.createGeometry("LINESTRING(800000 1000010,800000 1000000)");

    final boolean intersects = graph.intersects(line2);
    Assert.assertEquals("Intersects incorrect", false, intersects);
  }

  public void testIntersectionFalseStartLoop1() {
    final LineString line1 = this.geometryFactory.createGeometry("LINESTRING(1189556.287 388686.558,1189562.632 388678.83,1189600.858 388676.435,1189628.151 388671.584,1189641.422 388666.145,1189652.815 388657.629,1189661.246 388647.99,1189673.352 388622.518,1189682.078 388581.915,1189682.664 388567.95,1189676.365 388550.699,1189664.681 388542.216,1189651.787 388538.678,1189609.68 388537.912,1189596.534 388540.359,1189585.183 388547.877,1189574.749 388557.432,1189566.276 388568.068,1189554.171 388593.541,1189552.039 388620.431,1189556.918 388647.614,1189556.58 388679.576,1189556.287 388686.558)");
    final LineStringGraph graph = new LineStringGraph(line1);
    final LineString line2 = this.geometryFactory.createGeometry("LINESTRING(1189556.287 388686.558,1189555.995 388693.54,1189555.182 388694.284)");

    final boolean intersects = graph.intersects(line2);
    Assert.assertEquals("Intersects incorrect", false, intersects);
  }

  public void testIntersectionFalseStartStart() {
    final LineString line1 = this.geometryFactory.createGeometry("LINESTRING(844000 1343000,844100 1343000,844200 1343000)");
    final LineStringGraph graph = new LineStringGraph(line1);
    final LineString line2 = this.geometryFactory.createGeometry("LINESTRING(844000 1343000,843900 1343000,843800 1343000)");

    final boolean intersects = graph.intersects(line2);
    Assert.assertEquals("Intersects incorrect", false, intersects);
  }

  public void testIntersectionTrueEndMiddle() {
    final LineString line1 = this.geometryFactory.createGeometry("LINESTRING(800020 1000010,800010 1000010,800000 1000010)");
    final LineStringGraph graph = new LineStringGraph(line1);
    final LineString line2 = this.geometryFactory.createGeometry("LINESTRING(800000 1000000,800000 1000010,800000 1000020)");

    final boolean intersects = graph.intersects(line2);
    Assert.assertEquals("Intersects incorrect", true, intersects);
  }

  public void testIntersectionTrueMiddleEnd() {
    final LineString line1 = this.geometryFactory.createGeometry("LINESTRING(800000 1000000,800000 1000010,800000 1000020)");
    final LineStringGraph graph = new LineStringGraph(line1);
    final LineString line2 = this.geometryFactory.createGeometry("LINESTRING(800020 1000010,800010 1000010,800000 1000010)");

    final boolean intersects = graph.intersects(line2);
    Assert.assertEquals("Intersects incorrect", true, intersects);
  }

  public void testIntersectionTrueMiddleMiddle() {
    final LineString line1 = this.geometryFactory.createGeometry("LINESTRING(800000 1000020,800010 1000010,800020 1000020)");
    final LineStringGraph graph = new LineStringGraph(line1);
    final LineString line2 = this.geometryFactory.createGeometry("LINESTRING(800000 1000000,800010 1000010,800020 1000000)");

    final boolean intersects = graph.intersects(line2);
    Assert.assertEquals("Intersects incorrect", true, intersects);
  }

  public void testIntersectionTrueMiddleStart() {
    final LineString line1 = this.geometryFactory.createGeometry("LINESTRING(800000 1000000,800000 1000010,800000 1000020)");
    final LineStringGraph graph = new LineStringGraph(line1);
    final LineString line2 = this.geometryFactory.createGeometry("LINESTRING(800000 1000010,800010 1000010,800020 1000010)");

    final boolean intersects = graph.intersects(line2);
    Assert.assertEquals("Intersects incorrect", true, intersects);
  }

  public void testIntersectionTrueStartMiddle() {
    final LineString line1 = this.geometryFactory.createGeometry("LINESTRING(800000 1000010,800010 1000010,800020 1000010)");
    final LineStringGraph graph = new LineStringGraph(line1);
    final LineString line2 = this.geometryFactory.createGeometry("LINESTRING(800000 1000000,800000 1000010,800000 1000020)");

    final boolean intersects = graph.intersects(line2);
    Assert.assertEquals("Intersects incorrect", true, intersects);
  }

}
