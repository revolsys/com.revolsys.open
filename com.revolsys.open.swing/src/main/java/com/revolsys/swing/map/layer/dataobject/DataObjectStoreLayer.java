package com.revolsys.swing.map.layer.dataobject;

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

import org.slf4j.LoggerFactory;
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
import com.revolsys.transaction.TransactionUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

public class DataObjectStoreLayer extends AbstractDataObjectLayer {

  private BoundingBox boundingBox = new BoundingBox();

  private Map<Object, LayerDataObject> cachedObjects = new HashMap<Object, LayerDataObject>();

  private DataObjectStore dataStore;

  private DataObjectQuadTree index = new DataObjectQuadTree();

  private BoundingBox loadingBoundingBox = new BoundingBox();

  private SwingWorker<DataObjectQuadTree, Void> loadingWorker;

  private Method saveChangesMethod;

  private Method saveObjectChangesMethod;

  private Object sync = new Object();

  private String typePath;

  public DataObjectStoreLayer(final DataObjectStore dataStore) {
    this.dataStore = dataStore;
    saveChangesMethod = ReflectionUtils.findMethod(getClass(),
      "transactionSaveChanges");
    saveChangesMethod.setAccessible(true);

    saveObjectChangesMethod = ReflectionUtils.findMethod(getClass(),
      "transactionSaveObjectChanges", LayerDataObject.class);
    saveObjectChangesMethod.setAccessible(true);
  }

  public DataObjectStoreLayer(final DataObjectStore dataStore,
    final String typePath) {
    this(dataStore);
    setMetaData(dataStore.getMetaData(typePath));
    setTypePath(typePath);
  }

  @Override
  public void addEditingObject(final LayerDataObject object) {
    final LayerDataObject cachedObject = getCacheObject(object);
    if (cachedObject != null) {
      super.addEditingObject(cachedObject);
    }
  }

  @Override
  protected void addModifiedObject(final LayerDataObject object) {
    final LayerDataObject cacheObject = getCacheObject(object);
    if (cacheObject != null) {
      super.addModifiedObject(cacheObject);
    }
  }

  @Override
  protected void addSelectedObject(final LayerDataObject object) {
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
        firePropertyChange("loaded", false, true);
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
  public void delete() {
    final SwingWorker<DataObjectQuadTree, Void> loadingWorker = this.loadingWorker;
    this.sync = null;
    this.beanPropertyListener = null;
    this.boundingBox = null;
    this.cachedObjects = null;
    this.dataStore = null;
    this.index = null;
    this.loadingBoundingBox = null;
    this.loadingWorker = null;
    this.saveChangesMethod = null;
    this.saveObjectChangesMethod = null;
    this.typePath = null;
    super.delete();
    if (loadingWorker != null) {
      loadingWorker.cancel(true);
    }
  }

  @Override
  protected boolean doSaveChanges() {
    return invokeInTransaction(saveChangesMethod);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return getCoordinateSystem().getAreaBoundingBox();
  }

  protected LayerDataObject getCacheObject(final LayerDataObject object) {
    if (object == null) {
      return null;
    } else {
      final Object idValue = object.getIdValue();
      return getCacheObject(idValue, object);
    }
  }

  protected LayerDataObject getCacheObject(final Object id) {
    synchronized (cachedObjects) {
      if (id == null) {
        return null;
      } else {
        LayerDataObject object = cachedObjects.get(id);
        if (object == null) {
          object = getObjectById(id);
          if (object != null) {
            cachedObjects.put(id, object);
          }
        }
        return object;
      }
    }
  }

  private LayerDataObject getCacheObject(final Object id,
    final LayerDataObject object) {
    if (object != null && isLayerObject(object)) {
      if (object.getState() == DataObjectState.New) {
        return object;
      } else {
        synchronized (cachedObjects) {
          if (cachedObjects.containsKey(id)) {
            return cachedObjects.get(id);
          } else {
            cachedObjects.put(id, object);
            return object;
          }
        }
      }
    } else {
      return null;
    }
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  @Override
  public List<LayerDataObject> getDataObjects(final BoundingBox boundingBox) {
    if (boundingBox.isNull()) {
      return Collections.emptyList();
    } else if (sync == null) {
      return Collections.emptyList();
    } else {
      synchronized (sync) {
        final BoundingBox loadBoundingBox = boundingBox.expandPercent(0.2);
        if (!this.boundingBox.contains(boundingBox)
          && !this.loadingBoundingBox.contains(boundingBox)) {
          if (loadingWorker != null) {
            loadingWorker.cancel(true);
          }
          this.loadingBoundingBox = loadBoundingBox;
          loadingWorker = createLoadingWorker(loadBoundingBox);
          SwingWorkerManager.execute(loadingWorker);
        }
      }
      Polygon polygon = boundingBox.toPolygon();
      final GeometryFactory geometryFactory = getGeometryFactory();
      polygon = geometryFactory.project(polygon);

      final List objects = index.queryIntersects(polygon);
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

  @Override
  public LayerDataObject getObjectById(final Object id) {
    final DataObjectMetaData metaData = getMetaData();
    final String idAttributeName = metaData.getIdAttributeName();
    if (idAttributeName == null) {
      LoggerFactory.getLogger(getClass()).error(
        typePath + " does not have a primary key");
      return null;
    } else {
      final Query query = Query.equal(metaData, idAttributeName, id);
      query.setProperty("dataObjectFactory", this);
      final DataObjectStore dataStore = getDataStore();
      return (LayerDataObject)dataStore.queryFirst(query);
    }

  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  protected List<LayerDataObject> getObjects(final BoundingBox boundingBox) {
    final BoundingBox convertedBoundingBox = boundingBox.convert(getGeometryFactory());
    BoundingBox loadedBoundingBox;
    DataObjectQuadTree index;
    synchronized (sync) {
      loadedBoundingBox = this.boundingBox;
      index = this.index;
    }
    List<LayerDataObject> queryObjects;
    if (loadedBoundingBox.contains(boundingBox)) {
      queryObjects = (List)index.query(convertedBoundingBox);
    } else {
      queryObjects = getObjectsFromDataStore(convertedBoundingBox);
    }
    final List<LayerDataObject> allObjects = new ArrayList<LayerDataObject>();
    if (!queryObjects.isEmpty()) {
      final Polygon polygon = convertedBoundingBox.toPolygon();
      for (final LayerDataObject object : queryObjects) {
        final Geometry geometry = object.getGeometryValue();
        if (geometry.intersects(polygon)) {
          allObjects.add(object);
        }
      }
    }
    return allObjects;
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  protected List<LayerDataObject> getObjectsFromDataStore(
    final BoundingBox boundingBox) {
    List<LayerDataObject> queryObjects;
    final Reader reader = dataStore.query(typePath, boundingBox);
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

  protected boolean invokeInTransaction(final Method method,
    final Object... args) {
    final DataObjectStore dataStore = getDataStore();
    final PlatformTransactionManager transactionManager = dataStore.getTransactionManager();
    return (Boolean)TransactionUtils.invoke(transactionManager, this, method,
      args);
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

  public List<LayerDataObject> query(final Map<String, ? extends Object> filter) {
    final DataObjectMetaData metaData = getMetaData();
    final Query query = Query.and(metaData, filter);
    return query(query);
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  @Override
  public List<LayerDataObject> query(final Query query) {
    query.setProperty("dataObjectFactory", this);
    final Reader reader = dataStore.query(query);
    try {
      final List<LayerDataObject> readObjects = reader.read();
      final List<LayerDataObject> objects = new ArrayList<LayerDataObject>();
      for (final LayerDataObject object : readObjects) {
        final Object id = object.getIdValue();
        synchronized (cachedObjects) {
          final LayerDataObject cachedObject = cachedObjects.get(id);
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
    fireObjectsChanged();
  }

  @Override
  public boolean saveChanges(final LayerDataObject object) {
    return invokeInTransaction(saveObjectChangesMethod, object);
  }

  protected void setIndex(final BoundingBox loadedBoundingBox,
    final DataObjectQuadTree index) {
    if (sync != null) {
      synchronized (sync) {
        if (loadedBoundingBox == loadingBoundingBox) {
          this.index = index;
          final List<LayerDataObject> newObjects = getNewObjects();
          index.insert(newObjects);
          clearLoading(loadedBoundingBox);
        }
      }
      firePropertyChange("refresh", false, true);
    }
  }

  @Override
  public void setSelectedObjects(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerDataObject> objects = getObjects(boundingBox);
      setSelectedObjects(objects);
    }
  }

  @Override
  public void setSelectedObjects(final Collection<LayerDataObject> objects) {
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

  protected synchronized boolean transactionSaveChanges() {
    final Writer<DataObject> writer = dataStore.createWriter();
    try {
      final Set<LayerDataObject> deletedObjects = getDeletedObjects();

      for (final DataObject object : deletedObjects) {
        object.setState(DataObjectState.Deleted);
        writer.write(object);
      }
      unselectObjects(deletedObjects);

      for (final DataObject object : getModifiedObjects()) {
        writer.write(object);
      }

      final Collection<LayerDataObject> newObjects = getNewObjects();
      for (final DataObject object : newObjects) {
        writer.write(object);
      }
    } finally {
      writer.close();
    }
    return true;
  }

  protected synchronized boolean transactionSaveObjectChanges(
    final LayerDataObject object) {
    final Writer<DataObject> writer = dataStore.createWriter();
    try {
      if (super.isDeleted(object)) {
        removeDeletedObject(object);
        object.setState(DataObjectState.Deleted);
        writer.write(object);
      } else if (super.isModified(object)) {
        removeModifiedObject(object);
        writer.write(object);
      } else if (isNew(object)) {
        removeNewObject(object);
        writer.write(object);
      }
    } finally {
      writer.close();
    }
    return true;
  }

  @Override
  public void unselectObjects(
    final Collection<? extends LayerDataObject> objects) {
    super.unselectObjects(objects);
    cleanCachedObjects();
  }

}
