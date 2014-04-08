package com.revolsys.gis.model.geometry.util;

import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.jts.geom.GeometryFactory;

public class IndexedLineSegment extends LineSegment {

  private final int[] index;

  public IndexedLineSegment(final GeometryFactory geometryFactory,
    final LineSegment line, final int... index) {
    super(geometryFactory, line);
    this.index = index;
  }

  public IndexedLineSegment(final LineSegment line, final int... index) {
    super(line);
    this.index = index;
  }

  public int[] getIndex() {
    return index;
  }
}
