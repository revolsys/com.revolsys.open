package com.revolsys.geometry.algorithm.index;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.geometry.algorithm.index.quadtree.QuadTree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.Record;
import com.revolsys.record.Records;
import com.revolsys.record.filter.RecordEqualsFilter;
import com.revolsys.visitor.CreateListVisitor;

public class RecordQuadTree<R extends Record> extends QuadTree<R> {
  private static final long serialVersionUID = 1L;

  public RecordQuadTree() {
  }

  public RecordQuadTree(final GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

  public RecordQuadTree(final GeometryFactory geometryFactory,
    final Iterable<? extends R> records) {
    super(geometryFactory);
    addRecords(records);
  }

  public RecordQuadTree(final Iterable<? extends R> records) {
    addRecords(records);
  }

  public void addRecord(final R record) {
    if (record != null) {
      final Geometry geometry = record.getGeometry();
      if (geometry != null && !geometry.isEmpty()) {
        final BoundingBox boundingBox = geometry.getBoundingBox();
        insert(boundingBox, record);
      }
    }
  }

  public void addRecords(final Iterable<? extends R> records) {
    if (records != null) {
      for (final R record : records) {
        addRecord(record);
      }
    }
  }

  @Override
  public List<R> query(final BoundingBox boundingBox) {
    final List<R> results = super.query(boundingBox);
    for (final Iterator<R> iterator = results.iterator(); iterator.hasNext();) {
      final R record = iterator.next();
      final Geometry geometry = record.getGeometry();
      if (geometry == null) {
        iterator.remove();
      } else {
        final BoundingBox recordBoundingBox = geometry.getBoundingBox();
        if (!boundingBox.intersects(recordBoundingBox)) {
          iterator.remove();
        }
      }
    }
    return results;
  }

  public void query(final Geometry geometry, final Consumer<R> visitor) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    forEach(visitor, boundingBox);
  }

  public List<R> queryDistance(final Geometry geometry, final double distance) {
    if (geometry == null) {
      return Collections.emptyList();
    } else {
      BoundingBox boundingBox = geometry.getBoundingBox();
      boundingBox = boundingBox.expand(distance);
      final Predicate<R> filter = Records.newFilter(geometry, distance);
      return queryList(boundingBox, filter);
    }
  }

  public List<R> queryEnvelope(final R record) {
    if (record == null) {
      return Collections.emptyList();
    } else {
      final Geometry geometry = record.getGeometry();
      return queryBoundingBox(geometry);
    }
  }

  public R queryFirst(final R record, final Predicate<R> filter) {
    if (record == null) {
      return null;
    } else {
      final Geometry geometry = record.getGeometry();
      return getFirstBoundingBox(geometry, filter);
    }
  }

  public R queryFirstEquals(final R record, final Collection<String> excludedAttributes) {
    if (record == null) {
      return null;
    } else {
      final RecordEqualsFilter<R> filter = new RecordEqualsFilter<>(record, excludedAttributes);
      return queryFirst(record, filter);
    }
  }

  public List<R> queryIntersects(final BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final BoundingBox convertedBoundingBox = boundingBox.convert(geometryFactory);
    if (convertedBoundingBox.isEmpty()) {
      return Arrays.asList();
    } else {
      final Predicate<R> filter = Records.newFilter(convertedBoundingBox);
      return queryList(convertedBoundingBox, filter);
    }
  }

  public List<R> queryIntersects(Geometry geometry) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory != null) {
      geometry = geometry.convertGeometry(geometryFactory);
    }
    final Predicate<R> filter = Records.newFilterGeometryIntersects(geometry);
    return queryList(geometry, filter);
  }

  public List<R> queryList(final BoundingBox boundingBox, final Predicate<R> filter) {
    return queryList(boundingBox, filter, null);
  }

  public List<R> queryList(final BoundingBox boundingBox, final Predicate<R> filter,
    final Comparator<R> comparator) {
    final CreateListVisitor<R> listVisitor = new CreateListVisitor<>(filter);
    forEach(listVisitor, boundingBox);
    final List<R> list = listVisitor.getList();
    if (comparator != null) {
      Collections.sort(list, comparator);
    }
    return list;
  }

  public List<R> queryList(final Geometry geometry, final Predicate<R> filter) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    return queryList(boundingBox, filter);
  }

  public List<R> queryList(final Geometry geometry, final Predicate<R> filter,
    final Comparator<R> comparator) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    return queryList(boundingBox, filter, comparator);
  }

  public List<R> queryList(final R record, final Predicate<R> filter) {
    final Geometry geometry = record.getGeometry();
    return queryList(geometry, filter);
  }

  public boolean removeRecord(final R record) {
    if (record != null) {
      final Geometry geometry = record.getGeometry();
      if (geometry != null) {
        final BoundingBox boundinBox = geometry.getBoundingBox();
        return super.removeItem(boundinBox, record);
      }
    }
    return false;
  }

  public void removeRecords(final Iterable<? extends R> records) {
    if (records != null) {
      for (final R record : records) {
        removeRecord(record);
      }
    }
  }
}
