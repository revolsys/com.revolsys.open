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
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.JComponent;
import javax.swing.SwingWorker;

import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.filter.RecordGeometryBoundingBoxIntersectsFilter;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.identifier.SingleIdentifier;
import com.revolsys.data.io.RecordStore;
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
import com.revolsys.gis.algorithm.index.RecordQuadTree;
import com.revolsys.gis.io.Statistics;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.io.datastore.RecordStoreConnectionManager;
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
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.Label;

public class RecordStoreLayer extends AbstractRecordLayer {

  public static AbstractRecordLayer create(final Map<String, Object> properties) {
    return new RecordStoreLayer(properties);
  }

  public static final MapObjectFactory FACTORY = new InvokeMethodMapObjectFactory(
    "dataStore", "Data Store", RecordStoreLayer.class, "create");

  private BoundingBox boundingBox = new BoundingBoxDoubleGf();

  /**
   * Caches of sets of {@link Record#getIdentifier()} for different purposes (e.g. selected records, deleted records).
   * Each cache has a separate cacheId. The cache id is recommended to be a private variable to prevent modification
   * of that cache.
   */
  private final Map<Label, Set<Identifier>> cacheIdToRecordIdMap = new WeakHashMap<>();

  private RecordStore dataStore;

  private final Label cacheIdForm = new Label("form");

  private BoundingBox loadingBoundingBox = new BoundingBoxDoubleGf();

  private SwingWorker<List<LayerRecord>, Void> loadingWorker;

  /** Cache of records from {@link Record#getIdentifier()} to {@link Record}. */
  private final Map<Identifier, LayerRecord> recordIdToRecordMap = new WeakHashMap<>();

  private String typePath;

  public RecordStoreLayer(final RecordStore dataStore,
    final String typePath, final boolean exists) {
    this.dataStore = dataStore;
    setExists(exists);
    setType("dataStore");

    setRecordDefinition(dataStore.getRecordDefinition(typePath));
    setTypePath(typePath);
  }

  public RecordStoreLayer(final Map<String, ? extends Object> properties) {
    super(properties);
    setType("dataStore");
  }

  @Override
  protected ValueField addPropertiesTabGeneralPanelSource(final BasePanel parent) {
    final ValueField panel = super.addPropertiesTabGeneralPanelSource(parent);
    final Map<String, String> connectionProperties = getProperty("connection");
    String connectionName = null;
    String url = null;
    String username = null;
    if (isExists()) {
      final RecordStore dataStore = getDataStore();
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
      } else if (record instanceof AbstractProxyLayerRecord) {
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

  @Override
  public LayerRecord addRecordToCache(final Label cacheId,
    final LayerRecord record) {
    if (isLayerRecord(record)) {
      if (record.getState() == RecordState.Deleted && !isDeleted(record)) {
        return record;
      } else {
        final Identifier identifier = record.getIdentifier();
        if (identifier == null) {
          return super.addRecordToCache(cacheId, record);
        } else {
          synchronized (getSync()) {
            LayerRecord proxyRecord;
            if (record instanceof AbstractProxyLayerRecord) {
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
    return record;
  }

  /**
   * Remove any cached records that are currently not used.
   */
  @Override
  protected void cleanCachedRecords() {
    synchronized (getSync()) {
      super.cleanCachedRecords();
      final Set<Identifier> ids = new HashSet<>();
      for (final Set<Identifier> recordIds : this.cacheIdToRecordIdMap.values()) {
        if (recordIds != null) {
          ids.addAll(recordIds);
        }
      }
      this.recordIdToRecordMap.keySet().retainAll(ids);
    }
    System.out.println();
    for (final Entry<Label, Set<Identifier>> entry : this.cacheIdToRecordIdMap.entrySet()) {
      final Label key = entry.getKey();
      if (key != getCacheIdIndex()) {
        System.out.println(getTypePath() + "\tD\t" + key + "\t"
          + entry.getValue());
      }
    }

  }

  @Override
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

  protected LoadingWorker createLoadingWorker(final BoundingBox boundingBox) {
    return new LoadingWorker(this, boundingBox);
  }

  @SuppressWarnings("unchecked")
  protected <V extends LayerRecord> V createProxyRecord(
    final Identifier identifier) {
    return (V)new IdentifierProxyLayerRecord(this, identifier);
  }

  @Override
  public LayerRecord createRecord(final Map<String, Object> values) {
    if (!isReadOnly() && isEditable() && isCanAddRecords()) {
      final LayerRecord record = new NewProxyLayerRecord(this, values);
      addRecordToCache(getCacheIdNew(), record);
      cleanCachedRecords();
      fireRecordInserted(record);
      return record;
    } else {
      return null;
    }
  }

  @Override
  public void delete() {
    if (this.dataStore != null) {
      final Map<String, String> connectionProperties = getProperty("connection");
      if (connectionProperties != null) {
        final Map<String, Object> config = new HashMap<String, Object>();
        config.put("connection", connectionProperties);
        RecordStoreConnectionManager.releaseDataStore(config);
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
      final RecordStore dataStore = RecordStoreConnectionManager.getDataStore(config);

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

        final RecordDefinition recordDefinition = dataStore.getRecordDefinition(typePath);

        if (recordDefinition == null) {
          LoggerFactory.getLogger(getClass()).error(
            "Cannot find table " + typePath + " for layer " + getPath());
        } else {
          setRecordDefinition(recordDefinition);
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
      final RecordDefinition recordDefinition = getRecordDefinition();
      final Attribute geometryAttribute = getGeometryAttribute();
      final WithinDistance where = F.dWithin(geometryAttribute, geometry,
        distance);
      final Query query = new Query(recordDefinition, where);
      return query(query);
    }
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  @Override
  protected List<LayerRecord> doQuery(final Query query) {
    if (isExists()) {
      final RecordStore recordStore = getDataStore();
      if (recordStore != null) {
        final boolean enabled = setEventsEnabled(false);
        try {
          final Statistics statistics = query.getProperty("statistics");
          query.setProperty("recordFactory", this);
          try (
            final Reader<LayerRecord> reader = (Reader)recordStore.query(query)) {
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
      final RecordQuadTree index = getIndex();

      final List<LayerRecord> records = (List)index.queryIntersects(boundingBox);

      final Filter filter = new RecordGeometryBoundingBoxIntersectsFilter(
        boundingBox);
      for (final ListIterator<LayerRecord> iterator = records.listIterator(); iterator.hasNext();) {
        final LayerRecord record = iterator.next();
        if (!filter.accept(record)) {
          iterator.remove();
        }
      }
      return records;
    }
  }

  @Override
  protected boolean doSaveChanges(final LayerRecord record) {
    final boolean deleted = isDeleted(record);
    final RecordStore dataStore = getDataStore();
    final PlatformTransactionManager transactionManager = dataStore.getTransactionManager();
    try (
      Transaction transaction = new Transaction(transactionManager,
        Propagation.REQUIRES_NEW)) {
      try {

        if (isExists()) {
          if (dataStore != null) {
            try (
              final Writer<Record> writer = dataStore.createWriter()) {
              if (isCached(this.getCacheIdDeleted(), record)
                || super.isDeleted(record)) {
                preDeleteRecord(record);
                record.setState(RecordState.Deleted);
                writer.write(record);
              } else if (super.isModified(record)) {
                writer.write(record);
              } else if (isNew(record)) {
                Identifier identifier = record.getIdentifier();
                final RecordDefinition recordDefinition = getRecordDefinition();
                final List<String> idAttributeNames = recordDefinition.getIdAttributeNames();
                if (identifier == null && !idAttributeNames.isEmpty()) {
                  final Object idValue = dataStore.createPrimaryIdValue(this.typePath);
                  if (idValue != null) {
                    identifier = SingleIdentifier.create(idValue);
                    identifier.setIdentifier(record, idAttributeNames);
                  }
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

  @SuppressWarnings("unchecked")
  @Override
  public <V extends LayerRecord> V getCachedRecord(final Identifier identifier) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final List<String> idAttributeNames = recordDefinition.getIdAttributeNames();
    if (idAttributeNames.isEmpty()) {
      LoggerFactory.getLogger(getClass()).error(
        this.typePath + " does not have a primary key");
      return null;
    } else {
      LayerRecord record = this.recordIdToRecordMap.get(identifier);
      if (record == null) {
        final Condition where = Q.equal(identifier, idAttributeNames);
        final Query query = new Query(recordDefinition, where);
        query.setProperty("recordFactory", this);
        final RecordStore dataStore = getDataStore();
        if (dataStore == null) {
          return null;
        } else {
          record = (LayerRecord)dataStore.queryFirst(query);
          this.recordIdToRecordMap.put(identifier, record);
        }
      }
      return (V)record;
    }
  }

  @Override
  public int getCachedRecordCount(final Label cacheId) {
    int count = super.getCachedRecordCount(cacheId);
    final Set<Identifier> identifiers = this.cacheIdToRecordIdMap.get(cacheId);
    if (identifiers != null) {
      count += identifiers.size();
    }
    return count;
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

  @Override
  public List<LayerRecord> getCachedRecords(final Label cacheId) {
    synchronized (getSync()) {
      final List<LayerRecord> records = super.getCachedRecords(cacheId);
      final Set<Identifier> recordIds = this.cacheIdToRecordIdMap.get(cacheId);
      if (recordIds != null) {
        for (final Identifier recordId : recordIds) {
          final LayerRecord record = getRecordById(recordId);
          if (record != null) {
            records.add(record);
          }
        }
      }
      return records;
    }
  }

  @Override
  public RecordStore getDataStore() {
    return this.dataStore;
  }

  public Attribute getGeometryAttribute() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getGeometryAttribute();
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

  @Override
  public LayerRecord getRecordById(final Identifier id) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final List<String> idAttributeNames = recordDefinition.getIdAttributeNames();
    if (idAttributeNames.isEmpty()) {
      LoggerFactory.getLogger(getClass()).error(
        this.typePath + " does not have a primary key");
      return null;
    } else {
      LayerRecord record = this.recordIdToRecordMap.get(id);
      if (record == null) {
        final Condition where = Q.equal(id, idAttributeNames);
        final Query query = new Query(recordDefinition, where);
        query.setProperty("recordFactory", this);
        final RecordStore dataStore = getDataStore();
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
    RecordQuadTree index;
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

  protected List<LayerRecord> getRecordsFromDataStore(
    final BoundingBox boundingBox) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final Attribute geometryAttribute = getGeometryAttribute();
    final Query query = new Query(recordDefinition, F.envelopeIntersects(
      geometryAttribute, boundingBox));
    return query(query);
  }

  @Override
  public int getRowCount(final Query query) {
    if (isExists()) {
      final RecordStore dataStore = getDataStore();
      if (dataStore != null) {
        return dataStore.getRowCount(query);
      }
    }
    return 0;
  }

  public String getTypePath() {
    return this.typePath;
  }

  public boolean isCached(final Label cacheId, final LayerRecord record) {
    if (isLayerRecord(record)) {
      final Identifier identifier = record.getIdentifier();
      if (CollectionUtil.setContains(this.cacheIdToRecordIdMap, cacheId,
        identifier)) {
        return true;
      } else {
        return false;
      }
    }
    return false;
  }

  @Override
  public boolean isLayerRecord(final Record record) {
    if (record instanceof LayerRecord) {
      final LayerRecord layerRecord = (LayerRecord)record;
      if (layerRecord.getLayer() == this) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isRecordCached(final Label cacheId, final LayerRecord record) {
    if (isLayerRecord(record)) {
      synchronized (getSync()) {
        final Identifier identifier = record.getIdentifier();
        if (identifier != null) {
          final Set<Identifier> identifiers = this.cacheIdToRecordIdMap.get(cacheId);
          if (identifiers != null) {
            if (identifiers.contains(identifier)) {
              return true;
            }
          }
        }
        return super.isRecordCached(cacheId, record);
      }
    }
    return false;
  }

  @Override
  protected void postSaveChanges(final RecordState originalState,
    final LayerRecord record) {
    super.postSaveChanges(originalState, record);
    record.postSaveChanges();
  }

  @Override
  protected boolean postSaveDeletedRecord(final LayerRecord record) {
    final boolean deleted = super.postSaveDeletedRecord(record);
    if (deleted) {
      removeRecordFromCache(this.getCacheIdDeleted(), record);
    }
    return deleted;
  }

  protected void preDeleteRecord(final LayerRecord record) {
  }

  public List<LayerRecord> query(final Map<String, ? extends Object> filter) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final Query query = Query.and(recordDefinition, filter);
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
      setIndexRecords(null);
      cleanCachedRecords();
    }
    final RecordStore dataStore = getDataStore();
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

  @Override
  public boolean removeRecordFromCache(final Label cacheId,
    final LayerRecord record) {
    boolean removed = false;
    if (isLayerRecord(record)) {
      final Identifier identifier = record.getIdentifier();
      if (identifier != null) {
        synchronized (getSync()) {
          removed = CollectionUtil.removeFromSet(this.cacheIdToRecordIdMap,
            cacheId, identifier);
        }
      }
    }
    removed |= super.removeRecordFromCache(cacheId, record);
    return removed;
  }

  @Override
  public boolean removeRecordFromCache(final LayerRecord record) {
    synchronized (getSync()) {
      boolean removed = false;
      if (isLayerRecord(record)) {
        for (final Label cacheId : new ArrayList<>(
          this.cacheIdToRecordIdMap.keySet())) {
          removed |= removeRecordFromCache(cacheId, record);
        }
      }
      removed |= super.removeRecordFromCache(record);
      return removed;
    }
  }

  @Override
  public void revertChanges(final LayerRecord record) {
    removeRecordFromCache(this.getCacheIdDeleted(), record);
    super.revertChanges(record);
  }

  protected void setDataStore(final RecordStore dataStore) {
    this.dataStore = dataStore;
  }

  protected void setIndexRecords(final BoundingBox loadedBoundingBox,
    final List<LayerRecord> records) {
    synchronized (getSync()) {
      if (loadedBoundingBox == this.loadingBoundingBox) {
        setIndexRecords(records);
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

  public void setTypePath(final String typePath) {
    this.typePath = typePath;
    if (!StringUtils.hasText(getName())) {
      setName(PathUtil.getName(typePath));
    }
    if (StringUtils.hasText(typePath)) {
      if (isExists()) {
        final RecordStore dataStore = getDataStore();
        if (dataStore != null) {
          final RecordDefinition recordDefinition = dataStore.getRecordDefinition(typePath);
          if (recordDefinition != null) {

            setRecordDefinition(recordDefinition);
            setQuery(new Query(recordDefinition));
            return;
          }
        }
      }
    }
    setRecordDefinition(null);
    setQuery(null);
  }

  @Override
  public <V extends JComponent> V showForm(LayerRecord record) {
    synchronized (getSync()) {
      final Identifier identifier = getId(record);
      if (identifier != null) {
        record = addRecordToCache(this.cacheIdForm, record);
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
        final LayerRecord layerRecord = iterator.next();
        if (!isVisible(layerRecord) || super.isDeleted(layerRecord)) {
          iterator.remove();
        }
      }
      if (!records.isEmpty()) {
        showRecordsTable(RecordLayerTableModel.MODE_SELECTED);
      }
      unSelectRecords(records);
    }
  }

}
