package com.revolsys.swing.map.layer.dataobject;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JTable;

import com.revolsys.gis.algorithm.index.DataObjectQuadTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.swing.map.table.DataObjectLayerTablePanel;
import com.revolsys.swing.map.table.DataObjectListLayerTableModel;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectListLayer extends AbstractDataObjectLayer implements
  List<LayerDataObject> {

  public static DataObjectMetaDataImpl createMetaData(final String name,
    final GeometryFactory geometryFactory, final DataType geometryType) {
    final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(name);
    metaData.addAttribute("GEOMETRY", geometryType, true);
    metaData.setGeometryFactory(geometryFactory);
    return metaData;
  }

  private DataObjectQuadTree index = new DataObjectQuadTree();

  private List<LayerDataObject> objects = new ArrayList<LayerDataObject>();

  public DataObjectListLayer(final DataObjectMetaData metaData) {
    super(metaData);
    setEditable(true);
  }

  public DataObjectListLayer(final DataObjectMetaData metaData,
    final LayerDataObject... objects) {
    this(metaData, Arrays.asList(objects));
  }

  public DataObjectListLayer(final DataObjectMetaData metaData,
    final List<LayerDataObject> objects) {
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

  public void add(final DataObject object) {
    final LayerDataObject layerObject = createObject();
    if (layerObject != null) {
      layerObject.setValues(object);
    }
  }

  @Override
  public void add(final int index, final LayerDataObject element) {
    // TODO events
    objects.add(index, element);
  }

  @Override
  public boolean add(final LayerDataObject object) {
    addObject(object);
    return true;
  }

  @Override
  public boolean addAll(final Collection<? extends LayerDataObject> objects) {
    addAllObjects(objects);
    return true;
  }

  @Override
  public boolean addAll(final int index,
    final Collection<? extends LayerDataObject> c) {
    // TODO events
    return objects.addAll(index, c);
  }

  private void addAllInternal(
    final Collection<? extends LayerDataObject> objects) {
    this.objects.addAll(objects);
    index.insert(objects);
  }

  public void addAllObjects(final Collection<? extends LayerDataObject> objects) {
    final List<LayerDataObject> oldValue = new ArrayList<LayerDataObject>(
      this.objects);
    addAllInternal(objects);
    firePropertyChange("objects", oldValue, new ArrayList<LayerDataObject>(
      this.objects));
  }

  public void addObject(final LayerDataObject object) {
    final List<LayerDataObject> oldValue = new ArrayList<LayerDataObject>(
      this.objects);
    addObjectInternal(object);
    firePropertyChange("objects", oldValue, new ArrayList<LayerDataObject>(
      this.objects));
  }

  private void addObjectInternal(final LayerDataObject object) {
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

  @Override
  public LayerDataObject createObject() {
    final LayerDataObject object = super.createObject();
    if (object != null) {
      this.objects.add(object);
      fireObjectsChanged();
    }
    return object;
  }

  @Override
  public Component createTablePanel() {
    final JTable table = DataObjectListLayerTableModel.createTable(this);
    return new DataObjectLayerTablePanel(this, table);
  }

  public void deleteAll() {
    final List<LayerDataObject> oldObjects = new ArrayList<LayerDataObject>(
      objects);
    objects = new ArrayList<LayerDataObject>();
    index = new DataObjectQuadTree();
    firePropertyChange("objects", oldObjects, objects);
  }

  @Override
  public void deleteObjects(final Collection<? extends LayerDataObject> objects) {
    super.deleteObjects(objects);
    final List<LayerDataObject> oldValue = new ArrayList<LayerDataObject>(
      this.objects);
    this.objects.removeAll(objects);
    this.index.remove(objects);
    firePropertyChange("objects", oldValue, new ArrayList<LayerDataObject>(
      this.objects));
  }

  @Override
  public LayerDataObject get(final int index) {
    return objects.get(index);
  }

  @Override
  public List<LayerDataObject> getDataObjects(final BoundingBox boundingBox) {
    final double width = boundingBox.getWidth();
    final double height = boundingBox.getHeight();
    if (boundingBox.isNull() || width == 0 || height == 0) {
      return Collections.emptyList();
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final BoundingBox convertedBoundingBox = boundingBox.convert(geometryFactory);
      final List<LayerDataObject> objects = (List)index.query(convertedBoundingBox);
      return objects;
    }
  }

  @Override
  public LayerDataObject getObject(final int index) {
    return objects.get(index);
  }

  @Override
  public List<LayerDataObject> getObjects() {
    final ArrayList<LayerDataObject> returnObjects = new ArrayList<LayerDataObject>(
      objects);
    return returnObjects;
  }

  @Override
  public List<LayerDataObject> query(Geometry geometry,
    final double distance) {
    geometry = getGeometryFactory().createGeometry(geometry);
    return (List)index.queryDistance(geometry, distance);
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
  public Iterator<LayerDataObject> iterator() {
    return objects.iterator();
  }

  @Override
  public int lastIndexOf(final Object o) {
    return objects.lastIndexOf(o);
  }

  @Override
  public ListIterator<LayerDataObject> listIterator() {
    return objects.listIterator();
  }

  @Override
  public ListIterator<LayerDataObject> listIterator(final int index) {
    return objects.listIterator(index);
  }

  @Override
  public LayerDataObject remove(final int index) {
    // TODO events
    return objects.remove(index);
  }

  @Override
  public boolean remove(final Object o) {
    if (o instanceof LayerDataObject) {
      final LayerDataObject object = (LayerDataObject)o;
      deleteObjects(object);
    }
    return true;
  }

  @Override
  public boolean removeAll(final Collection<?> objects) {
    final List<LayerDataObject> dataObjects = new ArrayList<LayerDataObject>();
    for (final Object object : objects) {
      if (object instanceof LayerDataObject) {
        final LayerDataObject dataObject = (LayerDataObject)object;
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
  public LayerDataObject set(final int index, final LayerDataObject element) {
    // TODO events
    return objects.set(index, element);
  }

  public void setEditingObjects(final LayerDataObject... objects) {
    setEditingObjects(Arrays.asList(objects));
  }

  public void setObjects(final Collection<LayerDataObject> newObjects) {
    final List<LayerDataObject> oldObjects = objects;
    objects = new ArrayList<LayerDataObject>();
    index = new DataObjectQuadTree();
    addAllObjects(newObjects);
    firePropertyChange("objects", oldObjects, objects);
  }

  public void setObjects(final LayerDataObject... objects) {
    setObjects(Arrays.asList(objects));
  }

  @Override
  public int size() {
    return objects.size();
  }

  @Override
  public List<LayerDataObject> subList(final int fromIndex, final int toIndex) {
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
}
