package com.revolsys.gis.algorithm.linematch;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.segment.LineSegment;
import com.revolsys.jts.geom.segment.LineSegmentDoubleGF;

public class LineSegmentMatch {
  private final LineSegment segment;

  private final List<LineSegment> segments = new ArrayList<LineSegment>();

  public LineSegmentMatch(final GeometryFactory geometryFactory,
    final Point start, final Point end) {
    this(new LineSegmentDoubleGF(geometryFactory, start, end));
  }

  public LineSegmentMatch(final LineSegment segment) {
    this.segment = segment;
  }

  public void addSegment(final LineSegment segment, final int index) {
    while (index >= segments.size()) {
      segments.add(null);
    }
    segments.set(index, segment);
  }

  public BoundingBox getEnvelope() {
    return segment.getBoundingBox();
  }

  public LineString getLine() {
    return segment;
  }

  public int getMatchCount(final int index) {
    if (segments.get(index) == null) {
      return 0;
    }
    int matchCount = 0;
    for (final LineSegment segment : segments) {
      if (segment != null) {
        matchCount++;
      }

    }
    return matchCount;
  }

  /**
   * @return the segment
   */
  public LineSegment getSegment() {
    return segment;
  }

  /**
   * @param index
   * @return the segment
   */
  public LineSegment getSegment(final int index) {
    return segments.get(index);
  }

  /**
   * @return the segments
   */
  public int getSegmentCount() {
    return segments.size();
  }

  /**
   * @return the segments
   */
  public List<LineSegment> getSegments() {
    return segments;
  }

  public boolean hasMatches(final int index) {
    if (index < segments.size()) {
      final LineSegment segment = segments.get(index);
      if (segment == null) {
        return false;
      }
      for (int i = 0; i < segments.size(); i++) {
        if (i != index) {
          final LineSegment otherSegment = segments.get(i);
          if (otherSegment != null) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public boolean hasMatches(final int index1, final int index2) {
    if (index1 < segments.size() && index2 < segments.size()) {
      final LineSegment segment1 = segments.get(index1);
      final LineSegment segment2 = segments.get(index2);
      return segment1 != null && segment2 != null;
    } else {
      return false;
    }
  }

  public boolean hasOtherSegment(final int index) {
    for (int i = 0; i < segments.size(); i++) {
      if (i != index) {
        if (segments.get(i) != null) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean hasSegment(final int index) {
    if (index < segments.size()) {
      return segments.get(index) != null;
    } else {
      return false;
    }
  }

  public boolean isMatchedWithBase(final int index) {
    if (segments.get(index) == null) {
      return false;
    } else if (segments.get(0) == null) {
      return false;
    } else {
      return true;
    }
  }

  public void removeSegment(final int i) {
    segments.set(i, null);
  }

  @Override
  public String toString() {
    return segment.toString();
  }
}
