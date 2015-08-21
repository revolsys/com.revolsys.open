package com.revolsys.gis.jts.filter;

import java.util.function.Predicate;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.IntersectionMatrix;
import com.revolsys.geometry.model.LineString;

public class LinearIntersectionFilter implements Predicate<LineString> {

  private final LineString line;

  private final Geometry preparedLine;

  private final BoundingBox envelope;

  public LinearIntersectionFilter(final LineString line) {
    this.line = line;
    this.preparedLine = line.prepare();
    this.envelope = line.getBoundingBox();
  }

  @Override
  public boolean test(final LineString line) {
    final BoundingBox envelope = line.getBoundingBox();
    if (envelope.intersects(this.envelope)) {
      if (this.preparedLine.intersects(line)) {
        final IntersectionMatrix relate = this.line.relate(line);
        if (relate.isOverlaps(1, 1) || relate.isContains() || relate.isWithin()) {
          return true;
        }
      }
    }
    return false;
  }
}
