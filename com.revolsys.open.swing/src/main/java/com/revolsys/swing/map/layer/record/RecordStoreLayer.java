package com.revolsys.swing.map.layer.record;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.SwingWorker;

import org.slf4j.LoggerFactory;

import com.revolsys.collection.iterator.Iterators;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.algorithm.index.RecordQuadTree;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.gis.io.Statistics;
import com.revolsys.identifier.Identifier;
import com.revolsys.io.PathName;
import com.revolsys.io.Writer;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.predicate.Predicates;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.RecordState;
import com.revolsys.record.Records;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordStoreConnectionManager;
import com.revolsys.record.query.Condition;
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
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.record.table.model.RecordSaveErrorTableModel;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;
import com.revolsys.util.Label;
import com.revolsys.util.Property;
import com.revolsys.util.enableable.BooleanValueCloseable;

public class RecordStoreLayer extends AbstractRecordLayer {
  public static AbstractLayer newLayer(final Map<String, Object> properties) {
    return new RecordStoreLayer(properties);
  }

  private BoundingBox loadedBoundingBox = BoundingBox.EMPTY;

  private BoundingBox loadingBoundingBox = BoundingBox.EMPTY;

  private SwingWorker<List<LayerRecord>, Void> loadingWorker;

  /**
   * Caches of sets of {@link Record#getIdentifier()} for different purposes (e.g. selected records, deleted records).
   * Each cache has a separate cacheId. The cache id is recommended to be a private variable to prevent modification
   * of that cache.
   */
  private Map<Label, Set<Identifier>> recordIdentifiersByCacheId = new WeakHashMap<>();

  /** Cache of records from {@link Record#getIdentifier()} to {@link Record}. */
  private Map<Identifier, LayerRecord> recordsByIdentifier = new WeakHashMap<>();

  private RecordStore recordStore;

  private PathName typePath;

  public RecordStoreLayer(final Map<String, ? extends Object> properties) {
    super(properties);
    setType("recordStoreLayer");
  }

  public RecordStoreLayer(final RecordStore recordStore, final PathName typePath,
    final boolean exists) {
    this.recordStore = recordStore;
    setExists(exists);
    setType("recordStoreLayer");

    final RecordDefinition recordDefinition = recordStore.getRecordDefinition(typePath);
    setTypePath(typePath);
    setRecordDefinition(recordDefinition);
  }

  @Override
  protected LayerRecord addRecordToCache(final Label cacheId, final LayerRecord record) {
    if (isLayerRecord(record)) {
      if (record.getState() == RecordState.DELETED && !isDeleted(record)) {
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
              getCachedRecord(identifier, record);
              proxyRecord = newProxyLayerRecord(identifier);
            }
            Maps.addToSet(this.recordIdentifiersByCacheId, cacheId, identifier);
            return proxyRecord;
          }
        }
      }
    }
    return record;
  }

  protected Set<Identifier> cleanCachedRecordIds() {
    final Set<Identifier> identifiers = new HashSet<>();
    for (final Set<Identifier> recordIds : this.recordIdentifiersByCacheId.values()) {
      if (recordIds != null) {
        identifiers.addAll(recordIds);
      }
    }
    addProxiedRecordIdsToCollection(identifiers);
    return identifiers;
  }

  /**
  * Remove any cached records that are currently not used.
  */
  @Override
  protected void cleanCachedRecords() {
    synchronized (getSync()) {
      super.cleanCachedRecords();
      final Set<Identifier> identifiers = cleanCachedRecordIds();
      synchronized (this.recordsByIdentifier) {
        this.recordsByIdentifier.keySet().retainAll(identifiers);
      }
    }
  }

  @Override
  public void clearCachedRecords(final Label cacheId) {
    synchronized (getSync()) {
      super.clearCachedRecords(cacheId);
      this.recordIdentifiersByCacheId.remove(cacheId);
    }
  }

  protected void clearLoading(final BoundingBox loadedBoundingBox) {
    synchronized (getSync()) {
      if (loadedBoundingBox == this.loadingBoundingBox) {
        firePropertyChange("loaded", false, true);
        this.loadedBoundingBox = this.loadingBoundingBox;
        this.loadingBoundingBox = BoundingBox.EMPTY;
        this.loadingWorker = null;
      }

    }
  }

  @Override
  public RecordStoreLayer clone() {
    final RecordStoreLayer clone = (RecordStoreLayer)super.clone();
    clone.recordIdentifiersByCacheId = new WeakHashMap<>();
    clone.loadedBoundingBox = BoundingBox.EMPTY;
    clone.loadingBoundingBox = BoundingBox.EMPTY;
    clone.loadingWorker = null;
    clone.recordsByIdentifier = new WeakHashMap<>();
    return clone;
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
    this.recordIdentifiersByCacheId = Collections.emptyMap();
    this.loadedBoundingBox = BoundingBox.EMPTY;
    this.loadingBoundingBox = BoundingBox.EMPTY;
    this.loadingWorker = null;
    this.recordsByIdentifier = Collections.emptyMap();
    this.typePath = null;
    if (loadingWorker != null) {
      loadingWorker.cancel(true);
    }
  }

  @Override
  protected void deleteRecordDo(final LayerRecord record) {
    final Identifier identifier = record.getIdentifier();
    super.deleteRecordDo(record);
    removeFromRecordIdToRecordMap(identifier);
  }

  @Override
  protected boolean doInitialize() {
    RecordStore recordStore = this.recordStore;
    if (recordStore == null) {
      final Map<String, String> connectionProperties = getProperty("connection");
      if (connectionProperties == null) {
        LoggerFactory.getLogger(getClass()).error(
          "A record store layer requires a connectionProperties entry with a name or url, username, and password: "
            + getPath());
        return false;
      } else {
        final Map<String, Object> config = new HashMap<>();
        config.put("connection", connectionProperties);
        recordStore = RecordStoreConnectionManager.getRecordStore(config);

        if (recordStore == null) {
          LoggerFactory.getLogger(getClass())
            .error("Unable to create record store for layer: " + getPath());
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
    final PathName typePath = getTypePath();
    RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      recordDefinition = recordStore.getRecordDefinition(typePath);
      if (recordDefinition == null) {
        recordDefinition = recordStore.getRecordDefinition(typePath);
        LoggerFactory.getLogger(getClass())
          .error("Cannot find table " + typePath + " for layer " + getPath());
        return false;
      } else {
        setRecordDefinition(recordDefinition);
      }
    }
    initRecordMenu();
    return true;
  }

  @Override
  protected void doRefresh() {
    synchronized (getSync()) {
      if (this.loadingWorker != null) {
        this.loadingWorker.cancel(true);
      }
      this.loadedBoundingBox = BoundingBox.EMPTY;
      this.loadingBoundingBox = this.loadedBoundingBox;
      setIndexRecords(null);
      cleanCachedRecords();
    }
    final RecordStore recordStore = getRecordStore();
    final PathName typePath = getTypePath();
    final CodeTable codeTable = recordStore.getCodeTable(typePath);
    if (codeTable != null) {
      codeTable.refresh();
    }
    super.doRefresh();
  }

  @Override
  protected boolean doSaveChanges(final RecordSaveErrorTableModel errors,
    final LayerRecord record) {
    final boolean deleted = super.isDeleted(record);

    if (isExists()) {
      if (this.recordStore != null) {
        final RecordStore recordStore = getRecordStore();
        try (
          Transaction transaction = recordStore.newTransaction(Propagation.REQUIRES_NEW)) {
          try {
            Identifier identifier = record.getIdentifier();
            try (
              final Writer<Record> writer = recordStore.newRecordWriter()) {
              if (isRecordCached(getCacheIdDeleted(), record) || super.isDeleted(record)) {
                preDeleteRecord(record);
                record.setState(RecordState.DELETED);
                writeDelete(writer, record);
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
            removeFromRecordIdToRecordMap(identifier);
            return true;
          } catch (final Throwable e) {
            throw transaction.setRollbackOnly(e);
          }
        }
      }
    }
    return false;
  }

  @Override
  protected void forEachRecord(Query query, final Consumer<? super LayerRecord> consumer) {
    if (isExists()) {
      final RecordStore recordStore = getRecordStore();
      if (recordStore != null && query != null) {
        final Predicate<Record> filter = query.getWhereCondition();
        final Map<String, Boolean> orderBy = query.getOrderBy();

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
          final BooleanValueCloseable booleanValueCloseable = eventsDisabled();
          final RecordReader reader = newRecordStoreRecordReader(query);) {
          for (LayerRecord record : reader.<LayerRecord> i()) {
            boolean write = true;
            final Identifier identifier = getId(record);
            if (identifier != null) {
              final LayerRecord cachedRecord = this.recordsByIdentifier.get(identifier);
              if (cachedRecord != null) {
                record = cachedRecord;
                if (record.isChanged()) {
                  write = false;
                }
              }
            }
            if (write) {
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
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    if (hasGeometryField()) {
      final CoordinateSystem coordinateSystem = getCoordinateSystem();
      if (coordinateSystem != null) {
        return coordinateSystem.getAreaBoundingBox();
      }
    }
    return BoundingBox.EMPTY;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected <R extends LayerRecord> R getCachedRecord(final Identifier identifier) {
    final RecordDefinition recordDefinition = getInternalRecordDefinition();
    final List<String> idFieldNames = recordDefinition.getIdFieldNames();
    if (idFieldNames.isEmpty()) {
      LoggerFactory.getLogger(getClass()).error(this.typePath + " does not have a primary key");
      return null;
    } else {
      synchronized (this.recordsByIdentifier) {
        LayerRecord record = this.recordsByIdentifier.get(identifier);
        if (record == null) {
          final Condition where = getCachedRecordQuery(idFieldNames, identifier);
          final Query query = new Query(recordDefinition, where);
          try (
            RecordReader reader = newRecordStoreRecordReader(query)) {
            record = reader.getFirst();
            if (record != null) {
              this.recordsByIdentifier.put(identifier, record);
            }
          }
        }
        return (R)record;
      }
    }
  }

  /**
   * Get the record from the cache if it exists, otherwise add this record to the cache
   *
   * @param identifier
   * @param record
   */
  protected LayerRecord getCachedRecord(final Identifier identifier, final LayerRecord record) {
    assert!(record instanceof AbstractProxyLayerRecord);
    synchronized (this.recordsByIdentifier) {
      final LayerRecord cachedRecord = this.recordsByIdentifier.get(identifier);
      if (cachedRecord == null) {
        this.recordsByIdentifier.put(identifier, record);
        return record;
      } else {
        // TODO see if it has been updated and refresh values if appropriate
        return cachedRecord;
      }
    }
  }

  protected Condition getCachedRecordQuery(final List<String> idFieldNames,
    final Identifier identifier) {
    return Q.equalId(idFieldNames, identifier);
  }

  @Override
  protected Set<Label> getCacheIdsDo(final LayerRecord record) {
    final Set<Label> cacheIds = super.getCacheIdsDo(record);
    final Identifier identifier = record.getIdentifier();
    if (identifier != null) {
      for (final Entry<Label, Set<Identifier>> entry : this.recordIdentifiersByCacheId.entrySet()) {
        final Label cacheId = entry.getKey();
        if (!cacheIds.contains(cacheId)) {
          final Collection<Identifier> identifiers = entry.getValue();
          if (identifiers.contains(identifier)) {
            cacheIds.add(cacheId);
          }
        }
      }
    }
    return cacheIds;
  }

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
  public LayerRecord getRecordById(final Identifier identifier) {
    final LayerRecord record = getCachedRecord(identifier);
    if (record == null) {
      return record;
    } else {
      return newProxyLayerRecord(identifier);
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

  @Override
  public int getRecordCountCached(final Label cacheId) {
    int count = super.getRecordCountCached(cacheId);
    final Set<Identifier> identifiers = this.recordIdentifiersByCacheId.get(cacheId);
    if (identifiers != null) {
      count += identifiers.size();
    }
    return count;
  }

  /**
   * Get the count of the modified records where the original record did not match the filter but
   * the modified record does.
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
        return recordStore.getRecordCount(query);
      }
    }
    return 0;
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
          final BooleanValueCloseable booleanValueCloseable = eventsDisabled()) {
          final BoundingBox queryBoundingBox = convertBoundingBox(boundingBox);
          if (this.loadedBoundingBox.covers(queryBoundingBox)) {
            return (List)getIndex().queryIntersects(queryBoundingBox);
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
    if (geometry == null || !hasGeometryField()) {
      return Collections.emptyList();
    } else {
      final RecordDefinition recordDefinition = getRecordDefinition();
      final FieldDefinition geometryField = getGeometryField();
      final WithinDistance where = F.dWithin(geometryField, geometry, distance);
      final Query query = new Query(recordDefinition, where);
      return getRecords(query);
    }
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  @Override
  public List<LayerRecord> getRecordsBackground(BoundingBox boundingBox) {
    if (hasGeometryField()) {
      boundingBox = convertBoundingBox(boundingBox);
      if (Property.hasValue(boundingBox)) {
        synchronized (getSync()) {
          final BoundingBox loadBoundingBox = boundingBox.expandPercent(0.2);
          if (!this.loadedBoundingBox.covers(boundingBox)
            && !this.loadingBoundingBox.covers(boundingBox)) {
            if (this.loadingWorker != null) {
              this.loadingWorker.cancel(true);
            }
            this.loadingBoundingBox = loadBoundingBox;
            this.loadingWorker = newLoadingWorker(loadBoundingBox);
            Invoke.worker(this.loadingWorker);
          }
        }
        final RecordQuadTree index = getIndex();

        final List<LayerRecord> records = (List)index.queryIntersects(boundingBox);
        return records;
      }
    }
    return Collections.emptyList();
  }

  @Override
  public List<LayerRecord> getRecordsCached(final Label cacheId) {
    synchronized (getSync()) {
      final List<LayerRecord> records = super.getRecordsCached(cacheId);
      final Set<Identifier> recordIds = this.recordIdentifiersByCacheId.get(cacheId);
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

  protected List<LayerRecord> getRecordsPersisted(final BoundingBox boundingBox) {
    final RecordDefinition recordDefinition = getInternalRecordDefinition();
    final Query query = Query.intersects(recordDefinition, boundingBox);
    return getRecords(query);
  }

  @Override
  public <R extends LayerRecord> List<R> getRecordsPersisted(final Query query) {
    final List<R> records = new ArrayList<>();
    if (isExists()) {
      final RecordStore recordStore = getRecordStore();
      if (recordStore != null) {
        try (
          final BooleanValueCloseable booleanValueCloseable = eventsDisabled();
          final RecordReader reader = newRecordStoreRecordReader(query);) {
          final Statistics statistics = query.getProperty("statistics");
          for (final LayerRecord record : reader.<LayerRecord> i()) {
            final Identifier identifier = getId(record);
            R proxyRecord = null;
            if (identifier == null) {
              proxyRecord = newProxyLayerRecord(record);
            } else {
              synchronized (getSync()) {
                final LayerRecord cachedRecord = getCachedRecord(identifier, record);
                if (!cachedRecord.isDeleted()) {
                  proxyRecord = newProxyLayerRecord(identifier);
                }
              }
            }
            if (proxyRecord != null) {
              records.add(proxyRecord);
              if (statistics != null) {
                statistics.add(record);
              }
            }
          }
        }
      }
    }
    return records;
  }

  @Override
  public RecordStore getRecordStore() {
    return this.recordStore;
  }

  @Override
  public PathName getTypePath() {
    return this.typePath;
  }

  @Override
  public boolean isLayerRecord(final Record record) {
    if (record instanceof LoadingRecord) {
      return false;
    } else if (record instanceof LayerRecord) {
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
          if (Maps.collectionContains(this.recordIdentifiersByCacheId, cacheId, identifier)) {
            return true;
          }
        }
        return super.isRecordCached(cacheId, record);
      }
    }
    return false;
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
      final LayerRecord newRecord = ArrayLayerRecord.newRecordNew(this, values);
      addRecordToCache(getCacheIdNew(), newRecord);
      if (isEventsEnabled()) {
        cleanCachedRecords();
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
    if (recordDefinition.equals(getRecordDefinition())) {
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

  @Override
  @SuppressWarnings("unchecked")
  protected <R extends LayerRecord> R newProxyLayerRecord(LayerRecord record) {
    if (record instanceof AbstractProxyLayerRecord) {
      // Already a proxy
    } else if (RecordState.NEW.equals(record.getState())) {
      record = new NewProxyLayerRecord(this, record);
    } else {
      final Identifier identifier = record.getIdentifier();
      record = newProxyLayerRecord(identifier);
    }
    return (R)record;
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
      removeRecordFromCache(this.getCacheIdDeleted(), record);
    }
    return deleted;
  }

  protected void preDeleteRecord(final LayerRecord record) {
  }

  private void removeFromRecordIdToRecordMap(final Identifier identifier) {
    synchronized (this.recordsByIdentifier) {
      this.recordsByIdentifier.remove(identifier);
    }
  }

  @Override
  protected boolean removeRecordFromCache(final Label cacheId, final LayerRecord record) {
    boolean removed = false;
    if (isLayerRecord(record)) {
      final Identifier identifier = record.getIdentifier();
      if (identifier != null) {
        synchronized (getSync()) {
          removed = Maps.removeFromSet(this.recordIdentifiersByCacheId, cacheId, identifier);
        }
      }
    }
    removed |= super.removeRecordFromCache(cacheId, record);
    return removed;
  }

  @Override
  protected boolean removeRecordFromCache(final LayerRecord record) {
    boolean removed = false;
    if (isLayerRecord(record)) {
      synchronized (getSync()) {
        final Identifier identifier = record.getIdentifier();
        if (identifier != null) {
          for (final Iterator<Set<Identifier>> iterator = this.recordIdentifiersByCacheId.values()
            .iterator(); iterator.hasNext();) {
            final Set<Identifier> identifiers = iterator.next();
            identifiers.remove(identifier);
            if (identifiers.isEmpty()) {
              iterator.remove();
            }
          }
        }
        removed |= super.removeRecordFromCache(record);
      }
    }
    return removed;
  }

  @Override
  public void revertChanges(final LayerRecord record) {
    removeRecordFromCache(this.getCacheIdDeleted(), record);
    super.revertChanges(record);
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

  @Override
  public void setProperty(final String name, final Object value) {
    if ("typePath".equals(name)) {
      super.setProperty(name, PathName.newPathName(value));
    } else {
      super.setProperty(name, value);
    }
  }

  public void setRecordsToCache(final Label cacheId,
    final Collection<? extends LayerRecord> records) {
    synchronized (getSync()) {
      this.recordIdentifiersByCacheId.put(cacheId, new HashSet<>());
      addRecordsToCache(cacheId, records);
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
      if (isExists()) {
        final RecordStore recordStore = getRecordStore();
        if (recordStore != null) {
          final RecordDefinition recordDefinition = recordStore.getRecordDefinition(this.typePath);
          if (recordDefinition != null) {

            setRecordDefinition(recordDefinition);
            return;
          }
        }
      }
    }
    setRecordDefinition(null);
  }

  @Override
  public void showForm(LayerRecord record, final String fieldName) {
    synchronized (getSync()) {
      final Identifier identifier = getId(record);
      if (identifier != null) {
        record = addRecordToCache(getCacheIdForm(), record);
      }
      super.showForm(record, fieldName);
    }
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    MapSerializerUtil.add(map, "typePath", this.typePath);
    return map;
  }

  protected void writeDelete(final Writer<Record> writer, final LayerRecord record) {
    writer.write(record);
  }

  protected void writeUpdate(final Writer<Record> writer, final LayerRecord record) {
    writer.write(record);
  }

}
