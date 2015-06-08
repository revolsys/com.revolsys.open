package com.revolsys.gis.algorithm.index;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.data.filter.RecordEqualsFilter;
import com.revolsys.data.filter.RecordGeometryBoundingBoxIntersectsFilter;
import com.revolsys.data.filter.RecordGeometryDistanceFilter;
import com.revolsys.data.filter.RecordGeometryIntersectsFilter;
import com.revolsys.data.record.Record;
import com.revolsys.filter.Filter;
import com.revolsys.gis.algorithm.index.quadtree.QuadTree;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.visitor.CreateListVisitor;

public class RecordQuadTree extends QuadTree<Record> {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public RecordQuadTree() {
  }

  public RecordQuadTree(final Collection<? extends Record> objects) {
    insert(objects);
  }

  public RecordQuadTree(final GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

  public RecordQuadTree(final GeometryFactory geometryFactory,
    final Collection<? extends Record> objects) {
    super(geometryFactory);
    insert(objects);
  }

  public void insert(final Collection<? extends Record> objects) {
    for (final Record object : objects) {
      insert(object);
    }
  }

  public void insert(final Record record) {
    if (record != null) {
      final Geometry geometry = record.getGeometryValue();
      if (geometry != null && !geometry.isEmpty()) {
        final BoundingBox boundingBox = geometry.getBoundingBox();
        insert(boundingBox, record);
      }
    }
  }

  public void insertAll(final Collection<? extends Record> records) {
    for (final Record record : records) {
      insert(record);
    }
  }

  @Override
  public List<Record> query(final BoundingBox boundingBox) {
    final List<Record> results = super.query(boundingBox);
    for (final Iterator<Record> iterator = results.iterator(); iterator.hasNext();) {
      final Record object = iterator.next();
      final Geometry geometry = object.getGeometryValue();
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

  public void query(final Geometry geometry, final Visitor<Record> visitor) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    visit(boundingBox, visitor);
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
      final Geometry geometry = object.getGeometryValue();
      return queryBoundingBox(geometry);
    }
  }

  public Record queryFirst(final Record object, final Filter<Record> filter) {
    if (object == null) {
      return null;
    } else {
      final Geometry geometry = object.getGeometryValue();
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
      final Filter<Record> filter = new RecordGeometryBoundingBoxIntersectsFilter(boundingBox);
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

  public List<Record> queryList(final BoundingBox boundingBox, final Filter<Record> filter) {
    return queryList(boundingBox, filter, null);
  }

  public List<Record> queryList(final BoundingBox boundingBox, final Filter<Record> filter,
    final Comparator<Record> comparator) {
    final CreateListVisitor<Record> listVisitor = new CreateListVisitor<Record>(filter);
    visit(boundingBox, listVisitor);
    final List<Record> list = listVisitor.getList();
    if (comparator != null) {
      Collections.sort(list, comparator);
    }
    return list;
  }

  public List<Record> queryList(final Geometry geometry, final Filter<Record> filter) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    return queryList(boundingBox, filter);
  }

  public List<Record> queryList(final Geometry geometry, final Filter<Record> filter,
    final Comparator<Record> comparator) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    return queryList(boundingBox, filter, comparator);
  }

  public List<Record> queryList(final Record object, final Filter<Record> filter) {
    final Geometry geometry = object.getGeometryValue();
    return queryList(geometry, filter);
  }

  public void remove(final Collection<? extends Record> objects) {
    for (final Record object : objects) {
      remove(object);
    }
  }

  public boolean remove(final Record object) {
    final Geometry geometry = object.getGeometryValue();
    if (geometry == null) {
      return false;
    } else {
      final BoundingBox boundinBox = geometry.getBoundingBox();
      return super.remove(boundinBox, object);
    }
  }
}
