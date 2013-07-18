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

  private List<LayerDataObject> records = new ArrayList<LayerDataObject>();

  public DataObjectListLayer() {
  }

  public DataObjectListLayer(final DataObjectMetaData metaData) {
    super(metaData);
    setEditable(true);
  }

  public DataObjectListLayer(final DataObjectMetaData metaData,
    final LayerDataObject... records) {
    this(metaData, Arrays.asList(records));
  }

  public DataObjectListLayer(final DataObjectMetaData metaData,
    final List<LayerDataObject> records) {
    this(metaData);
    addAllRecords(records);
  }

  public DataObjectListLayer(final String name,
    final GeometryFactory geometryFactory, final DataType geometryType) {
    super(name);
    final DataObjectMetaDataImpl metaData = createMetaData(name,
      geometryFactory, geometryType);
    setMetaData(metaData);
  }

  public void add(final DataObject object) {
    final LayerDataObject layerObject = createRecord();
    if (layerObject != null) {
      layerObject.setValues(object);
    }
  }

  @Override
  public void add(final int index, final LayerDataObject element) {
    // TODO events
    records.add(index, element);
  }

  @Override
  public boolean add(final LayerDataObject object) {
    addObject(object);
    return true;
  }

  @Override
  public boolean addAll(final Collection<? extends LayerDataObject> records) {
    addAllRecords(records);
    return true;
  }

  @Override
  public boolean addAll(final int index,
    final Collection<? extends LayerDataObject> c) {
    // TODO events
    return records.addAll(index, c);
  }

  private void addAllInternal(
    final Collection<? extends LayerDataObject> records) {
    this.records.addAll(records);
    index.insert(records);
  }

  public void addAllRecords(final Collection<? extends LayerDataObject> records) {
    final List<LayerDataObject> oldValue = new ArrayList<LayerDataObject>(
      this.records);
    addAllInternal(records);
    firePropertyChange("records", oldValue, new ArrayList<LayerDataObject>(
      this.records));
  }

  public void addObject(final LayerDataObject object) {
    final List<LayerDataObject> oldValue = new ArrayList<LayerDataObject>(
      this.records);
    addObjectInternal(object);
    firePropertyChange("records", oldValue, new ArrayList<LayerDataObject>(
      this.records));
  }

  private void addObjectInternal(final LayerDataObject object) {
    if (!records.contains(object)) {
      records.add(object);
      index.insert(object);
    }
  }

  @Override
  public void clear() {
    deleteAll();
  }

  @Override
  public boolean contains(final Object o) {
    return records.contains(o);
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    return records.containsAll(c);
  }

  @Override
  public LayerDataObject createRecord() {
    final LayerDataObject object = super.createRecord();
    if (object != null) {
      this.records.add(object);
      fireRecordsChanged();
    }
    return object;
  }

  @Override
  public Component createTablePanel() {
    final JTable table = DataObjectListLayerTableModel.createTable(this);
    return new DataObjectLayerTablePanel(this, table);
  }

  public void deleteAll() {
    final List<LayerDataObject> oldRecords = new ArrayList<LayerDataObject>(
      records);
    records = new ArrayList<LayerDataObject>();
    index = new DataObjectQuadTree();
    firePropertyChange("records", oldRecords, records);
  }

  @Override
  public void deleteRecords(final Collection<? extends LayerDataObject> records) {
    super.deleteRecords(records);
    final List<LayerDataObject> oldValue = new ArrayList<LayerDataObject>(
      this.records);
    this.records.removeAll(records);
    this.index.remove(records);
    firePropertyChange("records", oldValue, new ArrayList<LayerDataObject>(
      this.records));
  }

  @Override
  public LayerDataObject get(final int index) {
    return records.get(index);
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
      final List<LayerDataObject> records = (List)index.query(convertedBoundingBox);
      return records;
    }
  }

  @Override
  public LayerDataObject getRecord(final int index) {
    return records.get(index);
  }

  @Override
  public List<LayerDataObject> getRecords() {
    final ArrayList<LayerDataObject> returnRecords = new ArrayList<LayerDataObject>(
      records);
    return returnRecords;
  }

  @Override
  public int getRowCount() {
    return records.size();
  }

  @Override
  public int indexOf(final Object o) {
    return records.indexOf(o);
  }

  @Override
  public boolean isEmpty() {
    return records.isEmpty();
  }

  @Override
  public Iterator<LayerDataObject> iterator() {
    return records.iterator();
  }

  @Override
  public int lastIndexOf(final Object o) {
    return records.lastIndexOf(o);
  }

  @Override
  public ListIterator<LayerDataObject> listIterator() {
    return records.listIterator();
  }

  @Override
  public ListIterator<LayerDataObject> listIterator(final int index) {
    return records.listIterator(index);
  }

  @Override
  public List<LayerDataObject> query(Geometry geometry, final double distance) {
    geometry = getGeometryFactory().createGeometry(geometry);
    return (List)index.queryDistance(geometry, distance);
  }

  @Override
  public LayerDataObject remove(final int index) {
    // TODO events
    return records.remove(index);
  }

  @Override
  public boolean remove(final Object o) {
    if (o instanceof LayerDataObject) {
      final LayerDataObject object = (LayerDataObject)o;
      deleteRecords(object);
    }
    return true;
  }

  @Override
  public boolean removeAll(final Collection<?> records) {
    final List<LayerDataObject> deleteRecords = new ArrayList<LayerDataObject>();
    for (final Object object : records) {
      if (object instanceof LayerDataObject) {
        final LayerDataObject dataObject = (LayerDataObject)object;
        deleteRecords.add(dataObject);
      }

    }
    deleteRecords(deleteRecords);
    return true;
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    // TODO events
    return records.retainAll(c);
  }

  @Override
  public LayerDataObject set(final int index, final LayerDataObject element) {
    // TODO events
    return records.set(index, element);
  }

  public void setRecords(final Collection<LayerDataObject> records) {
    final List<LayerDataObject> oldRecords = this.records;
    this.records = new ArrayList<LayerDataObject>();
    index = new DataObjectQuadTree();
    addAllRecords(records);
    firePropertyChange("records", oldRecords, this.records);
  }

  public void setRecords(final LayerDataObject... records) {
    setRecords(Arrays.asList(records));
  }

  @Override
  public int size() {
    return records.size();
  }

  @Override
  public List<LayerDataObject> subList(final int fromIndex, final int toIndex) {
    return records.subList(fromIndex, toIndex);
  }

  @Override
  public Object[] toArray() {
    return records.toArray();
  }

  @Override
  public <T> T[] toArray(final T[] a) {
    return records.toArray(a);
  }
}
