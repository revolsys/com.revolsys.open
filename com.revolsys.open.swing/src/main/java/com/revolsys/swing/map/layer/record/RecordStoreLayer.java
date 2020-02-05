package com.revolsys.swing.map.layer.record;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.SwingWorker;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.iterator.Iterators;
import com.revolsys.collection.map.IntegerCountMap;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.Writer;
import com.revolsys.predicate.Predicates;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.RecordState;
import com.revolsys.record.Records;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordStoreConnectionManager;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.In;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.functions.F;
import com.revolsys.record.query.functions.WithinDistance;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerErrors;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;
import com.revolsys.util.Property;
import com.revolsys.util.count.LabelCountMap;

public class RecordStoreLayer extends AbstractRecordLayer {
  private List<Set<Identifier>> cacheIdentifiers;

  private BoundingBox loadedBoundingBox = BoundingBox.empty();

  private BoundingBox loadingBoundingBox = BoundingBox.empty();

  private SwingWorker<List<LayerRecord>, Void> loadingWorker;

  /** Cache of records from {@link Record#getIdentifier()} to {@link Record}. */
  private Map<Identifier, RecordStoreLayerRecord> recordsByIdentifier = new HashMap<>();

  private IntegerCountMap<Identifier> recordCountsByIdentifier = new IntegerCountMap<>();

  private RecordStore recordStore;

  private PathName typePath;

  private List<RecordCacheRecordStoreLayer> recordStoreLayerCaches;

  public RecordStoreLayer() {
    this("recordStoreLayer");
  }

  public RecordStoreLayer(final Map<String, ? extends Object> properties) {
    this();
    setProperties(properties);
  }

  public RecordStoreLayer(final RecordStore recordStore, final PathName typePath,
    final boolean exists) {
    this();
    this.recordStore = recordStore;
    setExists(exists);

    setTypePath(typePath);
  }

  protected RecordStoreLayer(final String type) {
    super(type);
  }

  protected synchronized void addCachedIdentifiers(final Set<Identifier> identifiers) {
    if (this.cacheIdentifiers == null) {
      this.cacheIdentifiers = new ArrayList<>();
    }
    this.cacheIdentifiers.add(identifiers);
  }

  protected void addCachedRecord(final Identifier identifier, final LayerRecord record) {
    synchronized (getSync()) {
      this.recordsByIdentifier.put(identifier, (RecordStoreLayerRecord)record);
    }
  }

  protected void cancelLoading(final BoundingBox loadedBoundingBox) {
    synchronized (getSync()) {
      if (loadedBoundingBox == this.loadingBoundingBox) {
        firePropertyChange("loaded", false, true);
        this.loadedBoundingBox = BoundingBox.empty();
        this.loadingBoundingBox = BoundingBox.empty();
        this.loadingWorker = null;
      }
    }
  }

  public void decrementReferenceCount(final Identifier identifier) {
    synchronized (getSync()) {
      if (!this.recordCountsByIdentifier.decrementCount(identifier)) {
        this.recordsByIdentifier.remove(identifier);
      }
    }
  }

  @Override
  public void delete() {
    super.delete();
    if (this.recordStore != null) {
      final Map<String, String> connectionProperties = getProperty("connection");
      if (connectionProperties != null) {
        final Map<String, Object> config = new HashMap<>();
        config.put("connection", connectionProperties);
        RecordStoreConnectionManager.releaseRecordStore(config);
      }
      this.recordStore = null;
    }
    final SwingWorker<List<LayerRecord>, Void> loadingWorker = this.loadingWorker;
    this.loadedBoundingBox = BoundingBox.empty();
    this.loadingBoundingBox = BoundingBox.empty();
    this.loadingWorker = null;
    this.recordsByIdentifier.clear();
    if (loadingWorker != null) {
      loadingWorker.cancel(true);
    }
  }

  protected RecordStoreLayerRecord findCachedRecord(final LayerRecord record) {
    final Identifier identifier = record.getIdentifier();
    if (identifier == null) {
      return null;
    } else {
      return this.recordsByIdentifier.get(identifier);
    }
  }

  @Override
  protected void forEachRecordInternal(Query query, final Consumer<? super LayerRecord> consumer) {
    if (isExists()) {
      try {
        final RecordStore recordStore = getRecordStore();
        if (recordStore != null && query != null) {
          final Predicate<Record> filter = query.getWhereCondition();
          final Map<? extends CharSequence, Boolean> orderBy = query.getOrderBy();

          final List<LayerRecord> changedRecords = new ArrayList<>();
          changedRecords.addAll(getRecordsNew());
          changedRecords.addAll(getRecordsModified());
          Records.filterAndSort(changedRecords, filter, orderBy);
          final Iterator<LayerRecord> changedIterator = changedRecords.iterator();
          LayerRecord currentChangedRecord = Iterators.next(changedIterator);

          final RecordDefinition internalRecordDefinition = getInternalRecordDefinition();
          query = query.newQuery(internalRecordDefinition);
          final Comparator<Record> comparator = Records.newComparatorOrderBy(orderBy);
          try (
            final BaseCloseable booleanValueCloseable = eventsDisabled();
            Transaction transaction = recordStore.newTransaction(Propagation.REQUIRES_NEW);
            final RecordReader reader = newRecordStoreRecordReader(query);) {
            transaction.setRollbackOnly();
            for (LayerRecord record : reader.<LayerRecord> i()) {
              boolean write = true;
              final Identifier identifier = record.getIdentifier();
              if (identifier == null) {
                record = newProxyLayerRecordNoId(record);
              } else {
                final LayerRecord cachedRecord = findCachedRecord(record);
                if (cachedRecord != null) {
                  record = cachedRecord;
                  if (record.isChanged() || isDeleted(record) || isModified(record)) {
                    write = false;
                  }
                }
              }
              if (!isDeleted(record) && write) {
                while (currentChangedRecord != null
                  && comparator.compare(currentChangedRecord, record) < 0) {
                  consumer.accept(currentChangedRecord);
                  currentChangedRecord = Iterators.next(changedIterator);
                }
                consumer.accept(record);
              }
            }
            while (currentChangedRecord != null) {
              consumer.accept(currentChangedRecord);
              currentChangedRecord = Iterators.next(changedIterator);
            }
          }
        }
      } catch (final CancellationException e) {
      } catch (final RuntimeException e) {
        Logs.error(this, "Error executing query: " + query, e);
        throw e;
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <R extends LayerRecord> void forEachRecordsPersisted(final Query query,
    final Consumer<? super R> consumer) {
    if (query != null && isExists()) {
      final RecordStore recordStore = getRecordStore();
      if (recordStore != null) {
        try (
          final BaseCloseable booleanValueCloseable = eventsDisabled();
          Transaction transaction = recordStore.newTransaction(Propagation.REQUIRES_NEW);
          final RecordReader reader = newRecordStoreRecordReader(query);) {
          transaction.setRollbackOnly();
          final LabelCountMap labelCountMap = query.getProperty("statistics");
          for (final LayerRecord record : reader.<LayerRecord> i()) {
            final Identifier identifier = record.getIdentifier();
            R proxyRecord = null;
            if (identifier == null) {
              proxyRecord = (R)newProxyLayerRecordNoId(record);
            } else {
              synchronized (getSync()) {
                final LayerRecord cachedRecord = getCachedRecord(identifier, record, true);
                if (!cachedRecord.isDeleted()) {
                  proxyRecord = newProxyLayerRecord(identifier, cachedRecord);
                }
              }
            }
            if (proxyRecord != null) {
              consumer.accept(proxyRecord);
              if (labelCountMap != null) {
                labelCountMap.addCount(record);
              }
            }
          }
        }
      }
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    if (hasGeometryField()) {
      return getAreaBoundingBox();
    }
    return BoundingBox.empty();
  }

  @SuppressWarnings("unchecked")
  @Override
  protected <R extends LayerRecord> R getCachedRecord(final Identifier identifier) {
    final RecordDefinition recordDefinition = getInternalRecordDefinition();
    synchronized (getSync()) {
      LayerRecord record = this.recordsByIdentifier.get(identifier);
      if (record == null) {
        final List<String> idFieldNames = recordDefinition.getIdFieldNames();
        if (idFieldNames.isEmpty()) {
          return null;
        } else {
          final Condition where = getCachedRecordQuery(idFieldNames, identifier);
          final Query query = new Query(recordDefinition, where);
          final RecordStore recordStore = this.recordStore;
          if (recordStore != null) {
            try (
              Transaction transaction = recordStore.newTransaction(Propagation.REQUIRED);
              RecordReader reader = newRecordStoreRecordReader(query)) {
              transaction.setRollbackOnly();
              record = reader.getFirst();
              if (record != null) {
                addCachedRecord(identifier, record);
              }
            }
          }
        }
      }
      return (R)record;
    }
  }

  /**
   * Get the record from the cache if it exists, otherwise add this record to the
   * cache
   *
   * @param identifier
   * @param record
   */
  protected LayerRecord getCachedRecord(final Identifier identifier, final LayerRecord record,
    final boolean updateRecord) {
    assert !(record instanceof AbstractProxyLayerRecord);
    synchronized (getSync()) {
      final RecordStoreLayerRecord cachedRecord = findCachedRecord(record);
      if (cachedRecord == null) {
        addCachedRecord(identifier, record);
        return record;
      } else {
        if (updateRecord) {
          cachedRecord.refreshFromRecordStore(record);
        }
        return cachedRecord;
      }
    }
  }

  protected Condition getCachedRecordQuery(final List<String> idFieldNames,
    final Identifier identifier) {
    return Q.equalId(idFieldNames, identifier);
  }

  @Override
  public FieldDefinition getGeometryField() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getGeometryField();
    }
  }

  protected Identifier getId(final LayerRecord record) {
    if (isLayerRecord(record)) {
      return record.getIdentifier();
    } else {
      return null;
    }
  }

  protected RecordDefinition getInternalRecordDefinition() {
    return getRecordDefinition();
  }

  public BoundingBox getLoadingBoundingBox() {
    return this.loadingBoundingBox;
  }

  @Override
  public PathName getPathName() {
    return this.typePath;
  }

  @Override
  public LayerRecord getRecordById(final Identifier identifier) {
    final LayerRecord record = getCachedRecord(identifier);
    if (record == null) {
      return record;
    } else {
      return newProxyLayerRecord(identifier, record);
    }
  }

  @Override
  public int getRecordCount(final Query query) {
    int count = 0;
    count += Predicates.count(getRecordsNew(), query.getWhereCondition());
    count += getRecordCountChangeModified(query);
    count += getRecordCountPersisted(query);
    count -= Predicates.count(getRecordsDeleted(), query.getWhereCondition());
    return count;
  }

  /**
   * Get the count of the modified records where the original record did not match
   * the filter but the modified record does.
   *
   * @param query
   * @return
   */
  protected int getRecordCountChangeModified(final Query query) {
    final Condition filter = query.getWhereCondition();
    if (filter.isEmpty()) {
      return 0;
    } else {
      int count = 0;
      for (final LayerRecord record : getRecordsModified()) {
        final Record originalRecord = record.getOriginalRecord();
        final boolean modifiedMatches = filter.test(record);
        final boolean originalMatches = filter.test(originalRecord);
        if (modifiedMatches) {
          if (!originalMatches) {
            count++;
          }
        } else {
          if (originalMatches) {
            count--;
          }
        }
      }
      return count;
    }
  }

  @Override
  public int getRecordCountPersisted() {
    if (isExists()) {
      final RecordDefinition recordDefinition = getRecordDefinition();
      final Query query = new Query(recordDefinition);
      return getRecordCountPersisted(query);
    }
    return 0;
  }

  @Override
  public int getRecordCountPersisted(final Query query) {
    if (isExists()) {
      final RecordStore recordStore = getRecordStore();
      if (recordStore != null) {
        try (
          Transaction transaction = recordStore.newTransaction(Propagation.REQUIRES_NEW)) {
          return recordStore.getRecordCount(query);
        }
      }
    }
    return 0;
  }

  protected RecordDefinition getRecordDefinition(final PathName typePath) {
    if (typePath != null) {
      final RecordStore recordStore = getRecordStore();
      if (recordStore != null) {
        return recordStore.getRecordDefinition(typePath);
      }
    }
    return null;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  @Override
  public <R extends LayerRecord> List<R> getRecords(BoundingBox boundingBox) {
    if (hasGeometryField()) {
      boundingBox = convertBoundingBox(boundingBox);
      if (Property.hasValue(boundingBox)) {
        try (
          final BaseCloseable booleanValueCloseable = eventsDisabled()) {
          final BoundingBox queryBoundingBox = convertBoundingBox(boundingBox);
          boolean covers;
          synchronized (getSync()) {
            covers = this.loadedBoundingBox.bboxCovers(queryBoundingBox);
          }
          if (covers) {
            return getRecordsIndex(queryBoundingBox);
          } else {
            final List<R> records = (List)getRecordsPersisted(queryBoundingBox);
            return records;
          }
        }
      }
    }
    return Collections.emptyList();
  }

  @Override
  public <R extends LayerRecord> List<R> getRecords(final Geometry geometry,
    final double distance) {
    if (Property.isEmpty(geometry) || !hasGeometryField()) {
      return Collections.emptyList();
    } else {
      final RecordDefinition recordDefinition = getRecordDefinition();
      final FieldDefinition geometryField = getGeometryField();
      final WithinDistance where = F.dWithin(geometryField, geometry, distance);
      final Query query = new Query(recordDefinition, where);
      return getRecords(query);
    }
  }

  @Override
  public List<LayerRecord> getRecordsBackground(BoundingBox boundingBox) {
    if (hasGeometryField()) {
      boundingBox = convertBoundingBox(boundingBox);
      if (Property.hasValue(boundingBox)) {
        synchronized (getSync()) {
          final BoundingBox loadBoundingBox = boundingBox
            .bboxEdit(editor -> editor.expandPercent(0.2));
          if (!this.loadedBoundingBox.bboxCovers(boundingBox)
            && !this.loadingBoundingBox.bboxCovers(boundingBox)) {
            if (this.loadingWorker != null) {
              this.loadingWorker.cancel(true);
            }
            this.loadingBoundingBox = loadBoundingBox;
            this.loadingWorker = newLoadingWorker(loadBoundingBox);
            Invoke.worker(this.loadingWorker);
          }
        }
        final List<LayerRecord> records = getRecordsIndex(boundingBox);
        return records;
      }
    }
    return Collections.emptyList();
  }

  protected List<LayerRecord> getRecordsPersisted(final BoundingBox boundingBox) {
    final RecordDefinition recordDefinition = getInternalRecordDefinition();
    final Query query = Query.intersects(recordDefinition, boundingBox);
    return getRecords(query);
  }

  @Override
  public <R extends LayerRecord> List<R> getRecordsPersisted(final Query query) {
    final List<R> records = new ArrayList<>();
    final Consumer<R> consumer = records::add;
    forEachRecordsPersisted(query, consumer);
    return records;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <RS extends RecordStore> RS getRecordStore() {
    return (RS)this.recordStore;
  }

  public void incrementReferenceCount(final Identifier identifier) {
    synchronized (getSync()) {
      this.recordCountsByIdentifier.incrementCount(identifier);
    }
  }

  @Override
  protected boolean initializeDo() {
    RecordStore recordStore = this.recordStore;
    if (recordStore == null) {
      final Map<String, String> connectionProperties = getProperty("connection");
      if (connectionProperties == null) {
        Logs.error(this,
          "A record store layer requires a connection entry with a name or url, username, and password: "
            + getPath());
        return false;
      } else {
        final Map<String, Object> config = new HashMap<>();
        config.put("connection", connectionProperties);
        recordStore = RecordStoreConnectionManager.getRecordStore(config);

        if (recordStore == null) {
          Logs.error(this, "Unable to create record store for layer: " + getPath());
          return false;
        } else {
          try {
            recordStore.initialize();
          } catch (final Throwable e) {
            throw new RuntimeException("Unable to iniaitlize record store for layer " + getPath(),
              e);
          }

          setRecordStore(recordStore);
        }
      }
    }
    final PathName typePath = getPathName();
    RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      recordDefinition = getRecordDefinition(typePath);
      if (recordDefinition == null) {
        Logs.error(this, "Cannot find table " + typePath + " for layer " + getPath());
        return false;
      } else {
        final MapEx recordDefinitionProperties = getProperty("recordDefinitionProperties",
          MapEx.EMPTY);
        recordDefinition.setProperties(recordDefinitionProperties);
        setRecordDefinition(recordDefinition);
      }
    }
    return super.initializeDo();
  }

  public boolean isIdentifierCached(final Identifier identifier) {
    for (final Set<Identifier> identifiers : this.cacheIdentifiers) {
      if (identifiers.contains(identifier)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isReadOnly() {
    return !hasIdField() || super.isReadOnly();
  }

  protected Query newBoundingBoxQuery(BoundingBox boundingBox) {
    final RecordDefinition recordDefinition = getInternalRecordDefinition();
    final FieldDefinition geometryField = recordDefinition.getGeometryField();
    boundingBox = convertBoundingBox(boundingBox);
    if (geometryField == null || Property.isEmpty(boundingBox)) {
      return null;
    } else {
      Query query = getQuery();
      query = query.newQuery(recordDefinition);
      query.and(F.envelopeIntersects(geometryField, boundingBox));
      return query;
    }
  }

  @Override
  public LayerRecord newLayerRecord(final Map<String, ? extends Object> values) {
    if (!isReadOnly() && isEditable() && isCanAddRecords()) {

      final RecordDefinition recordDefinition = getRecordDefinition();
      final ArrayLayerRecord newRecord = new RecordStoreLayerRecord(this);
      if (values != null) {
        newRecord.setState(RecordState.INITIALIZING);
        final List<FieldDefinition> idFields = recordDefinition.getIdFields();
        for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
          if (!idFields.contains(fieldDefinition)) {
            final String fieldName = fieldDefinition.getName();
            final Object value = values.get(fieldName);
            fieldDefinition.setValue(newRecord, value);
          }
        }
        newRecord.setState(RecordState.NEW);
      }

      this.recordCacheNew.addRecord(newRecord);
      if (isEventsEnabled()) {
      }
      final LayerRecord proxyRecord = new NewProxyLayerRecord(this, newRecord);
      fireRecordInserted(proxyRecord);
      return proxyRecord;
    } else {
      return null;
    }
  }

  @Override
  protected LayerRecord newLayerRecord(final RecordDefinition recordDefinition) {
    final PathName layerTypePath = getPathName();
    if (recordDefinition.getPathName().equals(layerTypePath)) {
      return new RecordStoreLayerRecord(this);
    } else {
      throw new IllegalArgumentException("Cannot create records for " + recordDefinition);
    }
  }

  protected LoadingWorker newLoadingWorker(final BoundingBox boundingBox) {
    return new LoadingWorker(this, boundingBox);
  }

  @Override
  protected ValueField newPropertiesTabGeneralPanelSource(final BasePanel parent) {
    final ValueField panel = super.newPropertiesTabGeneralPanelSource(parent);
    final Map<String, String> connectionProperties = getProperty("connection");
    String connectionName = null;
    String url = null;
    String user = null;
    if (isExists()) {
      final RecordStore recordStore = getRecordStore();
      url = recordStore.getUrl();
      user = recordStore.getUsername();
    }
    if (connectionProperties != null) {
      connectionName = connectionProperties.get("name");
      if (!isExists()) {
        url = connectionProperties.get("url");
        user = connectionProperties.get("user");
      }
    }
    if (connectionName != null) {
      SwingUtil.addLabelledReadOnlyTextField(panel, "Record Store Name", connectionName);
    }
    if (url != null) {
      SwingUtil.addLabelledReadOnlyTextField(panel, "Record Store URL", url);
    }
    if (user != null) {
      SwingUtil.addLabelledReadOnlyTextField(panel, "Record Store Username", user);
    }
    SwingUtil.addLabelledReadOnlyTextField(panel, "Type Path", this.typePath);

    GroupLayouts.makeColumns(panel, 2, true);
    return panel;
  }

  @SuppressWarnings("unchecked")
  protected <V extends LayerRecord> V newProxyLayerRecord(final Identifier identifier) {
    return (V)new IdentifierProxyLayerRecord(this, identifier);
  }

  protected <V extends LayerRecord> V newProxyLayerRecord(final Identifier identifier,
    final LayerRecord cachedRecord) {
    return newProxyLayerRecord(identifier);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected <R extends LayerRecord> R newProxyLayerRecord(LayerRecord record) {
    if (record instanceof AbstractProxyLayerRecord) {
      // Already a proxy
    } else if (RecordState.NEW.equals(record.getState())) {
      record = new NewProxyLayerRecord(this, record);
    } else {
      final Identifier identifier = record.getIdentifier();
      if (identifier == null) {
        record = new NoIdProxyLayerRecord(this, record);
      } else {
        synchronized (getSync()) {
          addCachedRecord(identifier, record);
          record = newProxyLayerRecord(identifier);
        }
      }
    }
    return (R)record;
  }

  protected LayerRecord newProxyLayerRecordNoId(LayerRecord record) {
    final AbstractProxyLayerRecord proxyRecord = new NoIdProxyLayerRecord(this, record);
    final Identifier identifier = proxyRecord.getIdentifier();
    if (identifier != null) {
      this.recordsByIdentifier.put(identifier, (RecordStoreLayerRecord)record);
    }
    record = proxyRecord;
    return record;
  }

  @Override
  protected Collection<LayerRecord> newRecordCacheCollection() {
    return new ArrayList<>();
  }

  @Override
  protected RecordCache newRecordCacheDo(final String cacheId) {
    return newRecordCacheRecordStoreLayer(cacheId);
  }

  protected final RecordCacheRecordStoreLayer newRecordCacheRecordStoreLayer(final String cacheId) {
    if (this.recordStoreLayerCaches == null) {
      this.recordStoreLayerCaches = new ArrayList<>();
    }
    final RecordCacheCollection parentCache = newRecordCacheCollection(cacheId);
    final RecordCacheRecordStoreLayer recordCache = new RecordCacheRecordStoreLayer(cacheId, this,
      parentCache);
    addCachedIdentifiers(recordCache.identifiers);
    this.recordStoreLayerCaches.add(recordCache);
    return recordCache;
  }

  protected RecordReader newRecordStoreRecordReader(final Query query) {
    final RecordStore recordStore = getRecordStore();
    if (recordStore == null) {
      return RecordReader.empty();
    } else {
      final RecordFactory<LayerRecord> recordFactory = getRecordFactory();
      query.setRecordFactory(recordFactory);
      return recordStore.getRecords(query);
    }
  }

  @Override
  protected boolean postSaveDeletedRecord(final LayerRecord record) {
    final boolean deleted = super.postSaveDeletedRecord(record);
    if (deleted) {
      this.recordCacheDeletedInternal.removeContainsRecord(record);
    }
    return deleted;
  }

  protected void preDeleteRecord(final LayerRecord record) {
  }

  public void rebuildReferenceCounts() {
    final Map<Identifier, RecordStoreLayerRecord> recordsByIdentifier = new HashMap<>();
    final IntegerCountMap<Identifier> recordCountsByIdentifier = new IntegerCountMap<>();
    final Map<Identifier, RecordStoreLayerRecord> oldRecordsByIdentifier = this.recordsByIdentifier;
    for (final RecordCacheRecordStoreLayer recordCache : this.recordStoreLayerCaches) {
      for (final Identifier identifier : recordCache.getIdentifiers()) {
        final RecordStoreLayerRecord record = oldRecordsByIdentifier.get(identifier);
        recordsByIdentifier.put(identifier, record);
        recordCountsByIdentifier.incrementCount(identifier);
      }
    }
    this.recordsByIdentifier = recordsByIdentifier;
    this.recordCountsByIdentifier = recordCountsByIdentifier;
  }

  @Override
  protected void refreshDo() {
    synchronized (getSync()) {
      if (this.loadingWorker != null) {
        this.loadingWorker.cancel(true);
      }
      this.loadedBoundingBox = BoundingBox.empty();
      this.loadingBoundingBox = this.loadedBoundingBox;
      super.refreshDo();
    }
    final RecordStore recordStore = getRecordStore();
    final PathName pathName = getPathName();
    final CodeTable codeTable = recordStore.getCodeTable(pathName);
    if (codeTable != null) {
      codeTable.refresh();
    }
    if (hasIdField()) {
      final List<Identifier> identifiers = new ArrayList<>();
      synchronized (getSync()) {
        identifiers.addAll(this.recordsByIdentifier.keySet());
      }
      if (!identifiers.isEmpty()) {
        identifiers.sort(Identifier.comparator());
        final RecordDefinition recordDefinition = recordStore.getRecordDefinition(pathName);
        final List<FieldDefinition> idFields = recordDefinition.getIdFields();
        final int idFieldCount = idFields.size();
        if (idFieldCount == 1) {
          final FieldDefinition idField = idFields.get(0);
          final int pageSize = 999;
          final int identifierCount = identifiers.size();
          for (int i = 0; i < identifiers.size(); i += pageSize) {
            final List<Identifier> queryIdentifiers = identifiers.subList(i,
              Math.min(identifierCount, i + pageSize));
            final In in = Q.in(idField, queryIdentifiers);
            final Query query = new Query(recordDefinition, in);
            updateCachedRecords(recordStore, query);
          }
        } else if (!idFields.isEmpty()) {
          for (final Identifier identifier : identifiers) {
            final Query query = new Query(recordDefinition, Q.equalId(idFields, identifier));
            updateCachedRecords(recordStore, query);
          }
        }
      }
    }
  }

  void removeCachedRecord(final Identifier identifier) {
    this.recordsByIdentifier.remove(identifier);
  }

  private void removeFromRecordByIdentifier(final Identifier identifier) {
    synchronized (getSync()) {
      this.recordsByIdentifier.remove(identifier);
    }
  }

  @Override
  protected boolean removeRecordFromCache(final LayerRecord record) {
    final boolean removed = super.removeRecordFromCache(record);
    if (removed) {
      final Identifier identifier = record.getIdentifier();
      if (identifier != null) {
        removeFromRecordByIdentifier(identifier);
      }
    }
    return removed;
  }

  @Override
  public void revertChanges(final LayerRecord record) {
    this.recordCacheDeletedInternal.removeRecord(record);
    super.revertChanges(record);
  }

  @Override
  protected boolean saveChangesDo(final RecordLayerErrors errors, final LayerRecord record) {
    boolean deleted = super.isDeleted(record);

    if (isExists()) {
      final RecordStore recordStore = getRecordStore();
      if (recordStore == null) {
        return true;
      } else {
        try (
          Transaction transaction = recordStore.newTransaction(Propagation.REQUIRES_NEW)) {
          try {
            Identifier identifier = record.getIdentifier();
            try (
              final Writer<Record> writer = recordStore.newRecordWriter()) {
              if (this.recordCacheDeleted.containsRecord(record) || super.isDeleted(record)) {
                preDeleteRecord(record);
                record.setState(RecordState.DELETED);
                writeDelete(writer, record);
                deleted = true;
              } else {
                final RecordDefinition recordDefinition = getRecordDefinition();
                if (super.isNew(record)) {
                  final List<String> idFieldNames = recordDefinition.getIdFieldNames();
                  if (identifier == null && !idFieldNames.isEmpty()) {
                    identifier = recordStore.newPrimaryIdentifier(this.typePath);
                    if (identifier != null) {
                      identifier.setIdentifier(record, idFieldNames);
                    }
                  }
                }
                final int fieldCount = recordDefinition.getFieldCount();
                for (int fieldIndex = 0; fieldIndex < fieldCount; fieldIndex++) {
                  record.validateField(fieldIndex);
                }
                if (super.isModified(record)) {
                  writeUpdate(writer, record);
                } else if (super.isNew(record)) {
                  writer.write(record);
                }
              }
            }
            if (!deleted) {
              record.setState(RecordState.PERSISTED);
            }
            removeFromRecordByIdentifier(identifier);
            return true;
          } catch (final Throwable e) {
            throw transaction.setRollbackOnly(e);
          }
        }
      }
    } else {
      return true;
    }
  }

  protected void setIndexRecords(final BoundingBox loadedBoundingBox,
    final List<LayerRecord> records) {
    synchronized (getSync()) {
      if (loadedBoundingBox == this.loadingBoundingBox) {
        setIndexRecords(records);
        firePropertyChange("loaded", false, true);
        this.loadedBoundingBox = this.loadingBoundingBox;
        this.loadingBoundingBox = BoundingBox.empty();
        this.loadingWorker = null;
      }
    }
    firePropertyChange("refresh", false, true);
  }

  @Override
  public void setProperty(final String name, final Object value) {
    if ("typePath".equals(name)) {
      super.setProperty(name, PathName.newPathName(value));
    } else {
      super.setProperty(name, value);
    }
  }

  public void setRecordsToCache(final RecordCache recordCache,
    final Iterable<? extends LayerRecord> records) {
    synchronized (getSync()) {
      recordCache.setRecords(records);
    }
  }

  protected void setRecordStore(final RecordStore recordStore) {
    this.recordStore = recordStore;
  }

  public void setTypePath(final PathName typePath) {
    this.typePath = typePath;
    if (this.typePath != null) {
      if (!Property.hasValue(getName())) {
        setName(this.typePath.getName());
      }
    }
    if (isExists()) {
      final RecordDefinition recordDefinition = getRecordDefinition(typePath);
      setRecordDefinition(recordDefinition);
    }
  }

  @Override
  public void showForm(final LayerRecord record, final String fieldName) {
    if (record != null) {
      final Identifier identifier = getId(record);
      if (identifier != null) {
        this.recordCacheForm.addRecord(record);
      }
      final LayerRecord proxyRecord = record.newRecordProxy();
      super.showForm(proxyRecord, fieldName);
    }
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    addToMap(map, "typePath", this.typePath);
    return map;
  }

  private void updateCachedRecords(final RecordStore recordStore, final Query query) {
    try (
      Transaction transaction = recordStore.newTransaction(Propagation.REQUIRES_NEW);
      RecordReader reader = recordStore.getRecords(query)) {
      for (final Record record : reader) {
        final Identifier identifier = record.getIdentifier();
        final RecordStoreLayerRecord cachedRecord = this.recordsByIdentifier.get(identifier);
        if (cachedRecord != null) {
          cachedRecord.refreshFromRecordStore(record);
        }
      }
    }
  }

  protected void writeDelete(final Writer<Record> writer, final LayerRecord record) {
    writer.write(record);
  }

  protected void writeUpdate(final Writer<Record> writer, final LayerRecord record) {
    writer.write(record);
  }

}
