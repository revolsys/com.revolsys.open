package com.revolsys.swing.map.layer.dataobject;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.revolsys.gis.algorithm.index.DataObjectQuadTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.swing.map.Viewport2D;

public class DataObjectListLayer extends AbstractDataObjectLayer {

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

  public void addAllObjects(final Collection<DataObject> objects) {
    // TODO property change
    addAllInternal(objects);
  }

  private void addAllInternal(final Collection<DataObject> objects) {
    this.objects.addAll(objects);
    index.insert(objects);
  }

  public void addObject(final DataObject object) {
    // TODO events
    addObjectInternal(object);
  }

  private void addObjectInternal(final DataObject object) {
    if (!objects.contains(object)) {
      objects.add(object);
      index.insert(object);
    }
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
}
