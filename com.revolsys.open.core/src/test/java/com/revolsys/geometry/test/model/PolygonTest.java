package com.revolsys.geometry.test.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.vertex.Vertex;

public class PolygonTest {

  private static final List<Point> EXTERIOR_1 = Arrays.<Point> asList(new PointDoubleXY(0.0, 0.0),
    new PointDoubleXY(10.0, 0.0), new PointDoubleXY(10.0, 10.0), new PointDoubleXY(0.0, 10.0),
    new PointDoubleXY(0.0, 0));

  private static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory
    .floating2d(EpsgCoordinateSystems.WGS84_ID);

  private static final List<Point> INTERIOR_2 = Arrays.<Point> asList(new PointDoubleXY(2.0, 2.0),
    new PointDoubleXY(8.0, 2.0), new PointDoubleXY(8.0, 8.0), new PointDoubleXY(2.0, 8.0),
    new PointDoubleXY(2.0, 2.0));

  private static final Polygon WITH_HOLE = GEOMETRY_FACTORY
    .polygon(GEOMETRY_FACTORY.linearRing(EXTERIOR_1), GEOMETRY_FACTORY.linearRing(INTERIOR_2));

  @Test
  public void testFromFile() {
    TestUtil.doTestGeometry(getClass(), "Polygon.csv");
  }

  @Test
  public void testVertices() {

    final List<Point> allCoordinates = new ArrayList<>();
    allCoordinates.addAll(EXTERIOR_1);
    allCoordinates.addAll(INTERIOR_2);
    final Polygon polygon = WITH_HOLE;
    int i = 0;
    for (final Vertex vertex : polygon.vertices()) {
      final Point point = allCoordinates.get(i);
      Assert.assertEquals(point, vertex);
      i++;
    }
    Assert.assertEquals(new PointDoubleXY(0.0, 0.0), polygon.getVertex(0, 0));
    Assert.assertNull("VertexIndex out of range", polygon.getVertex(0, 6));
    Assert.assertNull("VertexIndex out of range", polygon.getVertex(1, 6));

    Assert.assertNull("RingIndex Negative", polygon.getVertex(-1, 0));
    Assert.assertNull("RingIndex out of range", polygon.getVertex(2, 0));
  }
}
