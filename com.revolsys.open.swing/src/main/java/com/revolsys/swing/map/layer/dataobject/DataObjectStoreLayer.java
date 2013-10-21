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
import com.revolsys.gis.data.io.DataObjectStoreFactoryRegistry;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.query.Query;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.io.datastore.DataObjectStoreConnectionManager;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.spring.InvokeMethodAfterCommit;
import com.revolsys.swing.map.layer.InvokeMethodMapObjectFactory;
import com.revolsys.swing.map.layer.dataobject.table.model.DataObjectLayerTableModel;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.transaction.TransactionUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

public class DataObjectStoreLayer extends AbstractDataObjectLayer {

  public static final MapObjectFactory FACTORY = new InvokeMethodMapObjectFactory(
    "dataStore", "Data Store", DataObjectStoreLayer.class, "create");

  public static DataObjectStoreLayer create(final Map<String, Object> properties) {
    return new DataObjectStoreLayer(properties);
  }

  private BoundingBox boundingBox = new BoundingBox();

  private final Map<String, LayerDataObject> cachedRecords = new HashMap<String, LayerDataObject>();

  private DataObjectStore dataStore;

  private BoundingBox loadingBoundingBox = new BoundingBox();

  private SwingWorker<DataObjectQuadTree, Void> loadingWorker;

  private Method saveChangesMethod;

  private Method saveObjectChangesMethod;

  private final Object sync = new Object();

  private String typePath;

  private final Set<String> deletedObjectIds = new LinkedHashSet<String>();

  private final Set<String> formObjectIds = new LinkedHashSet<String>();

  public DataObjectStoreLayer(final DataObjectStore dataStore,
    final String typePath, final boolean exists) {
    this.dataStore = dataStore;
    setExists(exists);
    setType("dataStore");
    this.saveChangesMethod = ReflectionUtils.findMethod(getClass(),
      "transactionSaveChanges");
    this.saveChangesMethod.setAccessible(true);

    this.saveObjectChangesMethod = ReflectionUtils.findMethod(getClass(),
      "transactionSaveObjectChanges", LayerDataObject.class);
    this.saveObjectChangesMethod.setAccessible(true);
    setMetaData(dataStore.getMetaData(typePath));
    setTypePath(typePath);
  }

  public DataObjectStoreLayer(final Map<String, ? extends Object> properties) {
    super(properties);
    setType("dataStore");
    this.saveChangesMethod = ReflectionUtils.findMethod(getClass(),
      "transactionSaveChanges");
    this.saveChangesMethod.setAccessible(true);

    this.saveObjectChangesMethod = ReflectionUtils.findMethod(getClass(),
      "transactionSaveObjectChanges", LayerDataObject.class);
    this.saveObjectChangesMethod.setAccessible(true);
  }

  @Override
  protected void addModifiedObject(final LayerDataObject object) {
    final LayerDataObject cacheObject = getCacheRecord(object);
    if (cacheObject != null) {
      super.addModifiedObject(cacheObject);
    }
  }

  @Override
  protected void addSelectedRecord(final LayerDataObject object) {
    final DataObject cachedObject = getCacheRecord(object);
    if (cachedObject != null) {
      super.addSelectedRecord(object);
    }
  }

  @Override
  public void addSelectedRecords(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerDataObject> objects = getRecords(boundingBox);
      for (final Iterator<LayerDataObject> iterator = objects.iterator(); iterator.hasNext();) {
        final LayerDataObject layerDataObject = iterator.next();
        if (!isVisible(layerDataObject) || super.isDeleted(layerDataObject)) {
          iterator.remove();
        }
      }
      if (!objects.isEmpty()) {
        showRecordsTable(DataObjectLayerTableModel.MODE_SELECTED);
      }
      addSelectedRecords(objects);
    }
  }

  protected void addToIndex(final LayerDataObject object) {
    getIndex().insert(object);
  }

  protected void cacheObjects(final Collection<? extends DataObject> objects) {
    for (final DataObject object : objects) {
      if (object instanceof LayerDataObject) {
        final LayerDataObject layerDataObject = (LayerDataObject)object;
        getCacheRecord(layerDataObject);
      }
    }
  }

  /**
   * Remove any cached objects that are currently not used.
   */
  private void cleanCachedObjects() {
    synchronized (this.cachedRecords) {
      final Set<String> ids = new HashSet<String>();
      ids.addAll(this.deletedObjectIds);
      ids.addAll(this.formObjectIds);
      for (final LayerDataObject record : getSelectedRecords()) {
        final String id = getId(record);
        if (id != null) {
          ids.add(id);
        }
      }
      for (final LayerDataObject record : getHighlightedRecords()) {
        final String id = getId(record);
        if (id != null) {
          ids.add(id);
        }
      }
      for (final LayerDataObject record : getModifiedRecords()) {
        final String id = getId(record);
        if (id != null) {
          ids.add(id);
        }
      }
      for (final DataObject record : getIndex().queryAll()) {
        final String id = getId((LayerDataObject)record);
        if (id != null) {
          ids.add(id);
        }
      }
      this.cachedRecords.keySet().retainAll(ids);
    }
  }

  @Override
  protected void clearChanges() {
    super.clearChanges();
    this.deletedObjectIds.clear();
    cleanCachedObjects();
    for (final LayerDataObject object : this.cachedRecords.values()) {
      removeForm(object);
    }
    this.cachedRecords.clear();
  }

  protected void clearLoading(final BoundingBox loadedBoundingBox) {
    synchronized (this.sync) {
      if (loadedBoundingBox == this.loadingBoundingBox) {
        firePropertyChange("loaded", false, true);
        this.boundingBox = this.loadingBoundingBox;
        this.loadingBoundingBox = new BoundingBox();
        this.loadingWorker = null;
      }

    }
  }

  @Override
  public void clearSelectedRecords() {
    synchronized (this.cachedRecords) {
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
    this.cachedRecords.clear();
    this.dataStore = null;
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
  protected void deleteRecord(final LayerDataObject record) {
    if (isLayerRecord(record)) {
      removeSelectedRecords(record);
      final LayerDataObject cacheRecord = getCacheRecord(record);
      final String id = getId(cacheRecord);
      if (StringUtils.hasText(id)) {
        this.deletedObjectIds.add(id);
        deleteRecord(cacheRecord, true);
        removeFromIndex(record);
        removeFromIndex(cacheRecord);
      } else {
        removeFromIndex(record);
        super.deleteRecord(record);
      }
    }
  }

  @Override
  protected boolean doInitialize() {
    final Map<String, String> connectionProperties = getProperty("connection");
    if (connectionProperties == null) {
      LoggerFactory.getLogger(getClass())
        .error(
          "A data store layer requires a connectionProperties entry with a name or url, username, and password: "
            + getPath());
    } else {
      final String name = connectionProperties.get("name");
      DataObjectStore dataStore;
      if (StringUtils.hasText(name)) {
        dataStore = DataObjectStoreConnectionManager.getConnection(name);
        // TODO give option to create new data store
        // if (dataStore == null) {
        // final DataObjectStoreConnectionManager connectionManager =
        // DataObjectStoreConnectionManager.get();
        // final ConnectionRegistry<DataObjectStoreConnection> registry =
        // connectionManager.getConnectionRegistry("User");
        // dataStore = new AddDataStoreConnectionPanel(registry,
        // name).showDialog();
        // }
      } else {
        dataStore = DataObjectStoreFactoryRegistry.createDataObjectStore(connectionProperties);
      }
      if (dataStore == null) {
        LoggerFactory.getLogger(getClass()).error(
          "Unable to create data store for layer: " + getPath());
      } else {
        try {
          dataStore.initialize();
        } catch (final Throwable e) {
          throw new RuntimeException(
            "Unable to iniaitlize data store for layer " + getPath(), e);
        }

        setDataStore(dataStore);

        final String typePath = getTypePath();
        final DataObjectMetaData metaData = dataStore.getMetaData(typePath);

        if (metaData == null) {
          LoggerFactory.getLogger(getClass()).error(
            "Cannot find table " + typePath + " for layer " + getPath());
        } else {
          setMetaData(metaData);
          return true;
        }
      }
    }
    return false;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  @Override
  protected List<LayerDataObject> doQuery(final BoundingBox boundingBox) {
    final boolean enabled = setEventsEnabled(false);
    try {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final BoundingBox queryBoundingBox = boundingBox.convert(geometryFactory);
      if (this.boundingBox.contains(queryBoundingBox)) {
        return (List)getIndex().queryIntersects(queryBoundingBox);
      } else {
        final List<LayerDataObject> readRecords = getRecordsFromDataStore(queryBoundingBox);
        final List<LayerDataObject> records = getCachedRecords(readRecords);
        return records;
      }
    } finally {
      setEventsEnabled(enabled);
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
        return (List)getIndex().queryDistance(queryGeometry, distance);
      } else {
        final String typePath = getTypePath();
        final DataObjectStore dataStore = getDataStore();
        final Reader reader = dataStore.query(this, typePath, queryGeometry,
          distance);
        try {
          final List<LayerDataObject> readObjects = reader.read();
          final List<LayerDataObject> objects = getCachedRecords(readObjects);
          return objects;
        } finally {
          reader.close();
        }
      }
    } finally {
      setEventsEnabled(enabled);
    }
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  @Override
  protected List<LayerDataObject> doQueryBackground(
    final BoundingBox boundingBox) {
    if (boundingBox.isNull()) {
      return Collections.emptyList();
    } else {
      synchronized (this.sync) {
        final BoundingBox loadBoundingBox = boundingBox.expandPercent(0.2);
        if (this.boundingBox == null || !this.boundingBox.contains(boundingBox)
          && !this.loadingBoundingBox.contains(boundingBox)) {
          if (this.loadingWorker != null) {
            this.loadingWorker.cancel(true);
          }
          this.loadingBoundingBox = loadBoundingBox;
          this.loadingWorker = createLoadingWorker(loadBoundingBox);
          Invoke.worker(this.loadingWorker);
        }
      }
      Polygon polygon = boundingBox.toPolygon();
      final GeometryFactory geometryFactory = getGeometryFactory();
      polygon = geometryFactory.project(polygon);

      final List objects = getIndex().queryIntersects(polygon);
      return objects;
    }
  }

  @Override
  protected boolean doSaveChanges() {
    return invokeInTransaction(this.saveChangesMethod);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return getCoordinateSystem().getAreaBoundingBox();
  }

  protected List<LayerDataObject> getCachedRecords(
    final List<LayerDataObject> readObjects) {
    final List<LayerDataObject> objects = new ArrayList<LayerDataObject>();
    for (final LayerDataObject object : readObjects) {
      final String id = getId(object);
      synchronized (this.cachedRecords) {
        final LayerDataObject cachedObject = this.cachedRecords.get(id);
        if (cachedObject == null) {
          objects.add(object);
        } else {
          objects.add(cachedObject);
        }
      }
    }
    return objects;
  }

  protected LayerDataObject getCacheRecord(final LayerDataObject object) {
    if (object == null) {
      return null;
    } else {
      final String id = getId(object);
      return getCacheRecord(id, object);
    }
  }

  protected LayerDataObject getCacheRecord(final String id) {
    synchronized (this.cachedRecords) {
      if (id == null) {
        return null;
      } else {
        LayerDataObject object = this.cachedRecords.get(id);
        if (object == null) {
          object = getRecordById(id);
          if (object != null) {
            this.cachedRecords.put(id, object);
          }
        }
        return object;
      }
    }
  }

  private LayerDataObject getCacheRecord(final String id,
    final LayerDataObject record) {
    if (record != null && isLayerRecord(record)) {
      if (record.getState() == DataObjectState.New) {
        return record;
      } else {
        synchronized (this.cachedRecords) {
          if (this.cachedRecords.containsKey(id)) {
            return this.cachedRecords.get(id);
          } else {
            this.cachedRecords.put(id, record);
            return record;
          }
        }
      }
    } else {
      return null;
    }
  }

  @Override
  public DataObjectStore getDataStore() {
    return this.dataStore;
  }

  protected String getId(final LayerDataObject object) {
    if (isLayerRecord(object)) {
      return StringConverterRegistry.toString(object.getIdValue());
    } else {
      return null;
    }
  }

  public BoundingBox getLoadingBoundingBox() {
    return this.loadingBoundingBox;
  }

  @Override
  public LayerDataObject getRecordById(final Object id) {
    final DataObjectMetaData metaData = getMetaData();
    final String idAttributeName = metaData.getIdAttributeName();
    if (idAttributeName == null) {
      LoggerFactory.getLogger(getClass()).error(
        this.typePath + " does not have a primary key");
      return null;
    } else {
      final String idString = StringConverterRegistry.toString(id);
      final LayerDataObject object = this.cachedRecords.get(idString);
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

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  protected List<LayerDataObject> getRecords(final BoundingBox boundingBox) {
    final BoundingBox convertedBoundingBox = boundingBox.convert(getGeometryFactory());
    BoundingBox loadedBoundingBox;
    DataObjectQuadTree index;
    synchronized (this.sync) {
      loadedBoundingBox = this.boundingBox;
      index = getIndex();
    }
    List<LayerDataObject> queryObjects;
    if (loadedBoundingBox.contains(boundingBox)) {
      queryObjects = (List)index.query(convertedBoundingBox);
    } else {
      queryObjects = getRecordsFromDataStore(convertedBoundingBox);
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
  protected List<LayerDataObject> getRecordsFromDataStore(
    final BoundingBox boundingBox) {
    if (isExists()) {
      final DataObjectStore dataStore = getDataStore();
      if (dataStore != null) {
        final Query query = new Query(getTypePath());
        query.setBoundingBox(boundingBox);
        query.setProperty("dataObjectFactory", this);
        final Reader reader = dataStore.query(query);
        try {
          return reader.read();
        } finally {
          reader.close();
        }
      }
    }
    return Collections.emptyList();
  }

  @Override
  public int getRowCount(final Query query) {
    if (isExists()) {
      final DataObjectStore dataStore = getDataStore();
      if (dataStore != null) {
        return dataStore.getRowCount(query);
      }
    }
    return 0;
  }

  public String getTypePath() {
    return this.typePath;
  }

  protected boolean invokeInTransaction(final Method method,
    final Object... args) {
    final DataObjectStore dataStore = getDataStore();
    final PlatformTransactionManager transactionManager = dataStore.getTransactionManager();
    return (Boolean)TransactionUtils.invoke(transactionManager, this, method,
      args);
  }

  @Override
  public boolean isLayerRecord(final DataObject object) {
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
    if (isExists()) {
      final DataObjectStore dataStore = getDataStore();
      if (dataStore != null) {
        final boolean enabled = setEventsEnabled(false);
        try {
          query.setProperty("dataObjectFactory", this);
          final Reader reader = dataStore.query(query);
          try {
            final List<LayerDataObject> readObjects = reader.read();
            final List<LayerDataObject> objects = getCachedRecords(readObjects);
            return objects;
          } finally {
            reader.close();
          }
        } finally {
          setEventsEnabled(enabled);
        }
      }
    }
    return Collections.emptyList();
  }

  @Override
  public void refresh() {
    super.refresh();
    synchronized (this.sync) {
      if (this.loadingWorker != null) {
        this.loadingWorker.cancel(true);
      }
      this.boundingBox = new BoundingBox();
      this.loadingBoundingBox = this.boundingBox;
      setIndex(null);
    }
    fireRecordsChanged();
  }

  @Override
  protected void removeForm(final LayerDataObject object) {
    synchronized (this.formObjectIds) {
      final String id = getId(object);
      if (id != null) {
        this.formObjectIds.remove(id);
        cleanCachedObjects();
      }
      super.removeForm(object);
    }
  }

  protected void removeFromIndex(final LayerDataObject object) {
    getIndex().remove(object);
  }

  @Override
  protected void removeSelectedRecord(final LayerDataObject object) {
    final DataObject cachedObject = getCacheRecord(object);
    if (cachedObject != null) {
      super.removeSelectedRecord(object);
    }
  }

  @Override
  public void removeSelectedRecords(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerDataObject> objects = getRecords(boundingBox);
      for (final Iterator<LayerDataObject> iterator = objects.iterator(); iterator.hasNext();) {
        final LayerDataObject layerDataObject = iterator.next();
        if (!isVisible(layerDataObject) || super.isDeleted(layerDataObject)) {
          iterator.remove();
        }
      }
      if (!objects.isEmpty()) {
        showRecordsTable(DataObjectLayerTableModel.MODE_SELECTED);
      }
      removeSelectedRecords(objects);
    }
  }

  @Override
  public void revertChanges(final LayerDataObject object) {
    final String id = getId(object);
    this.deletedObjectIds.remove(id);
    super.revertChanges(object);
  }

  @Override
  public boolean saveChanges(final LayerDataObject object) {
    return invokeInTransaction(this.saveObjectChangesMethod, object);
  }

  protected void setDataStore(final DataObjectStore dataStore) {
    this.dataStore = dataStore;
  }

  protected void setIndex(final BoundingBox loadedBoundingBox,
    final DataObjectQuadTree index) {
    if (this.sync != null) {
      synchronized (this.sync) {
        if (loadedBoundingBox == this.loadingBoundingBox) {
          setIndex(index);
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
      final List<LayerDataObject> objects = getRecords(boundingBox);
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
      if (isExists()) {
        final DataObjectStore dataStore = getDataStore();
        if (dataStore != null) {
          final DataObjectMetaData metaData = dataStore.getMetaData(typePath);
          if (metaData != null) {

            setMetaData(metaData);
            setQuery(new Query(metaData));
            return;
          }
        }
      }
    }
    setMetaData(null);
    setQuery(null);
  }

  @Override
  public <V extends JComponent> V showForm(final LayerDataObject object) {
    synchronized (this.formObjectIds) {
      final String id = getId(object);
      if (id == null) {
        return super.showForm(object);
      } else {
        this.formObjectIds.add(id);
        final LayerDataObject cachedObject = getCacheRecord(id, object);
        return super.showForm(cachedObject);
      }
    }
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    MapSerializerUtil.add(map, "typePath", this.typePath);
    return map;
  }

  protected synchronized boolean transactionSaveChanges() {
    if (isExists()) {
      final DataObjectStore dataStore = getDataStore();
      if (dataStore != null) {
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
    }
    return false;
  }

  protected synchronized boolean transactionSaveObjectChanges(
    final LayerDataObject object) {
    if (isExists()) {
      final DataObjectStore dataStore = getDataStore();
      if (dataStore != null) {
        final Writer<DataObject> writer = dataStore.createWriter();
        try {
          final String id = object.getString(getMetaData().getIdAttributeName());
          if (this.deletedObjectIds.contains(id) || super.isDeleted(object)) {
            removeDeletedObject(object);
            deletedObjectIds.remove(id);
            getIndex().remove(object);
            object.setState(DataObjectState.Deleted);
            writer.write(object);
          } else if (super.isModified(object)) {
            removeModifiedObject(object);
            writer.write(object);
            getIndex().insert(object);
          } else if (isNew(object)) {
            removeNewObject(object);
            getIndex().insert(object);
            writer.write(object);
          }
        } finally {
          writer.close();
        }
        InvokeMethodAfterCommit.invoke(this, "refresh");
        return true;
      }
    }
    return false;
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
      if (getIndex().remove(oldBoundingBox, object)) {
        addToIndex(object);
      }
    }

  }
}
