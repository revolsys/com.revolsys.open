package com.revolsys.geometry.algorithm.index;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.data.filter.RecordEqualsFilter;
import com.revolsys.data.filter.RecordGeometryBoundingBoxIntersectsFilter;
import com.revolsys.data.filter.RecordGeometryDistanceFilter;
import com.revolsys.data.filter.RecordGeometryIntersectsFilter;
import com.revolsys.data.record.Record;
import com.revolsys.geometry.algorithm.index.quadtree.QuadTree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
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
    addAll(records);
  }

  public RecordQuadTree(final Iterable<? extends Record> records) {
    addAll(records);
  }

  public void add(final Record record) {
    if (record != null) {
      final Geometry geometry = record.getGeometry();
      if (geometry != null && !geometry.isEmpty()) {
        final BoundingBox boundingBox = geometry.getBoundingBox();
        insert(boundingBox, record);
      }
    }
  }

  public void addAll(final Iterable<? extends Record> records) {
    for (final Record record : records) {
      add(record);
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
      final RecordGeometryDistanceFilter filter = new RecordGeometryDistanceFilter(geometry,
        distance);
      return queryList(boundingBox, filter, filter);
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

    final BoundingBox convertedBoundingBox = boundingBox.convert(getGeometryFactory());
    if (convertedBoundingBox.isEmpty()) {
      return Arrays.asList();
    } else {
      final Predicate<Record> filter = new RecordGeometryBoundingBoxIntersectsFilter(boundingBox);
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
