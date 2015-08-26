package com.revolsys.data.record.filter;

import java.util.function.Predicate;

import com.revolsys.data.record.Record;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.gis.algorithm.index.RecordQuadTree;
import com.revolsys.predicate.Predicates;
import com.revolsys.util.Property;

public class ClosestRecordFilter implements Predicate<Record> {

  public static ClosestRecordFilter query(final RecordQuadTree index, final Geometry geometry,
    final double maxDistance) {
    final ClosestRecordFilter closestFilter = new ClosestRecordFilter(geometry, maxDistance);
    final BoundingBox boundingBox = closestFilter.getFilterBoundingBox();
    index.queryList(boundingBox, closestFilter);
    return closestFilter;
  }

  public static ClosestRecordFilter query(final RecordQuadTree index, final Geometry geometry,
    final double maxDistance, final Predicate<Record> filter) {
    final ClosestRecordFilter closestFilter = new ClosestRecordFilter(geometry, maxDistance,
      filter);
    final BoundingBox boundingBox = closestFilter.getFilterBoundingBox();
    index.queryList(boundingBox, closestFilter);
    return closestFilter;
  }

  private double closestDistance = Double.MAX_VALUE;

  private Record closestRecord;

  private final Predicate<Record> filter;

  private final Geometry geometry;

  private final double maxDistance;

  public ClosestRecordFilter(final Geometry geometry, final double maxDistance) {
    this(geometry, maxDistance, Predicates.all());
  }

  public ClosestRecordFilter(final Geometry geometry, final double maxDistance,
    final Predicate<Record> filter) {
    this.geometry = geometry;
    this.maxDistance = maxDistance;
    this.filter = filter;
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

  @Override
  public boolean test(final Record record) {
    if (this.filter.test(record)) {
      final Geometry geometry = record.getGeometry();
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
}
