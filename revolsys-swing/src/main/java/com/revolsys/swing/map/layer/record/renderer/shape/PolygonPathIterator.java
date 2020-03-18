package com.revolsys.swing.map.layer.record.renderer.shape;

import java.awt.geom.PathIterator;

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Polygon;

public class PolygonPathIterator implements PathIterator {
  protected final Polygon polygon;

  protected LinearRing ring;

  private boolean done;

  protected int vertexIndex = 0;

  protected int awtType = SEG_MOVETO;

  private int vertexCount;

  int ringIndex = 0;

  /** Shared buffer for coordinate transforms. */
  protected final double[] currentCoordinates = new double[2];

  private int lastVertexIndex;

  public PolygonPathIterator(final Polygon polygon) {
    this.polygon = polygon;
    final LinearRing ring = polygon.getShell();
    setRing(ring);
    this.done = this.vertexCount == 0;

  }

  @Override
  public int currentSegment(final double[] coordinates) {
    final LineString line = this.ring;
    final int vertexIndex = this.vertexIndex;
    line.copyPoint(vertexIndex, 2, coordinates);
    return this.awtType;
  }

  @Override
  public int currentSegment(final float[] coordinates) {
    final LineString line = this.ring;
    final int vertexIndex = this.vertexIndex;
    line.copyPoint(vertexIndex, 2, this.currentCoordinates);
    coordinates[0] = (float)this.currentCoordinates[0];
    coordinates[1] = (float)this.currentCoordinates[1];
    return this.awtType;
  }

  @Override
  public int getWindingRule() {
    return WIND_EVEN_ODD;
  }

  @Override
  public boolean isDone() {
    return this.done;
  }

  @Override
  public void next() {
    this.vertexIndex++;
    if (this.vertexIndex < this.lastVertexIndex) {
      this.awtType = SEG_LINETO;
    } else if (this.vertexIndex == this.lastVertexIndex) {
      this.awtType = SEG_CLOSE;
    } else {
      while (++this.ringIndex < this.polygon.getRingCount()) {
        final LinearRing ring = this.polygon.getRing(this.ringIndex);
        if (ring.getVertexCount() > 0) {
          setRing(ring);
          return;
        }
      }
      this.done = true;
    }
  }

  protected void setRing(final LinearRing ring) {
    if (ring == null) {
      this.vertexCount = 0;
    } else {
      this.ring = ring;
      this.vertexIndex = 0;
      this.vertexCount = ring.getVertexCount();
      this.lastVertexIndex = this.vertexCount - 1;
      this.awtType = SEG_MOVETO;
    }
  }
}
