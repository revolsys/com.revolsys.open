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
import com.revolsys.record.filter.RecordGeometryIntersectsFilter;
import com.revolsys.visitor.CreateListVisitor;

public class RecordQuadTree extends QuadTree<Record> {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public RecordQuadTree() {
  }

  public RecordQuadTree(final GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

  public RecordQuadTree(final GeometryFactory geometryFactory,
    final Iterable<? extends Record> records) {
    super(geometryFactory);
    addRecords(records);
  }

  public RecordQuadTree(final Iterable<? extends Record> records) {
    addRecords(records);
  }

  public void addRecord(final Record record) {
    if (record != null) {
      final Geometry geometry = record.getGeometry();
      if (geometry != null && !geometry.isEmpty()) {
        final BoundingBox boundingBox = geometry.getBoundingBox();
        insert(boundingBox, record);
      }
    }
  }

  public void addRecords(final Iterable<? extends Record> records) {
    for (final Record record : records) {
      addRecord(record);
    }
  }

  @Override
  public List<Record> query(final BoundingBox boundingBox) {
    final List<Record> results = super.query(boundingBox);
    for (final Iterator<Record> iterator = results.iterator(); iterator.hasNext();) {
      final Record object = iterator.next();
      final Geometry geometry = object.getGeometry();
      if (geometry == null) {
        iterator.remove();
      } else {
        final BoundingBox objectBoundingBox = geometry.getBoundingBox();
        if (!boundingBox.intersects(objectBoundingBox)) {
          iterator.remove();
        }
      }
    }
    return results;
  }

  public void query(final Geometry geometry, final Consumer<Record> visitor) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    forEach(visitor, boundingBox);
  }

  public List<Record> queryDistance(final Geometry geometry, final double distance) {
    if (geometry == null) {
      return Collections.emptyList();
    } else {
      BoundingBox boundingBox = geometry.getBoundingBox();
      boundingBox = boundingBox.expand(distance);
      final Predicate<Record> filter = Records.newFilter(geometry, distance);
      return queryList(boundingBox, filter);
    }
  }

  public List<Record> queryEnvelope(final Record object) {
    if (object == null) {
      return Collections.emptyList();
    } else {
      final Geometry geometry = object.getGeometry();
      return queryBoundingBox(geometry);
    }
  }

  public Record queryFirst(final Record object, final Predicate<Record> filter) {
    if (object == null) {
      return null;
    } else {
      final Geometry geometry = object.getGeometry();
      return getFirstBoundingBox(geometry, filter);
    }
  }

  public Record queryFirstEquals(final Record object, final Collection<String> excludedAttributes) {
    if (object == null) {
      return null;
    } else {
      final RecordEqualsFilter filter = new RecordEqualsFilter(object, excludedAttributes);
      return queryFirst(object, filter);
    }
  }

  public List<Record> queryIntersects(final BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final BoundingBox convertedBoundingBox = boundingBox.convert(geometryFactory);
    if (convertedBoundingBox.isEmpty()) {
      return Arrays.asList();
    } else {
      final Predicate<Record> filter = Records.newFilter(convertedBoundingBox);
      return queryList(convertedBoundingBox, filter);
    }
  }

  public List<Record> queryIntersects(Geometry geometry) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory != null) {
      geometry = geometry.convert(geometryFactory);
    }
    final RecordGeometryIntersectsFilter filter = new RecordGeometryIntersectsFilter(geometry);
    return queryList(geometry, filter);
  }

  public List<Record> queryList(final BoundingBox boundingBox, final Predicate<Record> filter) {
    return queryList(boundingBox, filter, null);
  }

  public List<Record> queryList(final BoundingBox boundingBox, final Predicate<Record> filter,
    final Comparator<Record> comparator) {
    final CreateListVisitor<Record> listVisitor = new CreateListVisitor<Record>(filter);
    forEach(listVisitor, boundingBox);
    final List<Record> list = listVisitor.getList();
    if (comparator != null) {
      Collections.sort(list, comparator);
    }
    return list;
  }

  public List<Record> queryList(final Geometry geometry, final Predicate<Record> filter) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    return queryList(boundingBox, filter);
  }

  public List<Record> queryList(final Geometry geometry, final Predicate<Record> filter,
    final Comparator<Record> comparator) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    return queryList(boundingBox, filter, comparator);
  }

  public List<Record> queryList(final Record object, final Predicate<Record> filter) {
    final Geometry geometry = object.getGeometry();
    return queryList(geometry, filter);
  }

  public void remove(final Collection<? extends Record> objects) {
    for (final Record object : objects) {
      remove(object);
    }
  }

  public boolean remove(final Record object) {
    final Geometry geometry = object.getGeometry();
    if (geometry == null) {
      return false;
    } else {
      final BoundingBox boundinBox = geometry.getBoundingBox();
      return super.remove(boundinBox, object);
    }
  }
}
