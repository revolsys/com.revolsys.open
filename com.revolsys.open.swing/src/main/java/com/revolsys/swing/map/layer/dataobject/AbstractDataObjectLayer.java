package com.revolsys.swing.map.layer.dataobject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.slf4j.LoggerFactory;

import com.revolsys.beans.InvokeMethodCallable;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Query;
import com.revolsys.swing.SwingWorkerManager;
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

  private Set<Object> deletedObjectIds = new LinkedHashSet<Object>();

  private Set<DataObject> newObjects = new LinkedHashSet<DataObject>();

  private Map<Object, DataObject> modifiedObjects = new LinkedHashMap<Object, DataObject>();

  private boolean canAddObjects = true;

  private boolean canEditObjects = true;

  private boolean canDeleteObjects = true;

  private BoundingBox boundingBox = new BoundingBox();

  protected Query query;

  private final Object editSync = new Object();

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
    fireSelected();
  }

  @Override
  public void addSelectedObjects(final DataObject... objects) {
    addSelectedObjects(Arrays.asList(objects));
  }

  public void cancelChanges() {
    synchronized (editSync) {
      internalCancelChanges();
      fireObjectsChanged();
    }
  }

  protected void internalCancelChanges() {
    clearChanges();
  }

  protected void clearChanges() {
    newObjects = new LinkedHashSet<DataObject>();
    modifiedObjects = new LinkedHashMap<Object, DataObject>();
    deletedObjectIds = new LinkedHashSet<Object>();
    hiddenObjects.clear();
    editingObjects.clear();
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
  public void clearSelectedObjects() {
    selectedObjects = new LinkedHashSet<DataObject>();
    getPropertyChangeSupport().firePropertyChange("selected", true, false);
  }

  @Override
  public DataObject createObject() {
    if (!isReadOnly() && isEditable() && isCanAddObjects()) {
      final DataObjectMetaData metaData = getMetaData();
      final DataObjectStore dataStore = getDataStore();
      DataObject object;
      if (dataStore == null) {
        object = new ArrayDataObject(metaData);
      } else {
        object = dataStore.create(metaData);
      }
      newObjects.add(object);
      return object;
    } else {
      return null;
    }
  }

  @Override
  public void deleteObjects(final Collection<? extends DataObject> objects) {
    synchronized (editSync) {
      unselectObjects(objects);
      for (final DataObject object : objects) {
        final DataObjectMetaData metaData = getMetaData();
        if (object.getMetaData() == metaData) {
          if (newObjects.contains(object)) {
            newObjects.remove(object);
          } else {
            final Object id = object.getIdValue();
            modifiedObjects.remove(id);
            deletedObjectIds.add(id);
            hideObject(object);
          }
        }
      }
    }
    fireObjectsChanged();
  }

  protected void fireObjectsChanged() {
    getPropertyChangeSupport().firePropertyChange("objectsChanged", false, true);
  }

  protected void hideObject(DataObject object) {
    hiddenObjects.add(object);
  }

  protected void showObject(DataObject object) {
    hiddenObjects.remove(object);
  }

  @Override
  public void deleteObjects(final DataObject... objects) {
    deleteObjects(Arrays.asList(objects));
  }

  protected void fireSelected() {
    final boolean selected = !selectedObjects.isEmpty();
    getPropertyChangeSupport().firePropertyChange("selected", !selected,
      selected);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  public CoordinateSystem getCoordinateSystem() {
    return getGeometryFactory().getCoordinateSystem();
  }

  @Override
  public List<DataObject> getDataObjects(final BoundingBox boundingBox) {
    return Collections.emptyList();
  }

  @Override
  public DataObjectStore getDataStore() {
    return getMetaData().getDataObjectStore();
  }

  public Set<Object> getDeletedObjectIds() {
    return deletedObjectIds;
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

  public Map<Object, DataObject> getModifiedObjects() {
    return new LinkedHashMap<Object, DataObject>(modifiedObjects);
  }

  public Set<DataObject> getNewObjects() {
    return newObjects;
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
  public Query getQuery() {
    if (query == null) {
      return null;
    } else {
      return query.clone();
    }
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
  public BoundingBox getSelectedBoundingBox() {
    BoundingBox boundingBox = super.getSelectedBoundingBox();
    for (final DataObject object : getSelectedObjects()) {
      final Geometry geometry = object.getGeometryValue();
      boundingBox = boundingBox.expandToInclude(geometry);
    }
    return boundingBox;
  }

  @Override
  public List<DataObject> getSelectedObjects() {
    return new ArrayList<DataObject>(selectedObjects);
  }

  @Override
  public int getSelectionCount() {
    return selectedObjects.size();
  }

  private boolean hasChanges() {
    synchronized (editSync) {
      if (!newObjects.isEmpty()) {
        return true;
      } else if (!modifiedObjects.isEmpty()) {
        return true;
      } else if (!deletedObjectIds.isEmpty()) {
        return true;
      } else {
        return false;
      }
    }
  }

  @Override
  public boolean isCanAddObjects() {
    return !isReadOnly() && isEditable() && canAddObjects;
  }

  @Override
  public boolean isCanDeleteObjects() {
    return !isReadOnly() && isEditable() && canDeleteObjects;
  }

  @Override
  public boolean isCanEditObjects() {
    return !isReadOnly() && isEditable() && canEditObjects;
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
  public boolean isVisible(final DataObject object) {
    if (isVisible()) {
      final AbstractDataObjectLayerRenderer renderer = getRenderer();
      if (renderer.isVisible(object)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public List<DataObject> query(final Query query) {
    throw new UnsupportedOperationException("Query not currently supported");
  }

  @Override
  public void unselectObjects(final Collection<? extends DataObject> objects) {
    selectedObjects.removeAll(objects);
    fireSelected();
  }

  @Override
  public void unelectObjects(final DataObject... objects) {
    unselectObjects(Arrays.asList(objects));
  }

  public boolean saveChanges() {
    synchronized (editSync) {
      boolean saved = internalSaveChanges();
      if (saved) {
        clearChanges();
      }
      fireObjectsChanged();
      return saved;
    }
  }

  protected boolean internalSaveChanges() {
    return true;
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public void setCanAddObjects(final boolean canAddObjects) {
    this.canAddObjects = canAddObjects;
    getPropertyChangeSupport().firePropertyChange("canAddObjects",
      !isCanAddObjects(), isCanAddObjects());
  }

  public void setCanDeleteObjects(final boolean canDeleteObjects) {
    this.canDeleteObjects = canDeleteObjects;
    getPropertyChangeSupport().firePropertyChange("canDeleteObjects",
      !isCanDeleteObjects(), isCanDeleteObjects());
  }

  public void setCanEditObjects(final boolean canEditObjects) {
    this.canEditObjects = canEditObjects;
    getPropertyChangeSupport().firePropertyChange("canEditObjects",
      !isCanEditObjects(), isCanEditObjects());
  }

  @Override
  public void setEditable(final boolean editable) {
    if (SwingUtilities.isEventDispatchThread()) {
      SwingWorkerManager.execute("Set editable", this, "setEditable", editable);
    } else {
      synchronized (editSync) {
        if (editable == false) {
          if (hasChanges()) {
            final int result = InvokeMethodCallable.invokeAndWait(
              JOptionPane.class,
              "showConfirmDialog",
              JOptionPane.getRootFrame(),
              "The layer has unsaved changes. Click Yes to save changes. Click No to discard changes. Click Cancel to continue editing.",
              "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION);

            if (result == JOptionPane.YES_OPTION) {
              if (!saveChanges()) {
                return;
              }
            } else if (result == JOptionPane.NO_OPTION) {
              cancelChanges();
            } else {
              // Don't allow state change if cancelled
              return;
            }

          }
        }
        super.setEditable(editable);
        setCanAddObjects(canAddObjects);
        setCanDeleteObjects(canDeleteObjects);
        setCanEditObjects(canEditObjects);
      }
    }
  }

  @Override
  public void setEditingObjects(final BoundingBox boundingBox) {
    final List<DataObject> objects = getDataObjects(boundingBox);
    setEditingObjects(objects);
  }

  @Override
  public void setEditingObjects(
    final Collection<? extends DataObject> editingObjects) {
    this.editingObjects = new LinkedHashSet<DataObject>(editingObjects);
  }

  @Override
  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    super.setGeometryFactory(geometryFactory);
    if (geometryFactory != null && boundingBox.isNull()) {
      boundingBox = geometryFactory.getCoordinateSystem().getAreaBoundingBox();
    }
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
    if (metaData.getGeometryAttributeIndex() == -1) {
      setSelectSupported(false);
    }
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

  public void setQuery(final Query query) {
    final Query oldValue = this.query;
    this.query = query;
    getPropertyChangeSupport().firePropertyChange("query", oldValue, query);
  }

  @Override
  public void setRenderer(final LayerRenderer<? extends Layer> renderer) {
    super.setRenderer(renderer);
  }

  @Override
  public void setSelectedObjects(final BoundingBox boundingBox) {
    final List<DataObject> objects = getDataObjects(boundingBox);
    setSelectedObjects(objects);
  }

  @Override
  public void setSelectedObjects(final Collection<DataObject> selectedObjects) {
    this.selectedObjects = new LinkedHashSet<DataObject>(selectedObjects);
    fireSelected();

  }

  @Override
  public void setSelectedObjects(final DataObject... selectedObjects) {
    setSelectedObjects(Arrays.asList(selectedObjects));
  }

  @Override
  public void setSelectedObjectsById(final Object id) {
    final DataObjectMetaData metaData = getMetaData();
    final String idAttributeName = metaData.getIdAttributeName();
    if (idAttributeName == null) {
      setSelectedObjects();
    } else {
      final Query query = new Query(metaData);
      query.addFilter(idAttributeName, id);
      final List<DataObject> objects = query(query);
      setSelectedObjects(objects);
    }
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
}
