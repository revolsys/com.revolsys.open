package com.revolsys.swing.map.layer.dataobject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.SwingWorker;

import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
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
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.spring.InvokeMethodAfterCommit;
import com.revolsys.swing.SwingWorkerManager;
import com.revolsys.swing.map.table.DataObjectLayerTableModel;
import com.revolsys.transaction.TransactionUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

public class DataObjectStoreLayer extends AbstractDataObjectLayer {

  private BoundingBox boundingBox = new BoundingBox();

  private final Map<String, LayerDataObject> cachedObjects = new HashMap<String, LayerDataObject>();

  private DataObjectStore dataStore;

  private DataObjectQuadTree index = new DataObjectQuadTree();

  private BoundingBox loadingBoundingBox = new BoundingBox();

  private SwingWorker<DataObjectQuadTree, Void> loadingWorker;

  private Method saveChangesMethod;

  private Method saveObjectChangesMethod;

  private final Object sync = new Object();

  private String typePath;

  private final Set<String> deletedObjectIds = new LinkedHashSet<String>();

  private final Set<String> formObjectIds = new LinkedHashSet<String>();

  public DataObjectStoreLayer(final DataObjectStore dataStore) {
    this.dataStore = dataStore;
    setType("dataStore");
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

  protected void addToIndex(final LayerDataObject object) {
    index.insert(object);
  }

  protected void cacheObjects(final Collection<? extends DataObject> objects) {
    for (final DataObject object : objects) {
      if (object instanceof LayerDataObject) {
        final LayerDataObject layerDataObject = (LayerDataObject)object;
        getCacheObject(layerDataObject);
      }
    }
  }

  /**
   * Remove any cached objects that are currently not used.
   */
  private void cleanCachedObjects() {
    synchronized (cachedObjects) {
      final Set<String> ids = new HashSet<String>();
      ids.addAll(deletedObjectIds);
      ids.addAll(formObjectIds);
      for (final LayerDataObject object : getSelectedRecords()) {
        final String id = getId(object);
        if (id != null) {
          ids.add(id);
        }
      }
      for (final LayerDataObject object : getModifiedRecords()) {
        final String id = getId(object);
        if (id != null) {
          ids.add(id);
        }
      }
      if (index != null) {
        for (final DataObject object : index.queryAll()) {
          final String id = getId((LayerDataObject)object);
          if (id != null) {
            ids.add(id);
          }
        }
      }
      cachedObjects.keySet().retainAll(ids);
    }
  }

  @Override
  protected void clearChanges() {
    super.clearChanges();
    deletedObjectIds.clear();
    cleanCachedObjects();
    for (final LayerDataObject object : cachedObjects.values()) {
      removeForm(object);
    }
    cachedObjects.clear();
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
  public void clearSelectedRecords() {
    synchronized (cachedObjects) {
      super.clearSelectedRecords();
      cleanCachedObjects();
    }
  }

  protected LoadingWorker createLoadingWorker(final BoundingBox boundingBox) {
    return new LoadingWorker(this, boundingBox);
  }

  @Override
  public void delete() {
    final SwingWorker<DataObjectQuadTree, Void> loadingWorker = this.loadingWorker;
    this.beanPropertyListener = null;
    this.boundingBox = null;
    this.cachedObjects.clear();
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
  protected void deleteObject(final LayerDataObject object) {
    if (isLayerObject(object)) {
      final LayerDataObject cacheObject = getCacheObject(object);
      final String id = getId(cacheObject);
      if (StringUtils.hasText(id)) {
        deletedObjectIds.add(id);
        deleteObject(cacheObject, true);
        removeFromIndex(object);
        removeFromIndex(cacheObject);
      } else {
        removeFromIndex(object);
        super.deleteObject(object);
      }
    }
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  @Override
  protected List<LayerDataObject> doQuery(final BoundingBox boundingBox) {
    if (boundingBox.isNull()) {
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

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  @Override
  public List<LayerDataObject> doQuery(final Geometry geometry,
    final double distance) {
    final boolean enabled = setEventsEnabled(false);
    try {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Geometry queryGeometry = geometryFactory.copy(geometry);
      BoundingBox boundingBox = BoundingBox.getBoundingBox(queryGeometry);
      boundingBox = boundingBox.expand(distance);
      if (this.boundingBox.contains(boundingBox)) {
        return (List)index.queryDistance(queryGeometry, distance);
      } else {
        final String typePath = getTypePath();
        final DataObjectStore dataStore = getDataStore();
        final Reader reader = dataStore.query(this, typePath, queryGeometry,
          distance);
        try {
          final List<LayerDataObject> readObjects = reader.read();
          final List<LayerDataObject> objects = getCachedObjects(readObjects);
          return objects;
        } finally {
          reader.close();
        }
      }
    } finally {
      setEventsEnabled(enabled);
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

  protected List<LayerDataObject> getCachedObjects(
    final List<LayerDataObject> readObjects) {
    final List<LayerDataObject> objects = new ArrayList<LayerDataObject>();
    for (final LayerDataObject object : readObjects) {
      final String id = getId(object);
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
  }

  protected LayerDataObject getCacheObject(final LayerDataObject object) {
    if (object == null) {
      return null;
    } else {
      final String id = getId(object);
      return getCacheObject(id, object);
    }
  }

  protected LayerDataObject getCacheObject(final String id) {
    synchronized (cachedObjects) {
      if (id == null) {
        return null;
      } else {
        LayerDataObject object = cachedObjects.get(id);
        if (object == null) {
          object = getRecordById(id);
          if (object != null) {
            cachedObjects.put(id, object);
          }
        }
        return object;
      }
    }
  }

  private LayerDataObject getCacheObject(final String id,
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

  @Override
  public DataObjectStore getDataStore() {
    return dataStore;
  }

  protected String getId(final LayerDataObject object) {
    if (isLayerObject(object)) {
      return StringConverterRegistry.toString(object.getIdValue());
    } else {
      return null;
    }
  }

  public BoundingBox getLoadingBoundingBox() {
    return loadingBoundingBox;
  }

  @Override
  public DataObjectMetaData getMetaData() {
    return dataStore.getMetaData(typePath);
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
      try {
        for (final LayerDataObject object : queryObjects) {
          final Geometry geometry = object.getGeometryValue();
          if (geometry.intersects(polygon)) {
            allObjects.add(object);
          }
        }
      } catch (final ClassCastException e) {
        LoggerFactory.getLogger(getClass()).error("error", e);
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
    final Reader reader = dataStore.query(this, typePath, boundingBox);
    try {
      queryObjects = reader.read();
    } finally {
      reader.close();
    }
    return queryObjects;
  }

  @Override
  public LayerDataObject getRecordById(final Object id) {
    final DataObjectMetaData metaData = getMetaData();
    final String idAttributeName = metaData.getIdAttributeName();
    if (idAttributeName == null) {
      LoggerFactory.getLogger(getClass()).error(
        typePath + " does not have a primary key");
      return null;
    } else {
      final String idString = StringConverterRegistry.toString(id);
      final LayerDataObject object = cachedObjects.get(idString);
      if (object == null) {
        final Query query = Query.equal(metaData, idAttributeName, id);
        query.setProperty("dataObjectFactory", this);
        final DataObjectStore dataStore = getDataStore();
        return (LayerDataObject)dataStore.queryFirst(query);
      } else {
        return object;
      }
    }

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
    final boolean enabled = setEventsEnabled(false);
    try {
      query.setProperty("dataObjectFactory", this);
      final Reader reader = dataStore.query(query);
      try {
        final List<LayerDataObject> readObjects = reader.read();
        final List<LayerDataObject> objects = getCachedObjects(readObjects);
        return objects;
      } finally {
        reader.close();
      }
    } finally {
      setEventsEnabled(enabled);
    }
  }

  @Override
  public void refresh() {
    super.refresh();
    synchronized (sync) {
      if (loadingWorker != null) {
        loadingWorker.cancel(true);
      }
      this.boundingBox = new BoundingBox();
      this.loadingBoundingBox = boundingBox;
      this.index = new DataObjectQuadTree();
    }
    fireRecordsChanged();
  }

  @Override
  protected void removeForm(final LayerDataObject object) {
    synchronized (formObjectIds) {
      final String id = getId(object);
      if (id != null) {
        formObjectIds.remove(id);
        cleanCachedObjects();
      }
      super.removeForm(object);
    }
  }

  protected void removeFromIndex(final LayerDataObject object) {
    index.remove(object);
  }

  @Override
  public void revertChanges(final LayerDataObject object) {
    final String id = getId(object);
    deletedObjectIds.remove(id);
    super.revertChanges(object);
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
          cacheObjects(index.queryAll());
          final List<LayerDataObject> newObjects = getNewRecords();
          index.insert(newObjects);
          clearLoading(loadedBoundingBox);
        }
      }
      firePropertyChange("refresh", false, true);
    }
  }

  @Override
  public void setSelectedRecords(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerDataObject> objects = getObjects(boundingBox);
      for (final Iterator<LayerDataObject> iterator = objects.iterator(); iterator.hasNext();) {
        final LayerDataObject layerDataObject = iterator.next();
        if (!isVisible(layerDataObject) || super.isDeleted(layerDataObject)) {
          iterator.remove();
        }
      }
      if (!objects.isEmpty()) {
        showRecordsTable(DataObjectLayerTableModel.MODE_SELECTED);
      }
      setSelectedRecords(objects);
    }
  }

  @Override
  public void setSelectedRecords(final Collection<LayerDataObject> objects) {
    super.setSelectedRecords(objects);
    cleanCachedObjects();
  }

  public void setTypePath(final String typePath) {
    this.typePath = typePath;
    if (!StringUtils.hasText(getName())) {
      setName(PathUtil.getName(typePath));
    }
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

  @Override
  public <V extends JComponent> V showForm(final LayerDataObject object) {
    synchronized (formObjectIds) {
      final String id = getId(object);
      if (id == null) {
        return null;
      } else {
        formObjectIds.add(id);
        final LayerDataObject cachedObject = getCacheObject(id, object);
        return super.showForm(cachedObject);
      }
    }
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    MapSerializerUtil.add(map, "typePath", typePath);
    return map;
  }

  protected synchronized boolean transactionSaveChanges() {
    final Writer<DataObject> writer = dataStore.createWriter();
    try {
      final Set<LayerDataObject> deletedObjects = getDeletedRecords();

      for (final DataObject object : deletedObjects) {
        object.setState(DataObjectState.Deleted);
        writer.write(object);
      }
      unselectRecords(deletedObjects);

      for (final DataObject object : getModifiedRecords()) {
        writer.write(object);
      }

      final Collection<LayerDataObject> newObjects = getNewRecords();
      for (final DataObject object : newObjects) {
        writer.write(object);
      }
    } finally {
      writer.close();
    }
    refresh();
    return true;
  }

  protected synchronized boolean transactionSaveObjectChanges(
    final LayerDataObject object) {
    final Writer<DataObject> writer = dataStore.createWriter();
    try {
      final String id = object.getString(getMetaData().getIdAttributeName());
      if (deletedObjectIds.contains(id) || super.isDeleted(object)) {
        removeDeletedObject(object);
        index.remove(object);
        object.setState(DataObjectState.Deleted);
        writer.write(object);
      } else if (super.isModified(object)) {
        removeModifiedObject(object);
        writer.write(object);
        index.insert(object);
      } else if (isNew(object)) {
        removeNewObject(object);
        index.insert(object);
        writer.write(object);
      }
    } finally {
      writer.close();
    }
    InvokeMethodAfterCommit.invoke(this, "refresh");
    return true;
  }

  @Override
  public void unselectRecords(
    final Collection<? extends LayerDataObject> objects) {
    super.unselectRecords(objects);
    cleanCachedObjects();
  }

  @Override
  protected void updateSpatialIndex(final LayerDataObject object,
    final Geometry oldGeometry) {
    if (oldGeometry != null) {
      final BoundingBox oldBoundingBox = BoundingBox.getBoundingBox(oldGeometry);
      if (index.remove(oldBoundingBox, object)) {
        addToIndex(object);
      }
    }

  }
}
