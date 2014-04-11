package com.revolsys.jts.geom.vertex;

import java.util.NoSuchElementException;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.jts.geom.LineString;

public class LineStringVertexIterable extends AbstractIterator<Vertex> {
  private LineStringVertex vertex;

  private int index = 0;

  private final int vertexCount;

  public LineStringVertexIterable(final LineString line) {
    this.vertex = new LineStringVertex(line, 0);
    this.vertexCount = line.getVertexCount();
  }

  @Override
  protected LineStringVertex getNext() throws NoSuchElementException {
    if (index < vertexCount) {
      vertex.setVertexIndex(index);
      index++;
      return vertex;
    } else {
      vertex = null;
      throw new NoSuchElementException();
    }
  }
}
