package com.revolsys.swing.map.layer.record.renderer.shape;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

import com.revolsys.geometry.model.LineString;

public class LineStringShape extends AbstractGeometryShape<LineString> {
  private final LineStringPathIteratorTransform iteratorTransform = new LineStringPathIteratorTransform();

  private final LineStringPathIterator iterator = new LineStringPathIterator();

  public LineStringShape() {
  }

  public LineStringShape(final LineString line) {
    super(line);
  }

  @Override
  public PathIterator getPathIterator(final AffineTransform at) {
    if (at == null) {
      return this.iterator.reset(this.geometry);
    } else {
      return this.iteratorTransform.reset(this.geometry, at);
    }
  }

}
