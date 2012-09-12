package com.revolsys.gis.jts.filter;

import com.revolsys.filter.Filter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;

public class LinearIntersectionFilter implements Filter<LineString> {

  private final LineString line;

  private final PreparedGeometry preparedLine;

  private final Envelope envelope;

  public LinearIntersectionFilter(final LineString line) {
    this.line = line;
    this.preparedLine = PreparedGeometryFactory.prepare(line);
    this.envelope = line.getEnvelopeInternal();
  }

  @Override
  public boolean accept(final LineString line) {
    final Envelope envelope = line.getEnvelopeInternal();
    if (envelope.intersects(this.envelope)) {
      if (preparedLine.intersects(line)) {
        final IntersectionMatrix relate = this.line.relate(line);
        if (relate.isOverlaps(1, 1) || relate.isContains() || relate.isWithin()) {
          return true;
        }
      }
    }
    return false;
  }
}
