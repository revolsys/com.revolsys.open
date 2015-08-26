package com.revolsys.data.filter;

import java.util.function.Predicate;

import com.revolsys.data.record.Record;
import com.revolsys.geometry.model.LineString;
import com.revolsys.gis.graph.linestring.LineStringRelate;

public class LineEqualWithinDistance implements Predicate<LineString> {

  public static Predicate<Record> getFilter(final Record object, final double maxDistance) {
    final LineString line = object.getGeometry();
    final LineEqualWithinDistance lineFilter = new LineEqualWithinDistance(line, maxDistance);
    return new RecordGeometryFilter<LineString>(lineFilter);
  }

  private final LineString line;

  private final double maxDistance;

  public LineEqualWithinDistance(final LineString line, final double maxDistance) {
    this.line = line;
    this.maxDistance = maxDistance;
  }

  @Override
  public boolean test(final LineString line2) {
    final LineStringRelate relate = new LineStringRelate(this.line, line2, this.maxDistance);
    return relate.isEqual();
  }
}
