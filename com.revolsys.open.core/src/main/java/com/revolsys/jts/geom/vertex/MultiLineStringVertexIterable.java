package com.revolsys.jts.geom.vertex;

import java.util.NoSuchElementException;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;

public class MultiLineStringVertexIterable extends AbstractIterator<Vertex> {
  private VertexImpl vertex;

  private int vertexIndex = 0;

  private int partIndex = 0;

  private LineString line;

  private MultiLineString multiLineString;

  public MultiLineStringVertexIterable(final MultiLineString multiLineString) {
    this.vertex = new VertexImpl(multiLineString, 0);
    this.multiLineString = multiLineString;
    this.line = multiLineString.getLineStrings().get(0);
  }

  @Override
  protected Vertex getNext() throws NoSuchElementException {
    while (vertexIndex >= line.getNumPoints()) {
      this.vertexIndex = 0;
      this.partIndex++;
      if (partIndex < multiLineString.getLineStrings().size()) {
        this.line = multiLineString.getLineStrings().get(partIndex);
      } else {
        this.multiLineString = null;
        this.line = null;
        this.vertex = null;
        throw new NoSuchElementException();
      }
    }

    this.vertex.setVertexId(this.partIndex, this.vertexIndex);
    this.vertexIndex++;
    return this.vertex;
  }
}
