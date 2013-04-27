package com.revolsys.gis.model.geometry.util;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.geometry.LineSegment;

public class IndexedLineSegment extends LineSegment {

  private int[] index;

  public IndexedLineSegment(LineSegment line, int... index) {
    super(line);
    this.index = index;
  }

  public IndexedLineSegment(GeometryFactory geometryFactory, LineSegment line,
    int... index) {
    super(geometryFactory, line);
    this.index = index;
  }

  public int[] getIndex() {
    return index;
  }
}
