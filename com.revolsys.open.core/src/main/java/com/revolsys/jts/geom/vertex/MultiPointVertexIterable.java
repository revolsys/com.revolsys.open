package com.revolsys.jts.geom.vertex;

import java.util.NoSuchElementException;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.jts.geom.MultiPoint;

public class MultiPointVertexIterable extends AbstractIterator<Vertex> {
  private int partIndex = 0;

  private MultiPointVertex vertex;

  private final int partCount;

  public MultiPointVertexIterable(final MultiPoint geometry) {
    this.vertex = new MultiPointVertex(geometry, 0);
    this.partCount = geometry.getGeometryCount();
  }

  @Override
  protected Vertex getNext() throws NoSuchElementException {
    if (this.partIndex < partCount) {
      this.vertex.setPartIndex(this.partIndex);
      this.partIndex++;
      return this.vertex;
    } else {
      this.vertex = null;
      throw new NoSuchElementException();
    }
  }
}
