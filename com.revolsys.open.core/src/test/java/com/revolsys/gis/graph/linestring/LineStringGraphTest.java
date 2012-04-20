package com.revolsys.gis.graph.linestring;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.revolsys.gis.cs.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class LineStringGraphTest {
  GeometryFactory geometryFactory = GeometryFactory.getFactory(3005, 1000.0);

  public void checkLines(
    final List<LineString> actualLines,
    final String... expectedLines) {
    Assert.assertEquals("Number of lines", expectedLines.length,
      actualLines.size());
    for (int i = 0; i < expectedLines.length; i++) {
      final LineString expectedLine = geometryFactory.createGeometry(expectedLines[i]);
      final LineString actualLine = actualLines.get(i);
      Assert.assertEquals("Line not equal", expectedLine, actualLine);
    }
  }

  @Test
  public void testEndOfSegmentOverlap() {
    final LineString line = geometryFactory.createGeometry("LINESTRING(844000 1343000,844010 1343000,844020 1343000,844010 1343000)");
    final LineStringGraph graph = new LineStringGraph(line);
    final List<LineString> lines = graph.getLines();
    checkLines(lines,
      "LINESTRING(844000 1343000,844010 1343000,844020 1343000)");
  }

  @Test
  public void testLoopSegmentOverlap() {
    final LineString line = geometryFactory.createGeometry("LINESTRING(844000 1343000,844010 1343000,844020 1343000,844020 1343010,844010 1343010,844010 1343000,844020 1343000,844030 1343000)");
    final LineStringGraph graph = new LineStringGraph(line);
    final List<LineString> lines = graph.getLines();

    checkLines(
      lines,
      "LINESTRING(844000 1343000,844010 1343000)",
      "LINESTRING(844010 1343000,844020 1343000)",
      "LINESTRING(844020 1343000,844020 1343010,844010 1343010,844010 1343000)",
      "LINESTRING(844020 1343000,844030 1343000)");

  }

  @Test
  public void testMidSegmentOverlap() {
    final LineString line = geometryFactory.createGeometry("LINESTRING(844000 1343000,844010 1343000,844020 1343000,844010 1343000,844020 1343000,844030 1343000)");
    final LineStringGraph graph = new LineStringGraph(line);
    final List<LineString> lines = graph.getLines();
    checkLines(lines,
      "LINESTRING(844000 1343000,844010 1343000,844020 1343000,844030 1343000)");

  }

  @Test
  public void testOverlapSpike() {
    final LineString line = geometryFactory.createGeometry("LINESTRING(844000 1343000,844010 1343000,844010 1343010,844010 1343000,844100 1343000)");
    final LineStringGraph graph = new LineStringGraph(line);
    final List<LineString> lines = graph.getLines();
    checkLines(lines, "LINESTRING(844000 1343000,844010 1343000)",
      "LINESTRING(844010 1343000,844010 1343010)",
      "LINESTRING(844010 1343000,844100 1343000)");
  }

  /**
   * +---+ | | ===S---+
   */
  @Test
  public void testPOverlapNotStart() {
    final LineString line = geometryFactory.createGeometry("LINESTRING(844100 1343000,844000 1343000,844100 1343000,844200 1343000,844200 1343100,844100 1343100,844100 1343000)");
    final LineStringGraph lineGraph = new LineStringGraph(line);
    lineGraph.splitEdgesCloseToNodes();
    lineGraph.splitCrossingEdges();
    final List<LineString> lines = lineGraph.getLines();
    checkLines(
      lines,
      "LINESTRING(844100 1343000,844000 1343000)",
      "LINESTRING(844100 1343000,844200 1343000,844200 1343100,844100 1343100,844100 1343000)");
  }

  @Test
  public void testSplitCrossingEdgesMiddle() {
    final LineString line = geometryFactory.createGeometry("LINESTRING(844000 1343000,844100 1343100,844000 1343100,844100 1343000)");
    final LineStringGraph graph = new LineStringGraph(line);
    graph.splitCrossingEdges();
    final List<LineString> lines = graph.getLines();
    checkLines(
      lines,
      "LINESTRING(844000 1343000,844050 1343050)",
      "LINESTRING(844050 1343050,844100 1343100,844000 1343100,844050 1343050)",
      "LINESTRING(844050 1343050,844100 1343000)");
  }

  @Test
  public void testSplitEdgesCloseToNodes() {
    final LineString line = geometryFactory.createGeometry("LINESTRING(844000 1343000.001,844100 1343000,844100 1343010,844050 1343000.001)");
    final LineStringGraph graph = new LineStringGraph(line);
    graph.splitEdgesCloseToNodes();
    final List<LineString> lines = graph.getLines();
    checkLines(
      lines,
      "LINESTRING(844000 1343000.001,844050 1343000.001)",
      "LINESTRING(844050 1343000.001,844100 1343000,844100 1343010,844050 1343000.001)");
  }

  @Test
  public void testStartOverlapWithPShapeAtEnd() {
    final LineString line = geometryFactory.createGeometry("LINESTRING(844000 1343000,844100 1343000,844200 1343000,844200 1343100,844100 1343100,844100 1343000)");
    final LineStringGraph graph = new LineStringGraph(line);
    graph.splitEdgesCloseToNodes();
    final List<LineString> lines = graph.getLines();
    checkLines(
      lines,
      "LINESTRING(844000 1343000,844100 1343000)",
      "LINESTRING(844100 1343000,844200 1343000,844200 1343100,844100 1343100,844100 1343000)");
  }

  @Test
  public void testFigure8WithOverlap() {
    final LineString line = geometryFactory.createGeometry("LINESTRING(844000 1343000,844100 1343000,844200 1343000,844300 1343000,844300 1343100,844200 1343100,844200 1343000,844100 1343000,844100 1343100,844000 1343100,844000 1343000)");
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

  @Test
  public void testWholeSegmentOverlap() {
    final LineString line = geometryFactory.createGeometry("LINESTRING(844000 1343000,844100 1343000,844000 1343000)");
    final LineStringGraph graph = new LineStringGraph(line);
    final List<LineString> lines = graph.getLines();
    checkLines(lines, "LINESTRING(844000 1343000,844100 1343000)");
  }
}
