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
import com.revolsys.swing.map.Viewport2D;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class DataObjectListLayer extends AbstractDataObjectLayer implements
  List<DataObject> {

  private DataObjectQuadTree index = new DataObjectQuadTree();

  private List<DataObject> objects = new ArrayList<DataObject>();

  public DataObjectListLayer(final DataObjectMetaData metaData) {
    super(metaData);
  }

  public DataObjectListLayer(final DataObjectMetaData metaData,
    final List<DataObject> objects) {
    this(metaData);
    addAllObjects(objects);
  }

  public DataObjectListLayer(final DataObjectMetaData metaData,
    final DataObject... objects) {
    this(metaData, Arrays.asList(objects));
  }

  public DataObjectListLayer(final String name,
    final GeometryFactory geometryFactory, final DataType geometryType) {
    super(name);
    final DataObjectMetaDataImpl metaData = createMetaData(name,
      geometryFactory, geometryType);
    setMetaData(metaData);
  }

  public static DataObjectMetaDataImpl createMetaData(final String name,
    final GeometryFactory geometryFactory, final DataType geometryType) {
    final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(name);
    metaData.addAttribute("GEOMETRY", geometryType, true);
    metaData.setGeometryFactory(geometryFactory);
    return metaData;
  }


  
  @Override
  public List<DataObject> getObjects(Geometry geometry, double distance) {
    geometry = getGeometryFactory().createGeometry(geometry);
    return index.queryDistance(geometry, distance);
  }
 

  public void addAllObjects(final Collection<? extends DataObject> objects) {
    List<DataObject> oldValue = new ArrayList<DataObject>(this.objects);
    addAllInternal(objects);
    getPropertyChangeSupport().firePropertyChange("objects", oldValue,
      new ArrayList<DataObject>(this.objects));
  }

  private void addAllInternal(final Collection<? extends DataObject> objects) {
    this.objects.addAll(objects);
    index.insert(objects);
  }

  public void addObject(final DataObject object) {
    List<DataObject> oldValue = new ArrayList<DataObject>(this.objects);
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
  public void deleteObjects(List<DataObject> objects) {
    List<DataObject> oldValue = new ArrayList<DataObject>(this.objects);
    this.objects.removeAll(objects);
    this.index.remove(objects);
    getPropertyChangeSupport().firePropertyChange("objects", oldValue,
      new ArrayList<DataObject>(this.objects));
  }

  public void deleteAll() {
    final List<DataObject> oldObjects = new ArrayList<DataObject>(objects);
    objects = new ArrayList<DataObject>();
    index = new DataObjectQuadTree();
    final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
    propertyChangeSupport.firePropertyChange("objects", oldObjects, objects);
  }

  @Override
  public List<DataObject> getDataObjects(final Viewport2D viewport) {
    return getDataObjects(viewport, viewport.getBoundingBox());
  }

  @Override
  public List<DataObject> getDataObjects(final Viewport2D viewport,
    final BoundingBox boundingBox) {
    GeometryFactory geometryFactory = getGeometryFactory();
    BoundingBox convertedBoundingBox = boundingBox.convert(geometryFactory);
    final List<DataObject> objects = index.query(convertedBoundingBox);
    return objects;
  }

  public List<DataObject> getObjects() {
    return new ArrayList<DataObject>(objects);
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

  public int getRowCount() {
    return objects.size();
  }

  public DataObject getObject(int index) {
    return objects.get(index);
  }

  @Override
  public int size() {
    return objects.size();
  }

  @Override
  public boolean isEmpty() {
    return objects.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return objects.contains(o);
  }

  @Override
  public Iterator<DataObject> iterator() {
    return objects.iterator();
  }

  @Override
  public Object[] toArray() {
    return objects.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return objects.toArray(a);
  }

  @Override
  public boolean add(DataObject object) {
    addObject(object);
    return true;
  }

  @Override
  public boolean remove(Object o) {
    if (o instanceof DataObject) {
      DataObject object = (DataObject)o;
      deleteObjects(object);
    }
    return true;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return objects.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends DataObject> objects) {
    addAllObjects(objects);
    return true;
  }

  @Override
  public boolean addAll(int index, Collection<? extends DataObject> c) {
    // TODO events
    return objects.addAll(index, c);
  }

  @Override
  public boolean removeAll(Collection<?> objects) {
    List<DataObject> dataObjects = new ArrayList<DataObject>();
    for (Object object : objects) {
      if (object instanceof DataObject) {
        DataObject dataObject = (DataObject)object;
        dataObjects.add(dataObject);
      }

    }
    deleteObjects(dataObjects);
    return true;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    // TODO events
    return objects.retainAll(c);
  }

  @Override
  public void clear() {
    deleteAll();
  }

  @Override
  public DataObject get(int index) {
    return objects.get(index);
  }

  @Override
  public DataObject set(int index, DataObject element) {
    // TODO events
    return objects.set(index, element);
  }

  @Override
  public void add(int index, DataObject element) {
    // TODO events
    objects.add(index, element);
  }

  @Override
  public DataObject remove(int index) {
    // TODO events
    return objects.remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return objects.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return objects.lastIndexOf(o);
  }

  @Override
  public ListIterator<DataObject> listIterator() {
    return objects.listIterator();
  }

  @Override
  public ListIterator<DataObject> listIterator(int index) {
    return objects.listIterator(index);
  }

  @Override
  public List<DataObject> subList(int fromIndex, int toIndex) {
    return objects.subList(fromIndex, toIndex);
  }
}
