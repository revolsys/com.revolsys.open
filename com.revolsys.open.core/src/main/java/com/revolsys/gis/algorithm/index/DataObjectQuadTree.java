package com.revolsys.gis.algorithm.index;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;
import com.revolsys.gis.algorithm.index.quadtree.QuadTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.filter.DataObjectEqualsFilter;
import com.revolsys.gis.data.model.filter.DataObjectGeometryDistanceFilter;
import com.revolsys.gis.data.model.filter.DataObjectGeometryIntersectsFilter;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.visitor.CreateListVisitor;

public class DataObjectQuadTree extends QuadTree<DataObject> {
  public DataObjectQuadTree() {
  }

  public DataObjectQuadTree(final Collection<? extends DataObject> objects) {
    insert(objects);
  }

  public DataObjectQuadTree(final GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

  public DataObjectQuadTree(final GeometryFactory geometryFactory,
    final Collection<? extends DataObject> objects) {
    super(geometryFactory);
    insert(objects);
  }

  public void insert(final Collection<? extends DataObject> objects) {
    for (final DataObject object : objects) {
      insert(object);
    }
  }

  public void insert(final DataObject object) {
    if (object != null) {
      final Geometry geometry = object.getGeometryValue();
      if (geometry != null && !geometry.isEmpty()) {
        final BoundingBox boundingBox = BoundingBox.getBoundingBox(geometry);
        insert(boundingBox, object);
      }
    }
  }

  public void insertAll(final Collection<? extends DataObject> objects) {
    for (final DataObject object : objects) {
      insert(object);
    }
  }

  @Override
  public List<DataObject> query(final BoundingBox boundingBox) {
    final List<DataObject> results = super.query(boundingBox);
    for (final Iterator<DataObject> iterator = results.iterator(); iterator.hasNext();) {
      final DataObject object = iterator.next();
      final Geometry geometry = object.getGeometryValue();
      final BoundingBox objectBoundingBox = BoundingBox.getBoundingBox(geometry);
      if (!boundingBox.intersects(objectBoundingBox)) {
        iterator.remove();
      }
    }
    return results;
  }

  public void query(final Geometry geometry, final Visitor<DataObject> visitor) {
    final BoundingBox boundingBox = BoundingBox.getBoundingBox(geometry);
    query(boundingBox, visitor);
  }

  public List<DataObject> queryDistance(final Geometry geometry,
    final double distance) {
    BoundingBox boundingBox = BoundingBox.getBoundingBox(geometry);
    boundingBox = boundingBox.expand(distance);
    final DataObjectGeometryDistanceFilter filter = new DataObjectGeometryDistanceFilter(
      geometry, distance);
    return queryList(boundingBox, filter);
  }

  public List<DataObject> queryEnvelope(final DataObject object) {
    final Geometry geometry = object.getGeometryValue();
    return queryBoundingBox(geometry);
  }

  public DataObject queryFirst(final DataObject object,
    final Filter<DataObject> filter) {
    final Geometry geometry = object.getGeometryValue();
    return queryFirst(geometry, filter);
  }

  public DataObject queryFirstEquals(final DataObject object,
    final Collection<String> excludedAttributes) {
    final DataObjectEqualsFilter filter = new DataObjectEqualsFilter(object,
      excludedAttributes);
    return queryFirst(object, filter);
  }

  public List<DataObject> queryIntersects(final BoundingBox boundingBox) {

    final BoundingBox convertedBoundingBox = boundingBox.convert(getGeometryFactory());
    if (convertedBoundingBox.isEmpty()) {
      return Arrays.asList();
    } else {
      final Geometry geometry = convertedBoundingBox.toPolygon(1, 1);
      final DataObjectGeometryIntersectsFilter filter = new DataObjectGeometryIntersectsFilter(
        geometry);
      return queryList(geometry, filter);
    }
  }

  public List<DataObject> queryIntersects(Geometry geometry) {
    final com.revolsys.jts.geom.GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory != null) {
      geometry = geometryFactory.copy(geometry);
    }
    final DataObjectGeometryIntersectsFilter filter = new DataObjectGeometryIntersectsFilter(
      geometry);
    return queryList(geometry, filter);
  }

  public List<DataObject> queryList(final BoundingBox boundingBox,
    final Filter<DataObject> filter) {
    return queryList(boundingBox, filter, null);
  }

  public List<DataObject> queryList(final BoundingBox boundingBox,
    final Filter<DataObject> filter, final Comparator<DataObject> comparator) {
    final CreateListVisitor<DataObject> listVisitor = new CreateListVisitor<DataObject>(
      filter);
    query(boundingBox, listVisitor);
    final List<DataObject> list = listVisitor.getList();
    if (comparator != null) {
      Collections.sort(list, comparator);
    }
    return list;
  }

  public List<DataObject> queryList(final DataObject object,
    final Filter<DataObject> filter) {
    final Geometry geometry = object.getGeometryValue();
    return queryList(geometry, filter);
  }

  public List<DataObject> queryList(final Geometry geometry,
    final Filter<DataObject> filter) {
    final BoundingBox boundingBox = BoundingBox.getBoundingBox(geometry);
    return queryList(boundingBox, filter);
  }

  public List<DataObject> queryList(final Geometry geometry,
    final Filter<DataObject> filter, final Comparator<DataObject> comparator) {
    final BoundingBox boundingBox = BoundingBox.getBoundingBox(geometry);
    return queryList(boundingBox, filter, comparator);
  }

  public void remove(final Collection<? extends DataObject> objects) {
    for (final DataObject object : objects) {
      remove(object);
    }
  }

  public boolean remove(final DataObject object) {
    final Geometry geometry = object.getGeometryValue();
    if (geometry == null) {
      return false;
    } else {
      final BoundingBox boundinBox = BoundingBox.getBoundingBox(geometry);
      return super.remove(boundinBox, object);
    }
  }
}
