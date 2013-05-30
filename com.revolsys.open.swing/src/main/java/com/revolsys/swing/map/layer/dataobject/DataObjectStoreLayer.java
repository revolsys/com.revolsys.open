package com.revolsys.swing.map.layer.dataobject;

import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingWorker;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.revolsys.gis.algorithm.index.DataObjectQuadTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.query.Query;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.swing.SwingWorkerManager;
import com.revolsys.transaction.InvokeMethodInTransaction;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

public class DataObjectStoreLayer extends AbstractDataObjectLayer {

  private BoundingBox boundingBox = new BoundingBox();

  private final Map<Object, DataObject> cachedObjects = new HashMap<Object, DataObject>();

  private final DataObjectStore dataStore;

  private DataObjectQuadTree index = new DataObjectQuadTree();

  private BoundingBox loadingBoundingBox = new BoundingBox();

  private SwingWorker<DataObjectQuadTree, Void> loadingWorker;

  private final Method saveChangesMethod;

  private final Object sync = new Object();

  private String typePath;

  public DataObjectStoreLayer(final DataObjectStore dataStore) {
    this.dataStore = dataStore;
    saveChangesMethod = ReflectionUtils.findMethod(getClass(),
      "transactionSaveChanges");
    saveChangesMethod.setAccessible(true);

  }

  public DataObjectStoreLayer(final DataObjectStore dataStore,
    final String typePath) {
    this(dataStore);
    setMetaData(dataStore.getMetaData(typePath));
    setTypePath(typePath);
  }

  @Override
  public DataObject getObjectById(Object id) {
    return getDataStore().load(getTypePath(), id);
  }

  @Override
  public void addEditingObject(final DataObject object) {
    final DataObject cachedObject = getCacheObject(object);
    if (cachedObject != null) {
      super.addEditingObject(cachedObject);
    }
  }

  @Override
  protected void addModifiedObject(final DataObject object) {
    final DataObject cacheObject = getCacheObject(object);
    if (cacheObject != null) {
      super.addModifiedObject(cacheObject);
    }
  }

  @Override
  protected void addSelectedObject(final DataObject object) {
    final DataObject cachedObject = getCacheObject(object);
    if (cachedObject != null) {
      super.addSelectedObject(object);
    }
  }

  /**
   * Remove any cached objects that are currently not used.
   */
  private void cleanCachedObjects() {
    synchronized (cachedObjects) {
      final Set<DataObject> objects = new HashSet<DataObject>();
      objects.addAll(getSelectedObjects());
      objects.addAll(getEditingObjects());
      objects.addAll(getModifiedObjects());
      cachedObjects.values().retainAll(objects);
    }
  }

  @Override
  protected void clearChanges() {
    super.clearChanges();
    cachedObjects.clear();
  }

  @Override
  public void clearEditingObjects() {
    synchronized (cachedObjects) {
      super.clearEditingObjects();
      cleanCachedObjects();
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
      super.clearSelectedObjects();
      cleanCachedObjects();
    }
  }

  protected LoadingWorker createLoadingWorker(final BoundingBox boundingBox) {
    return new LoadingWorker(this, boundingBox);
  }

  @Override
  protected boolean doSaveChanges() {

    return invokeInTransaction(saveChangesMethod);
  }

  @Override
  protected void fireObjectsChanged() {
    refresh();
    super.fireObjectsChanged();
  }

  @Override
  public BoundingBox getBoundingBox() {
    return getCoordinateSystem().getAreaBoundingBox();
  }

  protected DataObject getCacheObject(final DataObject object) {
    if (object == null) {
      return null;
    } else {
      final Object idValue = object.getIdValue();
      return getCacheObject(idValue, object);
    }
  }

  private DataObject getCacheObject(final Object id, final DataObject object) {
    if (object != null && isLayerObject(object)) {
      synchronized (cachedObjects) {
        if (cachedObjects.containsKey(id)) {
          return cachedObjects.get(id);
        } else {
          cachedObjects.put(id, object);
          return object;
        }
      }
    } else {
      return null;
    }
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

      final List<DataObject> objects = index.queryIntersects(polygon);
      return objects;
    }
  }

  @Override
  public DataObjectStore getDataStore() {
    return dataStore;
  }

  public BoundingBox getLoadingBoundingBox() {
    return loadingBoundingBox;
  }

  @Override
  public DataObjectMetaData getMetaData() {
    return dataStore.getMetaData(typePath);
  }

  protected List<DataObject> getObjects(final BoundingBox boundingBox) {
    final BoundingBox convertedBoundingBox = boundingBox.convert(getGeometryFactory());
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
  public int getRowCount(final Query query) {
    return dataStore.getRowCount(query);
  }

  public String getTypePath() {
    return typePath;
  }

  protected boolean invokeInTransaction(final Method saveChangesMethod) {
    final DataObjectStore dataStore = getDataStore();
    final PlatformTransactionManager transactionManager = dataStore.getTransactionManager();
    return (Boolean)InvokeMethodInTransaction.execute(transactionManager, this,
      saveChangesMethod);
  }

  @Override
  public boolean isLayerObject(final DataObject object) {
    if (object instanceof LayerDataObject) {
      final LayerDataObject layerDataObject = (LayerDataObject)object;
      if (layerDataObject.getLayer() == this) {
        return true;
      }
    }
    return false;
  }

  @Override
  public List<DataObject> query(final Query query) {
    query.setProperty("dataObjectFactory", this);
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
  public void refresh() {
    super.refresh();
    synchronized (sync) {
      if (loadingWorker != null) {
        loadingWorker.cancel(true);
      }

      boundingBox = new BoundingBox();
      loadingBoundingBox = boundingBox;
      index = new DataObjectQuadTree();
    }
  }

  protected void setIndex(final BoundingBox loadedBoundingBox,
    final DataObjectQuadTree index) {
    synchronized (sync) {
      if (loadedBoundingBox == loadingBoundingBox) {
        this.index = index;
        final List<DataObject> newObjects = getNewObjects();
        index.insert(newObjects);
        clearLoading(loadedBoundingBox);
      }
    }
    getPropertyChangeSupport().firePropertyChange("refresh", false, true);
  }

  @Override
  public void setSelectedObjects(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<DataObject> objects = getObjects(boundingBox);
      setSelectedObjects(objects);
    }
  }

  @Override
  public void setSelectedObjects(final Collection<DataObject> objects) {
    super.setSelectedObjects(objects);
    cleanCachedObjects();
  }

  public void setTypePath(final String typePath) {
    this.typePath = typePath;
    setName(PathUtil.getName(typePath));
    if (StringUtils.hasText(typePath)) {
      final DataObjectMetaData metaData = dataStore.getMetaData(typePath);
      if (metaData != null) {

        setMetaData(metaData);
        query = new Query(metaData);
        return;
      }
    }
    setMetaData(null);
    query = null;
  }

  protected boolean transactionSaveChanges() {
    final Writer<DataObject> writer = dataStore.createWriter();
    try {
      final Set<DataObject> deletedObjects = getDeletedObjects();

      for (final DataObject object : deletedObjects) {
        object.setState(DataObjectState.Deleted);
        writer.write(object);
      }
      unselectObjects(deletedObjects);

      for (final DataObject object : getModifiedObjects()) {
        writer.write(object);
      }

      final Collection<DataObject> newObjects = getNewObjects();
      for (final DataObject object : newObjects) {
        writer.write(object);
      }
    } finally {
      writer.close();
    }
    return true;
  }

  @Override
  public void unselectObjects(final Collection<? extends DataObject> objects) {
    super.unselectObjects(objects);
    cleanCachedObjects();
  }

}
