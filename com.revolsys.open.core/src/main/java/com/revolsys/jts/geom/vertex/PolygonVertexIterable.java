package com.revolsys.jts.geom.vertex;

import java.util.NoSuchElementException;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Polygon;

public class PolygonVertexIterable extends AbstractIterator<Vertex> {
  private VertexImpl vertex;

  private int vertexIndex = 0;

  private int ringIndex = 0;

  private LinearRing ring;

  private Polygon polygon;

  public PolygonVertexIterable(final Polygon polygon) {
    this.vertex = new VertexImpl(polygon, 0);
    this.polygon = polygon;
    this.ring = polygon.getExteriorRing();
  }

  @Override
  protected Vertex getNext() throws NoSuchElementException {
    while (vertexIndex >= ring.getVertexCount()) {
      vertexIndex = 0;
      ringIndex++;
      if (ringIndex < 1 + polygon.getNumInteriorRing()) {
        this.ring = polygon.getInteriorRingN(ringIndex - 1);
      } else {
        this.polygon = null;
        this.ring = null;
        this.vertex = null;
        throw new NoSuchElementException();
      }
    }

    vertex.setVertexId(ringIndex, vertexIndex);
    vertexIndex++;
    return vertex;
  }
}
