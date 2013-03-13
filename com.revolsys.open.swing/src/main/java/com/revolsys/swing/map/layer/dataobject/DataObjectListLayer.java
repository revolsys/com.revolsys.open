package com.revolsys.swing.map.layer.dataobject;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.revolsys.gis.algorithm.index.DataObjectQuadTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectListLayer extends AbstractDataObjectLayer implements
  List<DataObject> {

  public static DataObjectMetaDataImpl createMetaData(final String name,
    final GeometryFactory geometryFactory, final DataType geometryType) {
    final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(name);
    metaData.addAttribute("GEOMETRY", geometryType, true);
    metaData.setGeometryFactory(geometryFactory);
    return metaData;
  }

  private DataObjectQuadTree index = new DataObjectQuadTree();

  private List<DataObject> objects = new ArrayList<DataObject>();

  public DataObjectListLayer(final DataObjectMetaData metaData) {
    super(metaData);
  }

  public DataObjectListLayer(final DataObjectMetaData metaData,
    final DataObject... objects) {
    this(metaData, Arrays.asList(objects));
  }

  public DataObjectListLayer(final DataObjectMetaData metaData,
    final List<DataObject> objects) {
    this(metaData);
    addAllObjects(objects);
  }

  public DataObjectListLayer(final String name,
    final GeometryFactory geometryFactory, final DataType geometryType) {
    super(name);
    final DataObjectMetaDataImpl metaData = createMetaData(name,
      geometryFactory, geometryType);
    setMetaData(metaData);
  }

  @Override
  public boolean add(final DataObject object) {
    addObject(object);
    return true;
  }

  @Override
  public void add(final int index, final DataObject element) {
    // TODO events
    objects.add(index, element);
  }

  @Override
  public boolean addAll(final Collection<? extends DataObject> objects) {
    addAllObjects(objects);
    return true;
  }

  @Override
  public boolean addAll(final int index,
    final Collection<? extends DataObject> c) {
    // TODO events
    return objects.addAll(index, c);
  }

  private void addAllInternal(final Collection<? extends DataObject> objects) {
    this.objects.addAll(objects);
    index.insert(objects);
  }

  public void addAllObjects(final Collection<? extends DataObject> objects) {
    final List<DataObject> oldValue = new ArrayList<DataObject>(this.objects);
    addAllInternal(objects);
    getPropertyChangeSupport().firePropertyChange("objects", oldValue,
      new ArrayList<DataObject>(this.objects));
  }

  public void addObject(final DataObject object) {
    final List<DataObject> oldValue = new ArrayList<DataObject>(this.objects);
    addObjectInternal(object);
    getPropertyChangeSupport().firePropertyChange("objects", oldValue,
      new ArrayList<DataObject>(this.objects));
  }

  private void addObjectInternal(final DataObject object) {
    if (!objects.contains(object)) {
      objects.add(object);
      index.insert(object);
    }
  }

  @Override
  public void clear() {
    deleteAll();
  }

  @Override
  public boolean contains(final Object o) {
    return objects.contains(o);
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    return objects.containsAll(c);
  }

  public void deleteAll() {
    final List<DataObject> oldObjects = new ArrayList<DataObject>(objects);
    objects = new ArrayList<DataObject>();
    index = new DataObjectQuadTree();
    final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
    propertyChangeSupport.firePropertyChange("objects", oldObjects, objects);
  }

  @Override
  public void deleteObjects(final Collection<? extends DataObject> objects) {
    super.deleteObjects(objects);
    final List<DataObject> oldValue = new ArrayList<DataObject>(this.objects);
    this.objects.removeAll(objects);
    this.index.remove(objects);
    getPropertyChangeSupport().firePropertyChange("objects", oldValue,
      new ArrayList<DataObject>(this.objects));
  }

  @Override
  public DataObject get(final int index) {
    return objects.get(index);
  }

  @Override
  public List<DataObject> getDataObjects(final BoundingBox boundingBox) {
    final double width = boundingBox.getWidth();
    final double height = boundingBox.getHeight();
    if (boundingBox.isNull() || width == 0 || height == 0) {
      return Collections.emptyList();
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final BoundingBox convertedBoundingBox = boundingBox.convert(geometryFactory);
      final List<DataObject> objects = index.query(convertedBoundingBox);
      return objects;
    }
  }

  @Override
  public DataObject getObject(final int index) {
    return objects.get(index);
  }

  @Override
  public List<DataObject> getObjects() {
    return new ArrayList<DataObject>(objects);
  }

  @Override
  public List<DataObject> getObjects(Geometry geometry, final double distance) {
    geometry = getGeometryFactory().createGeometry(geometry);
    return index.queryDistance(geometry, distance);
  }

  @Override
  public int getRowCount() {
    return objects.size();
  }

  @Override
  public int indexOf(final Object o) {
    return objects.indexOf(o);
  }

  @Override
  public boolean isEmpty() {
    return objects.isEmpty();
  }

  @Override
  public Iterator<DataObject> iterator() {
    return objects.iterator();
  }

  @Override
  public int lastIndexOf(final Object o) {
    return objects.lastIndexOf(o);
  }

  @Override
  public ListIterator<DataObject> listIterator() {
    return objects.listIterator();
  }

  @Override
  public ListIterator<DataObject> listIterator(final int index) {
    return objects.listIterator(index);
  }

  @Override
  public DataObject remove(final int index) {
    // TODO events
    return objects.remove(index);
  }

  @Override
  public boolean remove(final Object o) {
    if (o instanceof DataObject) {
      final DataObject object = (DataObject)o;
      deleteObjects(object);
    }
    return true;
  }

  @Override
  public boolean removeAll(final Collection<?> objects) {
    final List<DataObject> dataObjects = new ArrayList<DataObject>();
    for (final Object object : objects) {
      if (object instanceof DataObject) {
        final DataObject dataObject = (DataObject)object;
        dataObjects.add(dataObject);
      }

    }
    deleteObjects(dataObjects);
    return true;
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    // TODO events
    return objects.retainAll(c);
  }

  @Override
  public DataObject set(final int index, final DataObject element) {
    // TODO events
    return objects.set(index, element);
  }

  public void setObjects(final Collection<DataObject> newObjects) {
    final List<DataObject> oldObjects = objects;
    objects = new ArrayList<DataObject>();
    index = new DataObjectQuadTree();
    addAllObjects(newObjects);
    getPropertyChangeSupport().firePropertyChange("objects", oldObjects,
      objects);
  }

  public void setObjects(final DataObject... objects) {
    setObjects(Arrays.asList(objects));
  }

  @Override
  public int size() {
    return objects.size();
  }

  @Override
  public List<DataObject> subList(final int fromIndex, final int toIndex) {
    return objects.subList(fromIndex, toIndex);
  }

  @Override
  public Object[] toArray() {
    return objects.toArray();
  }

  @Override
  public <T> T[] toArray(final T[] a) {
    return objects.toArray(a);
  }

  public void setEditingObjects(DataObject... objects) {
    setEditingObjects(Arrays.asList(objects));
  }
}
