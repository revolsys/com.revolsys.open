package com.revolsys.swing.map.layer.dataobject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.SwingWorker;

import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.filter.Filter;
import com.revolsys.gis.algorithm.index.DataObjectQuadTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.model.filter.DataObjectGeometryIntersectsFilter;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.io.Statistics;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.io.datastore.DataObjectStoreConnectionManager;
import com.revolsys.io.map.InvokeMethodMapObjectFactory;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.layer.dataobject.table.model.DataObjectLayerTableModel;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Polygon;

public class DataObjectStoreLayer extends AbstractDataObjectLayer {

  public static final MapObjectFactory FACTORY = new InvokeMethodMapObjectFactory(
    "dataStore", "Data Store", DataObjectStoreLayer.class, "create");

  public static AbstractDataObjectLayer create(
    final Map<String, Object> properties) {
    return new DataObjectStoreLayer(properties);
  }

  private BoundingBox boundingBox = new BoundingBox();

  private Map<String, LayerDataObject> cachedRecords = new HashMap<String, LayerDataObject>();

  private DataObjectStore dataStore;

  private BoundingBox loadingBoundingBox = new BoundingBox();

  private SwingWorker<DataObjectQuadTree, Void> loadingWorker;

  private final Object sync = new Object();

  private String typePath;

  private final Set<String> deletedRecordIds = new LinkedHashSet<String>();

  private final Set<String> formRecordIds = new LinkedHashSet<String>();

  public DataObjectStoreLayer(final DataObjectStore dataStore,
    final String typePath, final boolean exists) {
    this.dataStore = dataStore;
    setExists(exists);
    setType("dataStore");

    setMetaData(dataStore.getMetaData(typePath));
    setTypePath(typePath);
  }

  public DataObjectStoreLayer(final Map<String, ? extends Object> properties) {
    super(properties);
    setType("dataStore");
  }

  @SuppressWarnings("unchecked")
  protected <V extends LayerDataObject> boolean addCachedRecord(
    final List<V> records, final V record) {
    final String id = getId(record);
    if (id == null) {
      records.add(record);
    } else if (record.getState() == DataObjectState.Deleted) {
      return false;
    } else {
      synchronized (this.cachedRecords) {
        final V cachedRecord = (V)this.cachedRecords.get(id);
        if (cachedRecord == null) {
          records.add(record);
        } else {
          if (cachedRecord.getState() == DataObjectState.Deleted) {
            return false;
          } else {
            records.add(cachedRecord);
          }
        }
      }
    }
    return true;
  }

  protected void addIds(final Set<String> ids,
    final Collection<? extends DataObject> records) {
    for (final DataObject record : records) {
      final String id = getId((LayerDataObject)record);
      if (id != null) {
        ids.add(id);
      }
    }
  }

  @Override
  protected void addModifiedRecord(final LayerDataObject record) {
    final LayerDataObject cacheObject = getCacheRecord(record);
    if (cacheObject != null) {
      super.addModifiedRecord(cacheObject);
    }
  }

  @Override
  protected ValueField addPropertiesTabGeneralPanelSource(final BasePanel parent) {
    final ValueField panel = super.addPropertiesTabGeneralPanelSource(parent);
    final Map<String, String> connectionProperties = getProperty("connection");
    String connectionName = null;
    String url = null;
    String username = null;
    if (isExists()) {
      final DataObjectStore dataStore = getDataStore();
      url = dataStore.getUrl();
      username = dataStore.getUsername();
    }
    if (connectionProperties != null) {
      connectionName = connectionProperties.get("name");
      if (!isExists()) {
        url = connectionProperties.get("url");
        username = connectionProperties.get("username");

      }
    }
    if (connectionName != null) {
      SwingUtil.addReadOnlyTextField(panel, "Data Store Name", connectionName);
    }
    if (url != null) {
      SwingUtil.addReadOnlyTextField(panel, "Data Store URL", url);
    }
    if (username != null) {
      SwingUtil.addReadOnlyTextField(panel, "Data Store Username", username);
    }
    SwingUtil.addReadOnlyTextField(panel, "Type Path", typePath);

    GroupLayoutUtil.makeColumns(panel, 2, true);
    return panel;
  }

  @Override
  protected void addSelectedRecord(final LayerDataObject record) {
    final DataObject cachedObject = getCacheRecord(record);
    if (cachedObject != null) {
      super.addSelectedRecord(record);
    }
  }

  @Override
  public void addSelectedRecords(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerDataObject> records = getRecords(boundingBox);
      for (final Iterator<LayerDataObject> iterator = records.iterator(); iterator.hasNext();) {
        final LayerDataObject layerDataObject = iterator.next();
        if (!isVisible(layerDataObject) || super.isDeleted(layerDataObject)) {
          iterator.remove();
        }
      }
      if (!records.isEmpty()) {
        showRecordsTable(DataObjectLayerTableModel.MODE_SELECTED);
      }
      addSelectedRecords(records);
    }
  }

  protected void cacheRecords(final Collection<? extends DataObject> records) {
    for (final DataObject record : records) {
      if (record instanceof LayerDataObject) {
        final LayerDataObject layerRecord = (LayerDataObject)record;
        getCacheRecord(layerRecord);
      }
    }
  }

  /**
   * Remove any cached records that are currently not used.
   */
  protected void cleanCachedRecords() {
    synchronized (this.cachedRecords) {
      final Set<String> ids = getIdsToCache();
      final Map<String, LayerDataObject> cachedRecords = new HashMap<String, LayerDataObject>();
      for (final String id : ids) {
        final LayerDataObject record = this.cachedRecords.get(id);
        if (record != null) {
          cachedRecords.put(id, record);
        }
      }
      this.cachedRecords = cachedRecords;
    }
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
      cleanCachedRecords();
    }
  }

  protected LoadingWorker createLoadingWorker(final BoundingBox boundingBox) {
    return new LoadingWorker(this, boundingBox);
  }

  @Override
  public void delete() {
    if (dataStore != null) {
      final Map<String, String> connectionProperties = getProperty("connection");
      if (connectionProperties != null) {
        final Map<String, Object> config = new HashMap<String, Object>();
        config.put("connection", connectionProperties);
        DataObjectStoreConnectionManager.releaseDataStore(config);
      }
      this.dataStore = null;
    }
    final SwingWorker<DataObjectQuadTree, Void> loadingWorker = this.loadingWorker;
    this.boundingBox = new BoundingBox();
    this.cachedRecords = new HashMap<String, LayerDataObject>();
    this.loadingBoundingBox = new BoundingBox();
    this.loadingWorker = null;
    this.typePath = null;
    super.delete();
    if (loadingWorker != null) {
      loadingWorker.cancel(true);
    }
  }

  @Override
  public void deleteRecord(final LayerDataObject record) {
    if (isLayerRecord(record)) {
      record.setState(DataObjectState.Deleted);
      unSelectRecords(record);
      final String id = getId(record);
      if (StringUtils.hasText(id)) {
        final LayerDataObject cacheRecord = removeCacheRecord(id, record);
        this.deletedRecordIds.add(id);
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
      final Map<String, Object> config = new HashMap<String, Object>();
      config.put("connection", connectionProperties);
      final DataObjectStore dataStore = DataObjectStoreConnectionManager.getDataStore(config);

      final String typePath = getTypePath();
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
      final String typePath = getTypePath();
      final DataObjectStore dataStore = getDataStore();
      final Reader reader = dataStore.query(this, typePath, queryGeometry,
        distance);
      try {
        final List<LayerDataObject> results = reader.read();
        final List<LayerDataObject> records = getCachedRecords(results);
        return records;
      } finally {
        reader.close();
      }
    } finally {
      setEventsEnabled(enabled);
    }
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  @Override
  protected List<LayerDataObject> doQuery(final Query query) {
    if (isExists()) {
      final DataObjectStore dataStore = getDataStore();
      if (dataStore != null) {
        final boolean enabled = setEventsEnabled(false);
        try {
          final Statistics statistics = query.getProperty("statistics");
          query.setProperty("dataObjectFactory", this);
          final Reader<LayerDataObject> reader = (Reader)dataStore.query(query);
          try {
            final List<LayerDataObject> records = new ArrayList<LayerDataObject>();
            for (final LayerDataObject record : reader) {
              final boolean added = addCachedRecord(records, record);
              if (added && statistics != null) {
                statistics.add(record);
              }
            }
            return records;

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

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  @Override
  protected List<LayerDataObject> doQueryBackground(
    final BoundingBox boundingBox) {
    if (boundingBox == null || boundingBox.isEmpty()) {
      return Collections.emptyList();
    } else {
      synchronized (this.sync) {
        final BoundingBox loadBoundingBox = boundingBox.expandPercent(0.2);
        if (!this.boundingBox.contains(boundingBox)
          && !this.loadingBoundingBox.contains(boundingBox)) {
          if (this.loadingWorker != null) {
            this.loadingWorker.cancel(true);
          }
          this.loadingBoundingBox = loadBoundingBox;
          this.loadingWorker = createLoadingWorker(loadBoundingBox);
          Invoke.worker(this.loadingWorker);
        }
      }
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Polygon polygon = boundingBox.toPolygon(geometryFactory, 10, 10);

      final DataObjectQuadTree index = getIndex();

      final List<LayerDataObject> records = (List)index.queryIntersects(polygon);

      final Filter filter = new DataObjectGeometryIntersectsFilter(boundingBox);
      for (final ListIterator<LayerDataObject> iterator = records.listIterator(); iterator.hasNext();) {
        final LayerDataObject record = iterator.next();
        final LayerDataObject cachedRecord = getCacheRecord(record);
        if (filter.accept(cachedRecord)) {
          iterator.set(cachedRecord);
        } else {
          iterator.remove();
        }
      }
      return records;
    }
  }

  @Override
  protected boolean doSaveChanges(final LayerDataObject record) {
    final boolean deleted = isDeleted(record);
    final PlatformTransactionManager transactionManager = getDataStore().getTransactionManager();
    try (
      Transaction transaction = new Transaction(transactionManager,
        Propagation.REQUIRES_NEW)) {
      try {

        if (isExists()) {
          final DataObjectStore dataStore = getDataStore();
          if (dataStore != null) {
            final Writer<DataObject> writer = dataStore.createWriter();
            try {
              final String idAttributeName = getMetaData().getIdAttributeName();
              final String idString = record.getIdString();
              if (this.deletedRecordIds.contains(idString)
                || super.isDeleted(record)) {
                record.setState(DataObjectState.Deleted);
                writer.write(record);
              } else if (super.isModified(record)) {
                writer.write(record);
              } else if (isNew(record)) {
                Object id = record.getIdValue();
                if (id == null && StringUtils.hasText(idAttributeName)) {
                  id = dataStore.createPrimaryIdValue(typePath);
                  record.setValue(idAttributeName, id);
                }

                writer.write(record);
              }
            } finally {
              writer.close();
            }
            if (!deleted) {
              record.setState(DataObjectState.Persisted);
            }
            return true;
          }
        }
        return false;
      } catch (final Throwable e) {
        throw transaction.setRollbackOnly(e);
      }
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    return getCoordinateSystem().getAreaBoundingBox();
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  protected <V extends LayerDataObject> List<V> getCachedRecords() {
    synchronized (cachedRecords) {
      final List<V> cachedRecords = new ArrayList(this.cachedRecords.values());
      return cachedRecords;

    }
  }

  protected <V extends LayerDataObject> List<V> getCachedRecords(
    final Collection<? extends V> records) {
    final List<V> cachedRecords = new ArrayList<V>();
    for (final V record : records) {
      addCachedRecord(cachedRecords, record);
    }
    return cachedRecords;
  }

  public LayerDataObject getCacheRecord(final LayerDataObject record) {
    if (record == null) {
      return null;
    } else {
      final String id = getId(record);
      return getCacheRecord(id, record);
    }
  }

  private LayerDataObject getCacheRecord(final String id,
    final LayerDataObject record) {
    if (StringUtils.hasText(id) && record != null && isLayerRecord(record)) {
      if (record.getState() == DataObjectState.New) {
        return record;
      } else if (record.getState() == DataObjectState.Deleted) {
        return record;
      } else {
        synchronized (this.cachedRecords) {
          if (this.cachedRecords.containsKey(id)) {
            final LayerDataObject cachedRecord = this.cachedRecords.get(id);
            if (cachedRecord.getState() == DataObjectState.Deleted) {
              this.cachedRecords.remove(id);
            }
            return cachedRecord;
          } else {
            this.cachedRecords.put(id, record);
            return record;
          }
        }
      }
    } else {
      return record;
    }
  }

  @Override
  public DataObjectStore getDataStore() {
    return this.dataStore;
  }

  protected String getId(final LayerDataObject record) {
    if (isLayerRecord(record)) {
      return StringConverterRegistry.toString(record.getIdValue());
    } else {
      return null;
    }
  }

  protected Set<String> getIdsToCache() {
    final Set<String> ids = new HashSet<String>();
    ids.addAll(this.deletedRecordIds);
    ids.addAll(this.formRecordIds);
    addIds(ids, getSelectedRecords());
    addIds(ids, getHighlightedRecords());
    addIds(ids, getModifiedRecords());
    addIds(ids, getIndex().queryAll());
    return ids;
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
      final LayerDataObject record = this.cachedRecords.get(idString);
      if (record == null) {
        final Query query = Query.equal(metaData, idAttributeName, id);
        query.setProperty("dataObjectFactory", this);
        final DataObjectStore dataStore = getDataStore();
        return (LayerDataObject)dataStore.queryFirst(query);
      } else {
        return record;
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
        for (final LayerDataObject record : queryObjects) {
          if (!record.getState().equals(DataObjectState.Deleted)) {
            final Geometry geometry = record.getGeometryValue();
            if (geometry.intersects(polygon)) {
              allObjects.add(record);
            }
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

  @Override
  protected LayerDataObject internalCancelChanges(final LayerDataObject record) {
    if (record.getState() == DataObjectState.Deleted) {
      final String id = getId(record);
      if (StringUtils.hasText(id)) {
        this.deletedRecordIds.remove(id);
      }
    }
    return super.internalCancelChanges(record);
  }

  @Override
  public boolean isLayerRecord(final DataObject record) {
    if (record instanceof LayerDataObject) {
      final LayerDataObject layerDataObject = (LayerDataObject)record;
      if (layerDataObject.getLayer() == this) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void postSaveChanges(final DataObjectState originalState,
    final LayerDataObject record) {
    super.postSaveChanges(originalState, record);
    if (originalState == DataObjectState.New) {
      getCacheRecord(record);
    }
  }

  @Override
  protected boolean postSaveDeletedRecord(final LayerDataObject record) {
    final boolean deleted = super.postSaveDeletedRecord(record);
    if (deleted) {
      final String id = record.getIdString();
      deletedRecordIds.remove(id);
    }
    return deleted;
  }

  public List<LayerDataObject> query(final Map<String, ? extends Object> filter) {
    final DataObjectMetaData metaData = getMetaData();
    final Query query = Query.and(metaData, filter);
    return query(query);
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
      cleanCachedRecords();
    }
    final DataObjectStore dataStore = getDataStore();
    final String typePath = getTypePath();
    final CodeTable codeTable = dataStore.getCodeTable(typePath);
    if (codeTable != null) {
      codeTable.refresh();
    }
    fireRecordsChanged();
  }

  private LayerDataObject removeCacheRecord(final String id,
    final LayerDataObject record) {
    if (StringUtils.hasText(id) && record != null && isLayerRecord(record)) {
      if (record.getState() == DataObjectState.New) {
        return record;
      } else if (record.getState() == DataObjectState.Deleted) {
        return record;
      } else {
        synchronized (this.cachedRecords) {
          if (this.cachedRecords.containsKey(id)) {
            final LayerDataObject cachedRecord = this.cachedRecords.remove(id);
            if (cachedRecord.getState() == DataObjectState.Deleted) {
              this.cachedRecords.remove(id);
            }
            return cachedRecord;
          }
        }
      }
    }
    return record;
  }

  @Override
  protected void removeForm(final LayerDataObject record) {
    synchronized (this.formRecordIds) {
      final String id = getId(record);
      if (id != null) {
        this.formRecordIds.remove(id);
        cleanCachedRecords();
      }
      super.removeForm(record);
    }
  }

  @Override
  protected void removeSelectedRecord(final LayerDataObject record) {
    final DataObject cachedObject = getCacheRecord(record);
    if (cachedObject != null) {
      super.removeSelectedRecord(record);
    }
  }

  @Override
  public void revertChanges(final LayerDataObject record) {
    final String id = getId(record);
    this.deletedRecordIds.remove(id);
    super.revertChanges(record);
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
          cacheRecords(index.queryAll());
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
      final List<LayerDataObject> records = getRecords(boundingBox);
      for (final Iterator<LayerDataObject> iterator = records.iterator(); iterator.hasNext();) {
        final LayerDataObject layerDataObject = iterator.next();
        if (!isVisible(layerDataObject) || super.isDeleted(layerDataObject)) {
          iterator.remove();
        }
      }
      if (!records.isEmpty()) {
        showRecordsTable(DataObjectLayerTableModel.MODE_SELECTED);
      }
      setSelectedRecords(records);
    }
  }

  @Override
  public void setSelectedRecords(final Collection<LayerDataObject> records) {
    super.setSelectedRecords(records);
    cleanCachedRecords();
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
  public <V extends JComponent> V showForm(final LayerDataObject record) {
    synchronized (this.formRecordIds) {
      final String id = getId(record);
      if (id == null) {
        return super.showForm(record);
      } else {
        this.formRecordIds.add(id);
        final LayerDataObject cachedObject = getCacheRecord(id, record);
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

  @Override
  public void unSelectRecords(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerDataObject> records = getRecords(boundingBox);
      for (final Iterator<LayerDataObject> iterator = records.iterator(); iterator.hasNext();) {
        final LayerDataObject layerDataObject = iterator.next();
        if (!isVisible(layerDataObject) || super.isDeleted(layerDataObject)) {
          iterator.remove();
        }
      }
      if (!records.isEmpty()) {
        showRecordsTable(DataObjectLayerTableModel.MODE_SELECTED);
      }
      unSelectRecords(records);
    }
  }

  @Override
  public void unSelectRecords(
    final Collection<? extends LayerDataObject> records) {
    super.unSelectRecords(records);
    cleanCachedRecords();
  }

}
