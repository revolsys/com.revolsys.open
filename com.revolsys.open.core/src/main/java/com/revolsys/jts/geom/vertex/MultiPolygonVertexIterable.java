package com.revolsys.jts.geom.vertex;

import java.util.NoSuchElementException;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Polygon;

public class MultiPolygonVertexIterable extends AbstractIterator<Vertex> {
  private VertexImpl vertex;

  private int vertexIndex = 0;

  private int ringIndex = 0;

  private int partIndex = 0;

  private Polygon polygon;

  private LineString ring;

  private final MultiPolygon multiPolygon;

  public MultiPolygonVertexIterable(final MultiPolygon multiPolygon) {
    // TODO hanlde empty polygons
    this.multiPolygon = multiPolygon;
    this.polygon = multiPolygon.getPolygons().get(0);
    this.ring = this.polygon.getExteriorRing();
    this.vertex = new VertexImpl(multiPolygon, 0);
  }

  @Override
  protected Vertex getNext() throws NoSuchElementException {
    while (vertexIndex >= ring.getNumPoints()) {
      vertexIndex = 0;
      ringIndex++;
      if (ringIndex < 1 + polygon.getNumInteriorRing()) {
        ring = polygon.getInteriorRingN(ringIndex - 1);
      } else {
        partIndex++;
        if (partIndex < multiPolygon.getNumGeometries()) {
          polygon = multiPolygon.getPolygons().get(partIndex);
          ring = polygon.getExteriorRing();
        } else {
          vertex = null;
          throw new NoSuchElementException();
        }
      }
    }

    vertex.setVertexId(ringIndex, vertexIndex);
    vertexIndex++;
    return vertex;
  }
}
