package com.revolsys.swing.map.layer.record;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.JComponent;
import javax.swing.SwingWorker;

import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.filter.DataObjectGeometryBoundingBoxIntersectsFilter;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.identifier.SingleIdentifier;
import com.revolsys.data.io.DataObjectStore;
import com.revolsys.data.query.Condition;
import com.revolsys.data.query.Q;
import com.revolsys.data.query.Query;
import com.revolsys.data.query.functions.F;
import com.revolsys.data.query.functions.WithinDistance;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.filter.Filter;
import com.revolsys.gis.algorithm.index.DataObjectQuadTree;
import com.revolsys.gis.io.Statistics;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.io.datastore.DataObjectStoreConnectionManager;
import com.revolsys.io.map.InvokeMethodMapObjectFactory;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.layer.record.table.model.DataObjectLayerTableModel;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.Label;

public class DataObjectStoreLayer extends AbstractDataObjectLayer {

  public static AbstractDataObjectLayer create(
    final Map<String, Object> properties) {
    return new DataObjectStoreLayer(properties);
  }

  public static final MapObjectFactory FACTORY = new InvokeMethodMapObjectFactory(
    "dataStore", "Data Store", DataObjectStoreLayer.class, "create");

  private BoundingBox boundingBox = new BoundingBoxDoubleGf();

  /**
   * Caches of sets of {@link Record#getIdentifier()} for different purposes (e.g. selected records, deleted records).
   * Each cache has a separate cacheId. The cache id is recommended to be a private variable to prevent modification
   * of that cache.
   */
  private final Map<Label, Set<Identifier>> cacheIdToRecordIdMap = new WeakHashMap<>();

  private DataObjectStore dataStore;

  private final Label cacheIdDeleted = new Label("deleted");

  private final Label cacheIdForm = new Label("deleted");

  private final Label cacheIdModified = new Label("modified");

  private final Label cacheIdSelected = new Label("selected");

  private final Label cacheIdIndex = new Label("index");

  private BoundingBox loadingBoundingBox = new BoundingBoxDoubleGf();

  private SwingWorker<List<LayerRecord>, Void> loadingWorker;

  /** Cache of records from {@link Record#getIdentifier()} to {@link Record}. */
  private final Map<Identifier, LayerRecord> recordIdToRecordMap = new WeakHashMap<>();

  private final Object sync = new Object();

  private String typePath;

  public DataObjectStoreLayer(final DataObjectStore dataStore,
    final String typePath, final boolean exists) {
    this.dataStore = dataStore;
    setExists(exists);
    setType("dataStore");

    setMetaData(dataStore.getRecordDefinition(typePath));
    setTypePath(typePath);
  }

  public DataObjectStoreLayer(final Map<String, ? extends Object> properties) {
    super(properties);
    setType("dataStore");
  }

  @Override
  protected void addModifiedRecord(final LayerRecord record) {
    final LayerRecord cachedRecord = addRecordToCache(this.cacheIdModified,
      record);
    if (cachedRecord != null) {
      super.addModifiedRecord(cachedRecord);
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
    SwingUtil.addReadOnlyTextField(panel, "Type Path", this.typePath);

    GroupLayoutUtil.makeColumns(panel, 2, true);
    return panel;
  }

  @SuppressWarnings("unchecked")
  protected <V extends LayerRecord> boolean addProxyRecordToList(
    final List<V> records, final LayerRecord record) {
    if (record == null) {
      return false;
    } else if (record.getState() == RecordState.Deleted) {
      return false;
    } else {
      final Identifier identifier = getId(record);
      if (identifier == null) {
        records.add((V)record);
      } else if (record instanceof ProxyLayerRecord) {
        records.add((V)record);
      } else {
        synchronized (getSync()) {
          LayerRecord cachedRecord = this.recordIdToRecordMap.get(identifier);
          if (cachedRecord == null) {
            this.recordIdToRecordMap.put(identifier, record);
            cachedRecord = record;
          } else {
            if (cachedRecord.getState() == RecordState.Deleted) {
              return false;
            }
          }
          records.add((V)createProxyRecord(identifier));
        }
      }
      return true;
    }
  }

  @SuppressWarnings("unchecked")
  public <V extends LayerRecord> List<V> addRecordsToCache(final Label cacheId,
    final Collection<? extends LayerRecord> records) {
    synchronized (getSync()) {
      final List<V> results = new ArrayList<>();
      for (final LayerRecord record : records) {
        final LayerRecord cachedRecord = addRecordToCache(cacheId, record);
        results.add((V)cachedRecord);
      }
      cleanCachedRecords();
      return results;
    }
  }

  public LayerRecord addRecordToCache(final Label cacheId,
    final LayerRecord record) {
    if (record == null) {
      return null;
    } else if (!isLayerRecord(record)) {
      return record;
    } else if (record.getState() == RecordState.Deleted && !isDeleted(record)) {
      return record;
    } else {
      final Identifier identifier = record.getIdentifier();
      if (identifier == null) {
        return record;
      } else {
        synchronized (getSync()) {
          LayerRecord proxyRecord;
          if (record instanceof ProxyLayerRecord) {
            proxyRecord = record;
          } else {
            LayerRecord cachedRecord = this.recordIdToRecordMap.get(identifier);
            if (cachedRecord == null) {
              this.recordIdToRecordMap.put(identifier, record);
              cachedRecord = record;
            }
            proxyRecord = createProxyRecord(identifier);
          }
          CollectionUtil.addToSet(this.cacheIdToRecordIdMap, cacheId,
            identifier);
          return proxyRecord;
        }
      }
    }
  }

  @Override
  protected void addSelectedRecord(final LayerRecord record) {
    if (isLayerRecord(record)) {
      final LayerRecord cachedRecord = addRecordToCache(this.cacheIdSelected,
        record);
      super.addSelectedRecord(cachedRecord);
    }
  }

  @Override
  public void addSelectedRecords(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerRecord> records = getRecords(boundingBox);
      for (final Iterator<LayerRecord> iterator = records.iterator(); iterator.hasNext();) {
        final LayerRecord layerDataObject = iterator.next();
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

  /**
   * Remove any cached records that are currently not used.
   */
  protected void cleanCachedRecords() {
    synchronized (getSync()) {
      final Set<Identifier> ids = new HashSet<>();
      for (final Set<Identifier> recordIds : this.cacheIdToRecordIdMap.values()) {
        if (recordIds != null) {
          ids.addAll(recordIds);
        }
      }
      this.recordIdToRecordMap.keySet().retainAll(ids);
    }
  }

  public void clearCachedRecords(final Label cacheId) {
    synchronized (getSync()) {
      this.cacheIdToRecordIdMap.remove(cacheId);
      cleanCachedRecords();
    }
  }

  protected void clearLoading(final BoundingBox loadedBoundingBox) {
    synchronized (getSync()) {
      if (loadedBoundingBox == this.loadingBoundingBox) {
        firePropertyChange("loaded", false, true);
        this.boundingBox = this.loadingBoundingBox;
        this.loadingBoundingBox = new BoundingBoxDoubleGf();
        this.loadingWorker = null;
      }

    }
  }

  @Override
  public void clearSelectedRecords() {
    synchronized (getSync()) {
      super.clearSelectedRecords();
      clearCachedRecords(this.cacheIdSelected);
    }
  }

  protected LoadingWorker createLoadingWorker(final BoundingBox boundingBox) {
    return new LoadingWorker(this, boundingBox);
  }

  protected ProxyLayerRecord createProxyRecord(final Identifier identifier) {
    return new ProxyLayerRecord(this, identifier);
  }

  @Override
  public void delete() {
    if (this.dataStore != null) {
      final Map<String, String> connectionProperties = getProperty("connection");
      if (connectionProperties != null) {
        final Map<String, Object> config = new HashMap<String, Object>();
        config.put("connection", connectionProperties);
        DataObjectStoreConnectionManager.releaseDataStore(config);
      }
      this.dataStore = null;
    }
    final SwingWorker<List<LayerRecord>, Void> loadingWorker = this.loadingWorker;
    this.boundingBox = new BoundingBoxDoubleGf();
    this.recordIdToRecordMap.clear();
    this.loadingBoundingBox = new BoundingBoxDoubleGf();
    this.loadingWorker = null;
    this.typePath = null;
    super.delete();
    if (loadingWorker != null) {
      loadingWorker.cancel(true);
    }
  }

  @Override
  public void deleteRecord(final LayerRecord record,
    final boolean trackDeletions) {
    if (isLayerRecord(record)) {
      record.setState(RecordState.Deleted);
      unSelectRecords(record);
      final Identifier id = record.getIdentifier();
      if (id == null) {
        super.deleteRecord(record, trackDeletions);
      } else {
        addRecordToCache(this.cacheIdDeleted, record);
        super.deleteRecord(record, true);
      }
      removeFromIndex(record);
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

        final RecordDefinition metaData = dataStore.getRecordDefinition(typePath);

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
  protected List<LayerRecord> doQuery(final BoundingBox boundingBox) {
    final boolean enabled = setEventsEnabled(false);
    try {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final BoundingBox queryBoundingBox = boundingBox.convert(geometryFactory);
      if (this.boundingBox.covers(queryBoundingBox)) {
        return (List)getIndex().queryIntersects(queryBoundingBox);
      } else {
        final List<LayerRecord> records = getRecordsFromDataStore(queryBoundingBox);
        return records;
      }
    } finally {
      setEventsEnabled(enabled);
    }
  }

  @Override
  public List<LayerRecord> doQuery(final Geometry geometry,
    final double distance) {
    if (geometry == null) {
      return Collections.emptyList();
    } else {
      final RecordDefinition metaData = getMetaData();
      final Attribute geometryAttribute = getGeometryAttribute();
      final WithinDistance where = F.dWithin(geometryAttribute, geometry,
        distance);
      final Query query = new Query(metaData, where);
      return query(query);
    }
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  @Override
  protected List<LayerRecord> doQuery(final Query query) {
    if (isExists()) {
      final DataObjectStore dataStore = getDataStore();
      if (dataStore != null) {
        final boolean enabled = setEventsEnabled(false);
        try {
          final Statistics statistics = query.getProperty("statistics");
          query.setProperty("dataObjectFactory", this);
          try (
            final Reader<LayerRecord> reader = (Reader)dataStore.query(query)) {
            final List<LayerRecord> records = new ArrayList<>();
            for (final LayerRecord record : reader) {
              final boolean added = addProxyRecordToList(records, record);
              if (added && statistics != null) {
                statistics.add(record);
              }
            }
            return records;

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
  protected List<LayerRecord> doQueryBackground(final BoundingBox boundingBox) {
    if (boundingBox == null || boundingBox.isEmpty()) {
      return Collections.emptyList();
    } else {
      synchronized (getSync()) {
        final BoundingBox loadBoundingBox = boundingBox.expandPercent(0.2);
        if (!this.boundingBox.covers(boundingBox)
          && !this.loadingBoundingBox.covers(boundingBox)) {
          if (this.loadingWorker != null) {
            this.loadingWorker.cancel(true);
          }
          this.loadingBoundingBox = loadBoundingBox;
          this.loadingWorker = createLoadingWorker(loadBoundingBox);
          Invoke.worker(this.loadingWorker);
        }
      }
      final DataObjectQuadTree index = getIndex();

      final List<LayerRecord> records = (List)index.queryIntersects(boundingBox);

      final Filter filter = new DataObjectGeometryBoundingBoxIntersectsFilter(
        boundingBox);
      for (final ListIterator<LayerRecord> iterator = records.listIterator(); iterator.hasNext();) {
        final LayerRecord record = iterator.next();
        final LayerRecord cachedRecord = getCacheRecord(record);
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
  protected boolean doSaveChanges(final LayerRecord record) {
    final boolean deleted = isDeleted(record);
    final DataObjectStore dataStore = getDataStore();
    final PlatformTransactionManager transactionManager = dataStore.getTransactionManager();
    try (
      Transaction transaction = new Transaction(transactionManager,
        Propagation.REQUIRES_NEW)) {
      try {

        if (isExists()) {
          if (dataStore != null) {
            try (
              final Writer<Record> writer = dataStore.createWriter()) {
              if (isCached(this.cacheIdDeleted, record)
                || super.isDeleted(record)) {
                preDeleteRecord(record);
                record.setState(RecordState.Deleted);
                writer.write(record);
              } else if (super.isModified(record)) {
                writer.write(record);
              } else if (isNew(record)) {
                Identifier id = record.getIdentifier();
                final RecordDefinition metaData = getMetaData();
                final List<String> idAttributeNames = metaData.getIdAttributeNames();
                if (id == null && !idAttributeNames.isEmpty()) {
                  id = SingleIdentifier.create(dataStore.createPrimaryIdValue(this.typePath));
                  id.setIdentifier(record, idAttributeNames);
                }
                writer.write(record);
              }
            }
            if (!deleted) {
              record.setState(RecordState.Persisted);
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

  public LayerRecord getCachedRecord(final Identifier identifier) {
    final RecordDefinition metaData = getMetaData();
    final List<String> idAttributeNames = metaData.getIdAttributeNames();
    if (idAttributeNames.isEmpty()) {
      LoggerFactory.getLogger(getClass()).error(
        this.typePath + " does not have a primary key");
      return null;
    } else {
      LayerRecord record = this.recordIdToRecordMap.get(identifier);
      if (record == null) {
        final Condition where = Q.equal(identifier, idAttributeNames);
        final Query query = new Query(metaData, where);
        query.setProperty("dataObjectFactory", this);
        final DataObjectStore dataStore = getDataStore();
        record = (LayerRecord)dataStore.queryFirst(query);
        this.recordIdToRecordMap.put(identifier, record);
      }
      return record;
    }
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  protected <V extends LayerRecord> List<V> getCachedRecords() {
    synchronized (getSync()) {
      final List<V> cachedRecords = new ArrayList(
        this.recordIdToRecordMap.values());
      return cachedRecords;

    }
  }

  protected <V extends LayerRecord> List<V> getCachedRecords(
    final Collection<? extends V> records) {
    final List<V> cachedRecords = new ArrayList<V>();
    for (final V record : records) {
      addProxyRecordToList(cachedRecords, record);
    }
    return cachedRecords;
  }

  protected Label getCacheIdSelected() {
    return this.cacheIdSelected;
  }

  public LayerRecord getCacheRecord(final LayerRecord record) {
    if (record == null) {
      return null;
    } else if (record instanceof ProxyLayerRecord) {
      return record;
    } else {
      final Identifier id = getId(record);
      return getCacheRecord(id, record);
    }
  }

  private LayerRecord getCacheRecord(final Identifier id,
    final LayerRecord record) {
    if (id != null && record != null && isLayerRecord(record)) {
      if (record instanceof ProxyLayerRecord) {
        return record;
      } else if (record.getState() == RecordState.New) {
        return record;
      } else if (record.getState() == RecordState.Deleted) {
        return record;
      } else {
        synchronized (getSync()) {
          if (!this.recordIdToRecordMap.containsKey(id)) {
            this.recordIdToRecordMap.put(id, record);
          }
          return createProxyRecord(id);
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

  public Attribute getGeometryAttribute() {
    final RecordDefinition metaData = getMetaData();
    if (metaData == null) {
      return null;
    } else {
      return metaData.getGeometryAttribute();
    }
  }

  protected Identifier getId(final LayerRecord record) {
    if (isLayerRecord(record)) {
      return record.getIdentifier();
    } else {
      return null;
    }
  }

  public BoundingBox getLoadingBoundingBox() {
    return this.loadingBoundingBox;
  }

  protected LayerRecord getProxyRecord(final LayerRecord record) {
    if (record == null) {
      return null;
    } else if (!isLayerRecord(record)) {
      return record;
    } else {
      final Identifier identifier = record.getIdentifier();
      if (identifier == null) {
        return record;
      } else if (record instanceof ProxyLayerRecord) {
        return record;
      } else {
        synchronized (getSync()) {
          final LayerRecord cachedRecord = this.recordIdToRecordMap.get(identifier);
          if (cachedRecord == null) {
            return record;
          } else {
            return createProxyRecord(identifier);
          }
        }
      }
    }
  }

  @Override
  public LayerRecord getRecordById(final Identifier id) {
    final RecordDefinition metaData = getMetaData();
    final List<String> idAttributeNames = metaData.getIdAttributeNames();
    if (idAttributeNames.isEmpty()) {
      LoggerFactory.getLogger(getClass()).error(
        this.typePath + " does not have a primary key");
      return null;
    } else {
      LayerRecord record = this.recordIdToRecordMap.get(id);
      if (record == null) {
        final Condition where = Q.equal(id, idAttributeNames);
        final Query query = new Query(metaData, where);
        query.setProperty("dataObjectFactory", this);
        final DataObjectStore dataStore = getDataStore();
        record = (LayerRecord)dataStore.queryFirst(query);
        this.recordIdToRecordMap.put(id, record);
      }
      return createProxyRecord(id);
    }
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  protected List<LayerRecord> getRecords(final BoundingBox boundingBox) {
    final BoundingBox convertedBoundingBox = boundingBox.convert(getGeometryFactory());
    BoundingBox loadedBoundingBox;
    DataObjectQuadTree index;
    synchronized (getSync()) {
      loadedBoundingBox = this.boundingBox;
      index = getIndex();
    }
    List<LayerRecord> queryRecords;
    if (loadedBoundingBox.covers(boundingBox)) {
      queryRecords = (List)index.query(convertedBoundingBox);
    } else {
      queryRecords = getRecordsFromDataStore(convertedBoundingBox);
    }
    final List<LayerRecord> allRecords = new ArrayList<>();
    if (!queryRecords.isEmpty()) {
      final Polygon polygon = convertedBoundingBox.toPolygon();
      try {
        for (final LayerRecord record : queryRecords) {
          if (!record.getState().equals(RecordState.Deleted)) {
            final Geometry geometry = record.getGeometryValue();
            if (geometry.intersects(polygon)) {
              allRecords.add(record);
            }
          }
        }
      } catch (final ClassCastException e) {
        LoggerFactory.getLogger(getClass()).error("error", e);
      }
    }

    return allRecords;
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  protected List<LayerRecord> getRecordsFromDataStore(
    final BoundingBox boundingBox) {
    if (isExists()) {
      final DataObjectStore dataStore = getDataStore();
      if (dataStore != null) {
        final RecordDefinition metaData = getMetaData();
        final Attribute geometryAttribute = getGeometryAttribute();
        final Query query = new Query(metaData, F.envelopeIntersects(
          geometryAttribute, boundingBox));
        query.setProperty("dataObjectFactory", this);
        try (
          final Reader<LayerRecord> reader = (Reader)dataStore.query(query)) {
          final List<LayerRecord> records = new ArrayList<>();

          for (final LayerRecord record : reader) {
            final LayerRecord cachedRecord = getCacheRecord(record);
            records.add(cachedRecord);
          }
          return records;
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

  protected Object getSync() {
    return this.sync;
  }

  public String getTypePath() {
    return this.typePath;
  }

  @Override
  protected LayerRecord internalCancelChanges(final LayerRecord record) {
    if (record.getState() == RecordState.Deleted) {
      removeRecordFromCache(this.cacheIdDeleted, record);
    }
    return super.internalCancelChanges(record);
  }

  public boolean isCached(final Label cacheId, final LayerRecord record) {
    if (record == null) {
      return false;
    } else if (!isLayerRecord(record)) {
      return false;
    } else {
      final Identifier identifier = record.getIdentifier();
      if (CollectionUtil.setContains(this.cacheIdToRecordIdMap, cacheId,
        identifier)) {
        return true;
      } else {
        return false;
      }
    }
  }

  @Override
  public boolean isLayerRecord(final Record record) {
    if (record instanceof LayerRecord) {
      final LayerRecord layerDataObject = (LayerRecord)record;
      if (layerDataObject.getLayer() == this) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void postSaveChanges(final RecordState originalState,
    final LayerRecord record) {
    super.postSaveChanges(originalState, record);
    if (originalState == RecordState.New) {
      getCacheRecord(record);
    }
  }

  @Override
  protected boolean postSaveDeletedRecord(final LayerRecord record) {
    final boolean deleted = super.postSaveDeletedRecord(record);
    if (deleted) {
      removeRecordFromCache(this.cacheIdDeleted, record);
    }
    return deleted;
  }

  protected void preDeleteRecord(final LayerRecord record) {
  }

  public List<LayerRecord> query(final Map<String, ? extends Object> filter) {
    final RecordDefinition metaData = getMetaData();
    final Query query = Query.and(metaData, filter);
    return query(query);
  }

  @Override
  public void refresh() {
    super.refresh();
    synchronized (getSync()) {
      if (this.loadingWorker != null) {
        this.loadingWorker.cancel(true);
      }
      this.boundingBox = new BoundingBoxDoubleGf();
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

  @Override
  protected void removeForm(final LayerRecord record) {
    synchronized (getSync()) {
      final Identifier id = getId(record);
      if (id != null) {
        removeRecordFromCache(this.cacheIdForm, record);
      }
      super.removeForm(record);
    }
  }

  public boolean removeRecordFromCache(final Label cacheId,
    final LayerRecord record) {
    if (record == null) {
      return false;
    } else if (!isLayerRecord(record)) {
      return false;
    } else {
      final Identifier identifier = record.getIdentifier();
      if (identifier == null) {
        return false;
      } else {
        synchronized (getSync()) {
          CollectionUtil.removeFromSet(this.cacheIdToRecordIdMap, cacheId,
            identifier);
        }
      }
      return true;
    }
  }

  public int removeRecordsFromCache(final Label cacheId,
    final Collection<? extends LayerRecord> records) {
    synchronized (getSync()) {
      int count = 0;
      for (final LayerRecord record : records) {
        if (removeRecordFromCache(cacheId, record)) {
          count++;
        }
      }
      cleanCachedRecords();
      return count;
    }
  }

  @Override
  protected void removeSelectedRecord(final LayerRecord record) {
    final Record cahcedRecord = getCacheRecord(record);
    if (cahcedRecord != null) {
      super.removeSelectedRecord(record);
    }
  }

  @Override
  public void revertChanges(final LayerRecord record) {
    removeRecordFromCache(this.cacheIdDeleted, record);
    super.revertChanges(record);
  }

  protected void setDataStore(final DataObjectStore dataStore) {
    this.dataStore = dataStore;
  }

  protected void setRecords(final BoundingBox loadedBoundingBox,
    final List<LayerRecord> records) {
    synchronized (getSync()) {
      if (loadedBoundingBox == this.loadingBoundingBox) {
        final GeometryFactory geometryFactory = getGeometryFactory();
        final DataObjectQuadTree index = new DataObjectQuadTree(geometryFactory);
        for (final Record record : records) {
          final LayerRecord cacheRecord = addRecordToCache(this.cacheIdIndex,
            (LayerRecord)record);
          index.insert(cacheRecord);
        }
        cleanCachedRecords();
        final List<LayerRecord> newRecords = getNewRecords();
        index.insert(newRecords);
        setIndex(index);
        clearLoading(loadedBoundingBox);
      }
    }
    firePropertyChange("refresh", false, true);
  }

  public <V extends LayerRecord> List<V> setRecordsToCache(final Label cacheId,
    final Collection<? extends LayerRecord> records) {
    synchronized (getSync()) {
      this.cacheIdToRecordIdMap.put(cacheId, new HashSet<Identifier>());
      return addRecordsToCache(cacheId, records);
    }
  }

  @Override
  public void setSelectedRecords(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerRecord> records = getRecords(boundingBox);
      for (final Iterator<LayerRecord> iterator = records.iterator(); iterator.hasNext();) {
        final LayerRecord layerDataObject = iterator.next();
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
  public void setSelectedRecords(final Collection<LayerRecord> records) {
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
          final RecordDefinition metaData = dataStore.getRecordDefinition(typePath);
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
  public <V extends JComponent> V showForm(LayerRecord record) {
    synchronized (getSync()) {
      final Identifier identifier = getId(record);
      if (identifier != null) {
        addRecordToCache(this.cacheIdForm, record);
        record = getProxyRecord(record);
      }
      return super.showForm(record);
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
      final List<LayerRecord> records = getRecords(boundingBox);
      for (final Iterator<LayerRecord> iterator = records.iterator(); iterator.hasNext();) {
        final LayerRecord layerDataObject = iterator.next();
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
  public void unSelectRecords(final Collection<? extends LayerRecord> records) {
    super.unSelectRecords(records);
    removeRecordsFromCache(this.cacheIdSelected, records);
  }

}
