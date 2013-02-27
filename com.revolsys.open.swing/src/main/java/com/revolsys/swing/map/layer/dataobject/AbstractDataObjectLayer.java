package com.revolsys.swing.map.layer.dataobject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Query;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.renderer.AbstractDataObjectLayerRenderer;
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractDataObjectLayer extends AbstractLayer implements
  DataObjectLayer {

  private DataObjectMetaData metaData;

  private Set<DataObject> selectedObjects = new LinkedHashSet<DataObject>();

  private Set<DataObject> editingObjects = new LinkedHashSet<DataObject>();

  private Set<DataObject> hiddenObjects = new LinkedHashSet<DataObject>();

  public AbstractDataObjectLayer() {
    this("");
  }

  public AbstractDataObjectLayer(final DataObjectMetaData metaData) {
    this(metaData.getTypeName());
    setMetaData(metaData);
  }

  public AbstractDataObjectLayer(final String name) {
    this(name, GeometryFactory.getFactory(4326));
    setReadOnly(false);
    setSelectSupported(true);
    setQuerySupported(true);
    setRenderer(new GeometryStyleRenderer(this));
  }

  public AbstractDataObjectLayer(final String name,
    final GeometryFactory geometryFactory) {
    super(name);
    setGeometryFactory(geometryFactory);
  }

  @Override
  public void addSelectedObjects(final Collection<? extends DataObject> objects) {
    selectedObjects.addAll(objects);
    getPropertyChangeSupport().firePropertyChange("selected", false, true);
  }

  @Override
  public BoundingBox getSelectedBoundingBox() {
    BoundingBox boundingBox = super.getSelectedBoundingBox();
    for (DataObject object : getSelectedObjects()) {
      Geometry geometry = object.getGeometryValue();
      boundingBox.expandToInclude(geometry);
    }
    return boundingBox;
  }

  @Override
  public boolean isVisible(DataObject object) {
    if (isVisible()) {
      AbstractDataObjectLayerRenderer renderer = getRenderer();
      if (renderer.isVisible(object)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void addSelectedObjects(final DataObject... objects) {
    addSelectedObjects(Arrays.asList(objects));
  }

  @Override
  public void clearEditingObjects() {
    this.editingObjects.clear();
  }

  @Override
  public void clearHiddenObjects() {
    this.hiddenObjects.clear();
  }

  @Override
  public void clearSelection() {
    selectedObjects = new LinkedHashSet<DataObject>();
    getPropertyChangeSupport().firePropertyChange("selected", false, true);
  }

  @Override
  public void deleteObjects(final Collection<? extends DataObject> objects) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteObjects(final DataObject... objects) {
    deleteObjects(Arrays.asList(objects));
  }

  public CoordinateSystem getCoordinateSystem() {
    return getGeometryFactory().getCoordinateSystem();
  }

  @Override
  public List<DataObject> getDataObjects(final BoundingBox boundingBox) {
    return Collections.emptyList();
  }

  @Override
  public Set<DataObject> getEditingObjects() {
    return editingObjects;
  }

  @Override
  public Set<DataObject> getHiddenObjects() {
    return hiddenObjects;
  }

  @Override
  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  @Override
  public DataObject getObject(final int row) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<DataObject> getObjects() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<DataObject> getObjects(final Geometry geometry,
    final double distance) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getRowCount() {
    final DataObjectMetaData metaData = getMetaData();
    final Query query = new Query(metaData);
    return getRowCount(query);
  }

  @Override
  public int getRowCount(final Query query) {
    LoggerFactory.getLogger(getClass()).error("Get row count not implemented");
    return 0;
  }

  @Override
  public List<DataObject> getSelectedObjects() {
    return new ArrayList<DataObject>(selectedObjects);
  }

  @Override
  public int getSelectionCount() {
    return selectedObjects.size();
  }

  @Override
  public boolean isSelected(final DataObject object) {
    if (object == null) {
      return false;
    } else {
      return selectedObjects.contains(object);
    }
  }

  @Override
  public List<DataObject> query(final Query query) {
    throw new UnsupportedOperationException("Query not currently supported");
  }

  @Override
  public void removeSelectedObjects(
    final Collection<? extends DataObject> objects) {
    selectedObjects.removeAll(objects);
  }

  @Override
  public void removeSelectedObjects(final DataObject... objects) {
    removeSelectedObjects(Arrays.asList(objects));
    getPropertyChangeSupport().firePropertyChange("selected", false, true);
  }

  @Override
  public void setEditingObjects(
    final Collection<? extends DataObject> editingObjects) {
    this.editingObjects = new LinkedHashSet<DataObject>(editingObjects);
  }

  @Override
  public void setHiddenObjects(
    final Collection<? extends DataObject> hiddenObjects) {
    this.hiddenObjects = new LinkedHashSet<DataObject>(hiddenObjects);
  }

  @Override
  public void setHiddenObjects(final DataObject... hiddenObjects) {
    setHiddenObjects(Arrays.asList(hiddenObjects));
  }

  protected void setMetaData(final DataObjectMetaData metaData) {
    this.metaData = metaData;
    setGeometryFactory(metaData.getGeometryFactory());
  }

  @Override
  public void setProperty(final String name, final Object value) {
    if ("style".equals(name)) {
      if (value instanceof Map) {
        @SuppressWarnings("unchecked")
        final Map<String, Object> style = (Map<String, Object>)value;
        final LayerRenderer<DataObjectLayer> renderer = AbstractDataObjectLayerRenderer.getRenderer(
          this, style);
        if (renderer != null) {
          setRenderer(renderer);
        }
      }
    } else {
      super.setProperty(name, value);
    }
  }

  @Override
  public void setRenderer(final LayerRenderer<? extends Layer> renderer) {
    super.setRenderer(renderer);
  }

  @Override
  public void setSelectedObjects(final Collection<DataObject> selectedObjects) {
    this.selectedObjects = new LinkedHashSet<DataObject>();
    getPropertyChangeSupport().firePropertyChange("selected", false, true);
  }

  @Override
  public void setSelectedObjects(final DataObject... selectedObjects) {
    setSelectedObjects(Arrays.asList(selectedObjects));
  }

  @Override
  public void setSelectedObjects(BoundingBox boundingBox) {
    List<DataObject> objects = getDataObjects(boundingBox);
    setSelectedObjects(objects);
  }

  @Override
  public int setSelectedWithinDistance(final boolean selected,
    final Geometry geometry, final int distance) {
    final List<DataObject> objects = getObjects(geometry, distance);
    if (selected) {
      selectedObjects.addAll(objects);
    } else {
      selectedObjects.removeAll(objects);
    }
    return objects.size();
  }

  @Override
  public void setSelectedObjectsById(Object id) {
    DataObjectMetaData metaData = getMetaData();
    String idAttributeName = metaData.getIdAttributeName();
    if (idAttributeName == null) {
      setSelectedObjects();
    } else {
      Query query = new Query(metaData);
      query.addFilter(idAttributeName, id);
      List<DataObject> objects = query(query);
      setSelectedObjects(objects);
    }
  }
}
