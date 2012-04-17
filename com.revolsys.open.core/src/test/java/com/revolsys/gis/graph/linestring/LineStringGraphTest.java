package com.revolsys.gis.graph.linestring;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.revolsys.gis.cs.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class LineStringGraphTest {
  GeometryFactory geometryFactory = GeometryFactory.getFactory(3005, 1000.0);

  @Test
  public void testEndOfSegmentOverlap() {
    final LineString line = geometryFactory.createGeometry("LINESTRING(844000 1343000,844010 1343000,844020 1343000,844010 1343000)");
    final LineStringGraph graph = new LineStringGraph(line);
    graph.removeDuplicateEdges();
    final LineString actualLine = graph.getLine();
    final LineString expectedLine = geometryFactory.createGeometry("LINESTRING(844000 1343000,844010 1343000,844020 1343000)");
    Assert.assertEquals("Could not remove duplicates", expectedLine, actualLine);
  }

  @Test
  public void testLoopSegmentOverlap() {
    final LineString line = geometryFactory.createGeometry("LINESTRING(844000 1343000,844010 1343000,844020 1343000,844020 1343010,844010 1343010,844010 1343000,844020 1343000,844030 1343000)");
    final LineStringGraph graph = new LineStringGraph(line);
    graph.removeDuplicateEdges();
    final List<LineString> loops = graph.removeLoops();
    Assert.assertEquals("Number of loops", 1, loops.size());
    final LineString actualLoop = loops.get(0);
    final LineString expectedLoop = geometryFactory.createGeometry("LINESTRING (844020 1343000, 844020 1343010, 844010 1343010, 844010 1343000)");
    Assert.assertEquals("Could not remove duplicates", expectedLoop, actualLoop);

    final LineString actualLine = graph.getLine();
    final LineString expectedLine = geometryFactory.createGeometry("LINESTRING(844000 1343000,844010 1343000,844020 1343000,844030 1343000)");
    Assert.assertEquals("Could not remove duplicates", expectedLine, actualLine);
  }

  @Test
  public void testMidSegmentOverlap() {
    final LineString line = geometryFactory.createGeometry("LINESTRING(844000 1343000,844010 1343000,844020 1343000,844010 1343000,844020 1343000,844030 1343000)");
    final LineStringGraph graph = new LineStringGraph(line);
    graph.removeDuplicateEdges();
    final LineString actualLine = graph.getLine();
    final LineString expectedLine = geometryFactory.createGeometry("LINESTRING(844000 1343000,844010 1343000,844020 1343000,844030 1343000)");
    Assert.assertEquals("Could not remove duplicates", expectedLine, actualLine);
  }

  @Test
  public void testOverlapSpike() {
    final LineString line = geometryFactory.createGeometry("LINESTRING(844000 1343000,844010 1343000,844010 1343010,844010 1343000,844100 1343000)");
    final LineStringGraph graph = new LineStringGraph(line);
    graph.removeDuplicateEdges();
    final LineString actualLine = graph.getLine();
    final LineString expectedLine = geometryFactory.createGeometry("LINESTRING(844000 1343000,844010 1343000,844100 1343000)");
    Assert.assertEquals("Could not remove duplicates", expectedLine, actualLine);
  }

  @Test
  public void testSplitCrossingEdgesMiddle() {
    final LineString line = geometryFactory.createGeometry("LINESTRING(844000 1343000,844100 1343100,844000 1343100,844100 1343000)");
    final LineStringGraph graph = new LineStringGraph(line);
    graph.splitCrossingEdges();
    final LineString actualLine = graph.getLine();
    final LineString expectedLine = geometryFactory.createGeometry("LINESTRING(844000 1343000,844050 1343050,844100 1343100,844000 1343100,844050 1343050,844100 1343000)");
    Assert.assertEquals("Could not split crossing egdes", expectedLine,
      actualLine);
  }

  @Test
  public void testSplitEdgesCloseToNodes() {
    final LineString line = geometryFactory.createGeometry("LINESTRING(844000 1343000.001,844100 1343000,844100 1343010,844050 1343000.001)");
    final LineStringGraph graph = new LineStringGraph(line);
    graph.splitEdgesCloseToNodes();
    final LineString actualLine = graph.getLine();
    final LineString expectedLine = geometryFactory.createGeometry("LINESTRING(844000 1343000.001,844050 1343000.001,844100 1343000,844100 1343010,844050 1343000.001)");
    Assert.assertEquals("Could not split close nodes", expectedLine, actualLine);
  }

  @Test
  public void testWholeSegmentOverlap() {
    final LineString line = geometryFactory.createGeometry("LINESTRING(844000 1343000,844100 1343000,844000 1343000)");
    final LineStringGraph graph = new LineStringGraph(line);
    graph.removeDuplicateEdges();
    final LineString actualLine = graph.getLine();
    final LineString expectedLine = geometryFactory.createGeometry("LINESTRING(844000 1343000,844100 1343000)");
    Assert.assertEquals("Could not remove duplicates", expectedLine, actualLine);
  }
}
