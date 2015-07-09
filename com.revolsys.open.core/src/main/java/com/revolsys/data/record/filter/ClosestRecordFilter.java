package com.revolsys.data.record.filter;

import com.revolsys.data.record.Record;
import com.revolsys.filter.AcceptAllFilter;
import com.revolsys.filter.Filter;
import com.revolsys.gis.algorithm.index.RecordQuadTree;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.util.Property;

public class ClosestRecordFilter implements Filter<Record> {

  public static ClosestRecordFilter query(final RecordQuadTree index, final Geometry geometry,
    final double maxDistance) {
    final ClosestRecordFilter closestFilter = new ClosestRecordFilter(geometry, maxDistance);
    final BoundingBox boundingBox = closestFilter.getFilterBoundingBox();
    index.queryList(boundingBox, closestFilter);
    return closestFilter;
  }

  public static ClosestRecordFilter query(final RecordQuadTree index, final Geometry geometry,
    final double maxDistance, final Filter<Record> filter) {
    final ClosestRecordFilter closestFilter = new ClosestRecordFilter(geometry, maxDistance,
      filter);
    final BoundingBox boundingBox = closestFilter.getFilterBoundingBox();
    index.queryList(boundingBox, closestFilter);
    return closestFilter;
  }

  private final Filter<Record> filter;

  private final Geometry geometry;

  private final double maxDistance;

  private Record closestRecord;

  private double closestDistance = Double.MAX_VALUE;

  public ClosestRecordFilter(final Geometry geometry, final double maxDistance) {
    this(geometry, maxDistance, AcceptAllFilter.get());
  }

  public ClosestRecordFilter(final Geometry geometry, final double maxDistance,
    final Filter<Record> filter) {
    this.geometry = geometry;
    this.maxDistance = maxDistance;
    this.filter = filter;
  }

  @Override
  public boolean accept(final Record record) {
    if (this.filter.accept(record)) {
      final Geometry geometry = record.getGeometryValue();
      if (Property.hasValue(geometry)) {
        if (!(geometry instanceof Point)) {
          final BoundingBox boundingBox = geometry.getBoundingBox();
          if (!boundingBox.isWithinDistance(this.geometry, this.maxDistance)) {
            return false;
          }
        }
        final double distance = geometry.distance(this.geometry);
        if (distance < this.closestDistance) {
          this.closestDistance = distance;
          this.closestRecord = record;
          return true;
        }
      }
    }
    return false;
  }

  public double getClosestDistance() {
    return this.closestDistance;
  }

  public Record getClosestRecord() {
    return this.closestRecord;
  }

  public BoundingBox getFilterBoundingBox() {
    final BoundingBox boundingBox = GeometryFactory.boundingBox(this.geometry);
    return boundingBox.expand(this.maxDistance);
  }
}
