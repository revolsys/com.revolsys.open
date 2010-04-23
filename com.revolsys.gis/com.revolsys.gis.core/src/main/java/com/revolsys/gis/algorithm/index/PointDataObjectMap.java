package com.revolsys.gis.algorithm.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.revolsys.filter.Filter;
import com.revolsys.filter.FilterUtil;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.parallel.channel.Channel;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class PointDataObjectMap {

  private Comparator<DataObject> comparator;

  private Map<Coordinate, List<DataObject>> objectMap = new HashMap<Coordinate, List<DataObject>>();

  private int size = 0;

  public PointDataObjectMap() {
  }

  public PointDataObjectMap(
    final Comparator<DataObject> comparator) {
    this.comparator = comparator;
  }

  /**
   * Add a {@link Point} {@link DataObject} to the list of objects at the given
   * coordinate.
   * 
   * @param pointObjects The map of point objects.
   * @param object The object to add.
   */
  public void add(
    final DataObject object) {
    final Point point = object.getGeometryValue();
    final Coordinate coordinate = point.getCoordinate();
    List<DataObject> objects = objectMap.get(coordinate);
    if (objects == null) {
      objects = new ArrayList<DataObject>(1);
      final Coordinate indexCoordinate = new Coordinate(coordinate.x,
        coordinate.y);
      objectMap.put(indexCoordinate, objects);
    }
    objects.add(object);
    if (comparator != null) {
      Collections.sort(objects, comparator);
    }
    size++;
  }

  public void clear() {
    size = 0;
    objectMap = new HashMap<Coordinate, List<DataObject>>();
  }

  public Set<Coordinate> getCoordinates() {
    return Collections.unmodifiableSet(objectMap.keySet());
  }

  public DataObject getFirstMatch(
    final DataObject object,
    final Filter<DataObject> filter) {
    final List<DataObject> objects = getObjects(object);
    for (final DataObject matchObject : objects) {
      if (filter.accept(matchObject)) {
        return matchObject;
      }
    }
    return null;
  }

  public List<DataObject> getMatches(
    final DataObject object,
    final Filter<DataObject> filter) {
    final List<DataObject> objects = getObjects(object);
    final List<DataObject> filteredObjects = FilterUtil.filter(objects, filter);
    return filteredObjects;
  }

  public List<DataObject> getObjects(
    final Coordinate coordinate) {
    final List<DataObject> objects = objectMap.get(coordinate);
    if (objects == null) {
      return Collections.emptyList();
    } else {
      return new ArrayList<DataObject>(objects);
    }
  }

  public List<DataObject> getObjects(
    final DataObject object) {
    final Point point = object.getGeometryValue();
    final List<DataObject> objects = getObjects(point);
    return objects;
  }

  public List<DataObject> getObjects(
    final Point point) {
    final Coordinate coordinate = point.getCoordinate();
    final List<DataObject> objects = getObjects(coordinate);
    return objects;
  }

  public void remove(
    final DataObject object) {
    final Geometry geometry = object.getGeometryValue();
    final Coordinate coordinate = geometry.getCoordinate();
    final List<DataObject> objects = objectMap.get(coordinate);
    if (objects != null) {
      objects.remove(object);
      if (objects.isEmpty()) {
        objectMap.remove(coordinate);
      } else if (comparator != null) {
        Collections.sort(objects, comparator);
      }
    }
    size--;
  }

  public int size() {
    return size;
  }

  public void sort(
    DataObject object) {
    if (comparator != null) {
      final Geometry geometry = object.getGeometryValue();
      final Coordinate coordinate = geometry.getCoordinate();
      final List<DataObject> objects = objectMap.get(coordinate);
      if (objects != null) {
        Collections.sort(objects, comparator);
      }
    }
  }

  public void write(
    final Channel<DataObject> out) {
    if (out != null) {
      for (final Coordinate coordinate : getCoordinates()) {
        final List<DataObject> objects = getObjects(coordinate);
        for (final DataObject object : objects) {
          out.write(object);
        }
      }
    }
  }
}
