package com.revolsys.gis.algorithm.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.revolsys.data.record.Record;
import com.revolsys.filter.Filter;
import com.revolsys.filter.FilterUtil;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.parallel.channel.Channel;

public class PointRecordMap {

  private Comparator<Record> comparator;

  private Map<Point, List<Record>> objectMap = new HashMap<Point, List<Record>>();

  private int size = 0;

  private boolean removeEmptyLists;

  public PointRecordMap() {
  }

  public PointRecordMap(final Comparator<Record> comparator) {
    this.comparator = comparator;
  }

  /**
   * Add a {@link Point} {@link Record} to the list of objects at the given
   * coordinate.
   * 
   * @param pointObjects The map of point objects.
   * @param object The object to add.
   */
  public void add(final Record object) {
    final Point point = object.getGeometryValue();
    final Point coordinates = new PointDouble(point, 2);
    final List<Record> objects = getObjectInternal(coordinates);
    objects.add(object);
    if (comparator != null) {
      Collections.sort(objects, comparator);
    }
    size++;
  }

  public void clear() {
    size = 0;
    objectMap = new HashMap<Point, List<Record>>();
  }

  public boolean containsKey(final Point point) {
    final PointDouble coordinates = new PointDouble(point, 2);
    return objectMap.containsKey(coordinates);
  }

  public List<Record> getAll() {
    final List<Record> objects = new ArrayList<Record>();
    for (final List<Record> objectsAtPoint : objectMap.values()) {
      objects.addAll(objectsAtPoint);
    }
    return objects;
  }

  public Set<Point> getCoordinates() {
    return Collections.unmodifiableSet(objectMap.keySet());
  }

  public Record getFirstMatch(final Record object,
    final Filter<Record> filter) {
    final List<Record> objects = getObjects(object);
    for (final Record matchObject : objects) {
      if (filter.accept(matchObject)) {
        return matchObject;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public <V extends Record> V getFirstMatch(final Point point) {
    final List<Record> objects = getObjects(point);
    if (objects.isEmpty()) {
      return null;
    } else {
      return (V)objects.get(0);
    }

  }

  public List<Record> getMatches(final Record object,
    final Filter<Record> filter) {
    final List<Record> objects = getObjects(object);
    final List<Record> filteredObjects = FilterUtil.filter(objects, filter);
    return filteredObjects;
  }

  protected List<Record> getObjectInternal(final Point coordinates) {
    List<Record> objects = objectMap.get(coordinates);
    if (objects == null) {
      objects = new ArrayList<Record>(1);
      final Point indexCoordinates = new PointDouble(coordinates.getX(),
        coordinates.getY());
      objectMap.put(indexCoordinates, objects);
    }
    return objects;
  }

  public List<Record> getObjects(final Record object) {
    final Point point = object.getGeometryValue();
    final List<Record> objects = getObjects(point);
    return objects;
  }

  public List<Record> getObjects(final Point point) {
    final Point coordinates = new PointDouble(point, 2);
    final List<Record> objects = objectMap.get(coordinates);
    if (objects == null) {
      return Collections.emptyList();
    } else {
      return new ArrayList<Record>(objects);
    }
  }

  public void initialize(final Point point) {
    if (!isRemoveEmptyLists()) {
      final Point coordinates = new PointDouble(point, 2);
      getObjectInternal(coordinates);
    }
  }

  public boolean isRemoveEmptyLists() {
    return removeEmptyLists;
  }

  public void remove(final Record object) {
    final Geometry geometry = object.getGeometryValue();
    final Point coordinates = geometry.getPoint();
    final List<Record> objects = objectMap.get(coordinates);
    if (objects != null) {
      objects.remove(object);
      if (objects.isEmpty()) {
        if (isRemoveEmptyLists()) {
          objectMap.remove(coordinates);
        }
      } else if (comparator != null) {
        Collections.sort(objects, comparator);
      }
    }
    size--;
  }

  public void setRemoveEmptyLists(final boolean removeEmptyLists) {
    this.removeEmptyLists = removeEmptyLists;
  }

  public int size() {
    return size;
  }

  public void sort(final Record object) {
    if (comparator != null) {
      final Geometry geometry = object.getGeometryValue();
      final Point coordinate = geometry.getPoint();
      final List<Record> objects = objectMap.get(coordinate);
      if (objects != null) {
        Collections.sort(objects, comparator);
      }
    }
  }

  public void write(final Channel<Record> out) {
    if (out != null) {
      for (final Point coordinates : getCoordinates()) {
        final List<Record> objects = getObjects(coordinates);
        for (final Record object : objects) {
          out.write(object);
        }
      }
    }
  }
}
