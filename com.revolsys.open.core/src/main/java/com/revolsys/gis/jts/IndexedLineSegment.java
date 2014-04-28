package com.revolsys.gis.jts;

import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineSegment;

public class IndexedLineSegment extends LineSegmentImpl {

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
