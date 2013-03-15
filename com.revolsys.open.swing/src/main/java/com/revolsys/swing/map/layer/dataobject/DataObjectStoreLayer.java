package com.revolsys.swing.map.layer.dataobject;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingWorker;

import org.springframework.util.StringUtils;

import com.revolsys.gis.algorithm.index.DataObjectQuadTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreFactoryRegistry;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Query;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Reader;
import com.revolsys.swing.SwingWorkerManager;
import com.revolsys.swing.map.layer.InvokeMethodLayerFactory;
import com.revolsys.swing.map.layer.LayerFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

public class DataObjectStoreLayer extends AbstractDataObjectLayer {

  public static final LayerFactory<DataObjectStoreLayer> FACTORY = new InvokeMethodLayerFactory<DataObjectStoreLayer>(
    "dataStore", "Data Store", DataObjectStoreLayer.class, "create");

  public static DataObjectStoreLayer create(Map<String, Object> properties) {
    @SuppressWarnings("unchecked")
    Map<String, Object> connectionProperties = (Map<String, Object>)properties.get("connectionProperties");
    if (connectionProperties == null) {
      throw new IllegalArgumentException(
        "A data store layer requires a connectionProperties entry with a url, username and password.");
    } else {
      DataObjectStore dataStore = DataObjectStoreFactoryRegistry.createDataObjectStore(connectionProperties);
      dataStore.initialize();
      DataObjectStoreLayer layer = new DataObjectStoreLayer(dataStore);
      layer.setProperties(properties);
      return layer;
    }
  }

  private final DataObjectStore dataStore;

  private final Object sync = new Object();

  private String typePath;

  private DataObjectQuadTree index = new DataObjectQuadTree();

  private BoundingBox boundingBox = new BoundingBox();

  private BoundingBox loadingBoundingBox = new BoundingBox();

  private SwingWorker<DataObjectQuadTree, Void> loadingWorker;

  private final Set<Object> selectedObjectIds = new LinkedHashSet<Object>();

  private final Set<Object> editingObjectIds = new LinkedHashSet<Object>();

  private final Set<Object> hiddenObjectIds = new LinkedHashSet<Object>();

  private final Map<Object, DataObject> cachedObjects = new HashMap<Object, DataObject>();

  public DataObjectStoreLayer(final DataObjectStore dataStore,
    final String typePath) {
    super(PathUtil.getName(typePath), dataStore.getMetaData(typePath)
      .getGeometryFactory());
    this.dataStore = dataStore;
    setTypePath(typePath);
  }

  public DataObjectStoreLayer(final DataObjectStore dataStore) {
    this.dataStore = dataStore;
  }

  public void setTypePath(String typePath) {
    this.typePath = typePath;
    if (StringUtils.hasText(typePath)) {
      DataObjectMetaData metaData = dataStore.getMetaData(typePath);
      if (metaData != null) {
        setName(PathUtil.getName(typePath));

        setMetaData(metaData);
        query = new Query(metaData);
        return;
      }
    }
    setName("Type not found " + typePath);
    setMetaData(null);
    query = null;
  }

  @Override
  public void addSelectedObjects(final Collection<? extends DataObject> objects) {
    for (final DataObject object : objects) {
      if (object.getMetaData() == getMetaData()) {
        final Object id = object.getIdValue();
        synchronized (cachedObjects) {
          if (!selectedObjectIds.contains(id)) {
            selectedObjectIds.add(id);
            cacheObject(id, object);
          }
        }
      }
    }
    getPropertyChangeSupport().firePropertyChange("selected", false, true);
  }

  private void cacheObject(final Object id, final DataObject object) {
    synchronized (cachedObjects) {
      if (!cachedObjects.containsKey(id)) {
        cachedObjects.put(id, object);
      }
    }
  }

  /**
   * Remove any cached objects that are currently not used.
   */
  private void cleanCachedObjects() {
    synchronized (cachedObjects) {
      final Set<Object> ids = new HashSet<Object>();
      ids.addAll(selectedObjectIds);
      ids.addAll(hiddenObjectIds);
      ids.addAll(editingObjectIds);
      cachedObjects.keySet().retainAll(ids);
    }
  }

  protected void clearLoading(final BoundingBox loadedBoundingBox) {
    synchronized (sync) {
      if (loadedBoundingBox == loadingBoundingBox) {
        final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
        propertyChangeSupport.firePropertyChange("loaded", false, true);
        boundingBox = loadingBoundingBox;
        loadingBoundingBox = new BoundingBox();
        loadingWorker = null;
      }

    }
  }

  @Override
  public void clearSelectedObjects() {
    synchronized (cachedObjects) {
      selectedObjectIds.clear();
      cleanCachedObjects();
    }
  }

  protected LoadingWorker createLoadingWorker(final BoundingBox boundingBox) {
    return new LoadingWorker(this, boundingBox);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return getCoordinateSystem().getAreaBoundingBox();
  }

  @Override
  public List<DataObject> getDataObjects(BoundingBox boundingBox) {
    if (boundingBox.isNull()) {
      return Collections.emptyList();
    } else {
      synchronized (sync) {
        boundingBox = new BoundingBox(boundingBox);
        boundingBox.expandPercent(0.2);
        if (!this.boundingBox.contains(boundingBox)
          && !loadingBoundingBox.contains(boundingBox)) {
          if (loadingWorker != null) {
            loadingWorker.cancel(true);
          }
          loadingBoundingBox = boundingBox;
          loadingWorker = createLoadingWorker(boundingBox);
          SwingWorkerManager.execute(loadingWorker);
        }
      }
      Polygon polygon = boundingBox.toPolygon();
      final GeometryFactory geometryFactory = getGeometryFactory();
      polygon = geometryFactory.project(polygon);

      return index.queryIntersects(polygon);
    }
  }

  public DataObjectStore getDataStore() {
    return dataStore;
  }

  @Override
  public Set<DataObject> getEditingObjects() {
    final Set<DataObject> objects = new HashSet<DataObject>();
    for (final Object id : editingObjectIds) {
      final DataObject object = cachedObjects.get(id);
      if (object != null) {
        objects.add(object);
      }
    }
    return objects;
  }

  @Override
  public Set<DataObject> getHiddenObjects() {
    final Set<DataObject> objects = new HashSet<DataObject>();
    for (final Object id : hiddenObjectIds) {
      final DataObject object = cachedObjects.get(id);
      if (object != null) {
        objects.add(object);
      }
    }
    return objects;
  }

  @Override
  public DataObjectMetaData getMetaData() {
    return dataStore.getMetaData(typePath);
  }

  @Override
  public int getRowCount(final Query query) {
    return dataStore.getRowCount(query);
  }

  @Override
  public List<DataObject> getSelectedObjects() {
    final List<DataObject> objects = new ArrayList<DataObject>();
    for (final Object id : selectedObjectIds) {
      final DataObject object = cachedObjects.get(id);
      if (object != null) {
        objects.add(object);
      }
    }
    return objects;
  }

  @Override
  public int getSelectionCount() {
    return selectedObjectIds.size();
  }

  public String getTypePath() {
    return typePath;
  }

  @Override
  public boolean isSelected(final DataObject object) {
    if (object != null) {
      if (object.getMetaData() == getMetaData()) {
        final Object id = object.getIdValue();
        return selectedObjectIds.contains(id);
      }
    }
    return false;
  }

  @Override
  public List<DataObject> query(final Query query) {
    final Reader<DataObject> reader = dataStore.query(query);
    try {
      final List<DataObject> readObjects = reader.read();
      final List<DataObject> objects = new ArrayList<DataObject>();
      for (final DataObject object : readObjects) {
        final Object id = object.getIdValue();
        synchronized (cachedObjects) {
          final DataObject cachedObject = cachedObjects.get(id);
          if (cachedObject == null) {
            objects.add(object);
          } else {
            objects.add(cachedObject);
          }
        }
      }
      return objects;
    } finally {
      reader.close();
    }
  }

  @Override
  public void removeSelectedObjects(
    final Collection<? extends DataObject> objects) {
    for (final DataObject object : objects) {
      if (object.getMetaData() == getMetaData()) {
        final Object id = object.getIdValue();
        synchronized (cachedObjects) {
          selectedObjectIds.remove(id);
        }
      }
    }
    cleanCachedObjects();
    getPropertyChangeSupport().firePropertyChange("selected", false, true);
  }

  @Override
  public void setEditingObjects(final Collection<? extends DataObject> objects) {
    for (final DataObject object : objects) {
      if (object.getMetaData() == getMetaData()) {
        final Object id = object.getIdValue();
        synchronized (cachedObjects) {
          if (!editingObjectIds.contains(id)) {
            editingObjectIds.add(id);
            cacheObject(id, object);
          }
        }
      }

    }
  }

  @Override
  public void setHiddenObjects(final Collection<? extends DataObject> objects) {
    for (final DataObject object : objects) {
      if (object.getMetaData() == getMetaData()) {
        final Object id = object.getIdValue();
        synchronized (cachedObjects) {
          if (!hiddenObjectIds.contains(id)) {
            hiddenObjectIds.add(id);
            cacheObject(id, object);
          }
        }
      }

    }
  }

  protected void setIndex(final BoundingBox loadedBoundingBox,
    final DataObjectQuadTree index) {
    synchronized (sync) {
      if (loadedBoundingBox == loadingBoundingBox) {
        this.index = index;
        clearLoading(loadedBoundingBox);
      }
    }
    getPropertyChangeSupport().firePropertyChange("refresh", false, true);
  }

  public BoundingBox getLoadingBoundingBox() {
    return loadingBoundingBox;
  }

  @Override
  public void setSelectedObjects(final BoundingBox boundingBox) {
    final List<DataObject> objects = getObjects(boundingBox);
    setSelectedObjects(objects);
  }

  protected List<DataObject> getObjects(BoundingBox boundingBox) {
    BoundingBox convertedBoundingBox = boundingBox.convert(getGeometryFactory());
    BoundingBox loadedBoundingBox;
    DataObjectQuadTree index;
    synchronized (sync) {
      loadedBoundingBox = this.boundingBox;
      index = this.index;
    }
    List<DataObject> queryObjects;
    if (loadedBoundingBox.contains(boundingBox)) {
      queryObjects = index.query(convertedBoundingBox);
    } else {
      queryObjects = getObjectsFromDataStore(convertedBoundingBox);
    }
    final List<DataObject> allObjects = new ArrayList<DataObject>();
    if (!queryObjects.isEmpty()) {
      final Polygon polygon = convertedBoundingBox.toPolygon();
      for (final DataObject object : queryObjects) {
        final Geometry geometry = object.getGeometryValue();
        if (geometry.intersects(polygon)) {
          allObjects.add(object);
        }
      }
    }
    return allObjects;
  }

  protected List<DataObject> getObjectsFromDataStore(
    final BoundingBox boundingBox) {
    List<DataObject> queryObjects;
    final Reader<DataObject> reader = dataStore.query(typePath, boundingBox);
    try {
      queryObjects = reader.read();
    } finally {
      reader.close();
    }
    return queryObjects;
  }

  @Override
  public void setSelectedObjects(final Collection<DataObject> objects) {
    selectedObjectIds.clear();
    for (final DataObject object : objects) {
      if (object != null && object.getMetaData() == getMetaData()) {
        final Object id = object.getIdValue();
        if (id != null) {
          synchronized (cachedObjects) {
            if (!selectedObjectIds.contains(id)) {
              selectedObjectIds.add(id);
              cacheObject(id, object);
            }
          }
        }
      }
    }
    getPropertyChangeSupport().firePropertyChange("selected", false, true);
  }

}
