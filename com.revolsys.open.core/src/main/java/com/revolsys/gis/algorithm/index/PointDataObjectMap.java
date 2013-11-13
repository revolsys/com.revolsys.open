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
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.parallel.channel.Channel;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class PointDataObjectMap {

  private Comparator<DataObject> comparator;

  private Map<Coordinates, List<DataObject>> objectMap = new HashMap<Coordinates, List<DataObject>>();

  private int size = 0;

  private boolean removeEmptyLists;

  public PointDataObjectMap() {
  }

  public PointDataObjectMap(final Comparator<DataObject> comparator) {
    this.comparator = comparator;
  }

  /**
   * Add a {@link Point} {@link DataObject} to the list of objects at the given
   * coordinate.
   * 
   * @param pointObjects The map of point objects.
   * @param object The object to add.
   */
  public void add(final DataObject object) {
    final Point point = object.getGeometryValue();
    final Coordinates coordinates = CoordinatesUtil.get(point);
    List<DataObject> objects = objectMap.get(coordinates);
    if (objects == null) {
      objects = new ArrayList<DataObject>(1);
      final Coordinates indexCoordinates = new DoubleCoordinates(
        coordinates.getX(), coordinates.getY());
      objectMap.put(indexCoordinates, objects);
    }
    objects.add(object);
    if (comparator != null) {
      Collections.sort(objects, comparator);
    }
    size++;
  }

  public void clear() {
    size = 0;
    objectMap = new HashMap<Coordinates, List<DataObject>>();
  }

  public boolean containsKey(final Point point) {
    final Coordinates coordinates = CoordinatesUtil.get(point);
    return objectMap.containsKey(coordinates);
  }

  public List<DataObject> getAll() {
    final List<DataObject> objects = new ArrayList<DataObject>();
    for (final List<DataObject> objectsAtPoint : objectMap.values()) {
      objects.addAll(objectsAtPoint);
    }
    return objects;
  }

  public Set<Coordinates> getCoordinates() {
    return Collections.unmodifiableSet(objectMap.keySet());
  }

  public DataObject getFirstMatch(final DataObject object,
    final Filter<DataObject> filter) {
    final List<DataObject> objects = getObjects(object);
    for (final DataObject matchObject : objects) {
      if (filter.accept(matchObject)) {
        return matchObject;
      }
    }
    return null;
  }

  public List<DataObject> getMatches(final DataObject object,
    final Filter<DataObject> filter) {
    final List<DataObject> objects = getObjects(object);
    final List<DataObject> filteredObjects = FilterUtil.filter(objects, filter);
    return filteredObjects;
  }

  public List<DataObject> getObjects(final Coordinates coordinates) {
    final List<DataObject> objects = objectMap.get(coordinates);
    if (objects == null) {
      return Collections.emptyList();
    } else {
      return new ArrayList<DataObject>(objects);
    }
  }

  public List<DataObject> getObjects(final DataObject object) {
    final Point point = object.getGeometryValue();
    final List<DataObject> objects = getObjects(point);
    return objects;
  }

  public List<DataObject> getObjects(final Point point) {
    final Coordinates coordinates = CoordinatesUtil.get(point);
    final List<DataObject> objects = getObjects(coordinates);
    return objects;
  }

  public boolean isRemoveEmptyLists() {
    return removeEmptyLists;
  }

  public void remove(final DataObject object) {
    final Geometry geometry = object.getGeometryValue();
    final Coordinates coordinates = CoordinatesUtil.get(geometry);
    final List<DataObject> objects = objectMap.get(coordinates);
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

  public void sort(final DataObject object) {
    if (comparator != null) {
      final Geometry geometry = object.getGeometryValue();
      final Coordinate coordinate = geometry.getCoordinate();
      final List<DataObject> objects = objectMap.get(coordinate);
      if (objects != null) {
        Collections.sort(objects, comparator);
      }
    }
  }

  public void write(final Channel<DataObject> out) {
    if (out != null) {
      for (final Coordinates coordinates : getCoordinates()) {
        final List<DataObject> objects = getObjects(coordinates);
        for (final DataObject object : objects) {
          out.write(object);
        }
      }
    }
  }
}
