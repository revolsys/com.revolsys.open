package com.revolsys.swing.map.layer.record;

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
import java.util.WeakHashMap;

import javax.swing.JComponent;
import javax.swing.SwingWorker;

import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import com.revolsys.filter.Filter;
import com.revolsys.gis.algorithm.index.DataObjectQuadTree;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.model.RecordIdentifier;
import com.revolsys.gis.data.model.SingleRecordIdentifier;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.model.filter.DataObjectGeometryBoundingBoxIntersectsFilter;
import com.revolsys.gis.data.query.Condition;
import com.revolsys.gis.data.query.Q;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.data.query.functions.F;
import com.revolsys.gis.data.query.functions.WithinDistance;
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

public class DataObjectStoreLayer extends AbstractDataObjectLayer {

  public static AbstractDataObjectLayer create(
    final Map<String, Object> properties) {
    return new DataObjectStoreLayer(properties);
  }

  public static final MapObjectFactory FACTORY = new InvokeMethodMapObjectFactory(
    "dataStore", "Data Store", DataObjectStoreLayer.class, "create");

  private BoundingBox boundingBox = new BoundingBoxDoubleGf();

  private final Map<RecordIdentifier, LayerRecord> recordIdToRecordMap = new WeakHashMap<>();

  private DataObjectStore dataStore;

  private BoundingBox loadingBoundingBox = new BoundingBoxDoubleGf();

  private SwingWorker<DataObjectQuadTree, Void> loadingWorker;

  private final Object sync = new Object();

  private String typePath;

  private final Set<RecordIdentifier> deletedRecordIds = new LinkedHashSet<>();

  private final Set<RecordIdentifier> formRecordIds = new LinkedHashSet<>();

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
  protected <V extends LayerRecord> boolean addCachedRecord(
    final List<V> records, final LayerRecord record) {
    if (record == null) {
      return false;
    } else if (record.getState() == DataObjectState.Deleted) {
      return false;
    } else {
      final RecordIdentifier identifier = getId(record);
      if (identifier == null) {
        records.add((V)record);
      } else if (record instanceof ProxyLayerRecord) {
        records.add((V)record);
      } else {
        synchronized (this.recordIdToRecordMap) {
          LayerRecord cachedRecord = this.recordIdToRecordMap.get(identifier);
          if (cachedRecord == null) {
            this.recordIdToRecordMap.put(identifier, record);
            cachedRecord = record;
          } else {
            if (cachedRecord.getState() == DataObjectState.Deleted) {
              return false;
            }
          }
          records.add((V)new ProxyLayerRecord(this, identifier));
        }
      }
      return true;
    }
  }

  protected void addIds(final Set<RecordIdentifier> ids,
    final Collection<? extends DataObject> records) {
    for (final DataObject record : records) {
      final RecordIdentifier id = getId((LayerRecord)record);
      if (id != null) {
        ids.add(id);
      }
    }
  }

  @Override
  protected void addModifiedRecord(final LayerRecord record) {
    final LayerRecord cacheObject = getCacheRecord(record);
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
    SwingUtil.addReadOnlyTextField(panel, "Type Path", this.typePath);

    GroupLayoutUtil.makeColumns(panel, 2, true);
    return panel;
  }

  @Override
  protected void addSelectedRecord(final LayerRecord record) {
    final DataObject cachedObject = getCacheRecord(record);
    if (cachedObject != null) {
      super.addSelectedRecord(record);
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

  protected void cacheRecords(final Collection<? extends DataObject> records) {
    for (final DataObject record : records) {
      if (record instanceof LayerRecord) {
        final LayerRecord layerRecord = (LayerRecord)record;
        getCacheRecord(layerRecord);
      }
    }
  }

  /**
   * Remove any cached records that are currently not used.
   */
  protected void cleanCachedRecords() {
    synchronized (this.recordIdToRecordMap) {
      final Set<RecordIdentifier> ids = getIdsToCache();
      // final Map<RecordIdentifier, LayerRecord> cachedRecords = new
      // HashMap<>();
      // for (final RecordIdentifier id : ids) {
      // final LayerRecord record = this.cachedRecords.get(id);
      // if (record != null) {
      // cachedRecords.put(id, record);
      // }
      // }
      // this.cachedRecords = cachedRecords;
    }
  }

  protected void clearLoading(final BoundingBox loadedBoundingBox) {
    synchronized (this.sync) {
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
    synchronized (this.recordIdToRecordMap) {
      super.clearSelectedRecords();
      cleanCachedRecords();
    }
  }

  protected LoadingWorker createLoadingWorker(final BoundingBox boundingBox) {
    return new LoadingWorker(this, boundingBox);
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
    final SwingWorker<DataObjectQuadTree, Void> loadingWorker = this.loadingWorker;
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
      record.setState(DataObjectState.Deleted);
      unSelectRecords(record);
      final RecordIdentifier id = getId(record);
      if (id != null) {
        final LayerRecord cacheRecord = removeCacheRecord(id, record);
        this.deletedRecordIds.add(id);
        super.deleteRecord(cacheRecord, true);
        removeFromIndex(record);
        removeFromIndex(cacheRecord);
      } else {
        removeFromIndex(record);
        super.deleteRecord(record, trackDeletions);
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
      final DataObjectMetaData metaData = getMetaData();
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
              final boolean added = addCachedRecord(records, record);
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
      synchronized (this.sync) {
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
              final List<String> idAttributeNames = getMetaData().getIdAttributeNames();
              final RecordIdentifier idString = record.getIdentifier();
              if (this.deletedRecordIds.contains(idString)
                || super.isDeleted(record)) {
                preDeleteRecord(record);
                record.setState(DataObjectState.Deleted);
                writer.write(record);
              } else if (super.isModified(record)) {
                writer.write(record);
              } else if (isNew(record)) {
                RecordIdentifier id = record.getIdentifier();
                if (id == null && !idAttributeNames.isEmpty()) {
                  id = SingleRecordIdentifier.create(dataStore.createPrimaryIdValue(this.typePath));
                  id.setIdentifier(record, idAttributeNames);
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
  protected <V extends LayerRecord> List<V> getCachedRecords() {
    synchronized (this.recordIdToRecordMap) {
      final List<V> cachedRecords = new ArrayList(
        this.recordIdToRecordMap.values());
      return cachedRecords;

    }
  }

  protected <V extends LayerRecord> List<V> getCachedRecords(
    final Collection<? extends V> records) {
    final List<V> cachedRecords = new ArrayList<V>();
    for (final V record : records) {
      addCachedRecord(cachedRecords, record);
    }
    return cachedRecords;
  }

  public LayerRecord getCacheRecord(final LayerRecord record) {
    if (record == null) {
      return null;
    } else if (record instanceof ProxyLayerRecord) {
      return record;
    } else {
      final RecordIdentifier id = getId(record);
      return getCacheRecord(id, record);
    }
  }

  private LayerRecord getCacheRecord(final RecordIdentifier id,
    final LayerRecord record) {
    if (id != null && record != null && isLayerRecord(record)) {
      if (record instanceof ProxyLayerRecord) {
        return record;
      } else if (record.getState() == DataObjectState.New) {
        return record;
      } else if (record.getState() == DataObjectState.Deleted) {
        return record;
      } else {
        synchronized (this.recordIdToRecordMap) {
          if (!this.recordIdToRecordMap.containsKey(id)) {
            this.recordIdToRecordMap.put(id, record);
          }
          return new ProxyLayerRecord(this, id);
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
    final DataObjectMetaData metaData = getMetaData();
    if (metaData == null) {
      return null;
    } else {
      return metaData.getGeometryAttribute();
    }
  }

  protected RecordIdentifier getId(final LayerRecord record) {
    if (isLayerRecord(record)) {
      return record.getIdentifier();
    } else {
      return null;
    }
  }

  protected Set<RecordIdentifier> getIdsToCache() {
    final Set<RecordIdentifier> ids = new HashSet<>();
    ids.addAll(this.deletedRecordIds);
    ids.addAll(this.formRecordIds);
    addIds(ids, getSelectedRecords());
    addIds(ids, getHighlightedRecords());
    addIds(ids, getModifiedRecords());
    addIds(ids, getIndex().getAll());
    return ids;
  }

  public BoundingBox getLoadingBoundingBox() {
    return this.loadingBoundingBox;
  }

  @Override
  public LayerRecord getRecordById(final RecordIdentifier id) {
    final DataObjectMetaData metaData = getMetaData();
    final List<String> idAttributeNames = metaData.getIdAttributeNames();
    if (idAttributeNames.isEmpty()) {
      LoggerFactory.getLogger(getClass()).error(
        this.typePath + " does not have a primary key");
      return null;
    } else {
      final LayerRecord record = this.recordIdToRecordMap.get(id);
      if (record == null) {
        final Condition where = Q.equal(id, idAttributeNames);
        final Query query = new Query(metaData, where);
        query.setProperty("dataObjectFactory", this);
        final DataObjectStore dataStore = getDataStore();
        return (LayerRecord)dataStore.queryFirst(query);
      } else {
        return record;
      }
    }

  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  protected List<LayerRecord> getRecords(final BoundingBox boundingBox) {
    final BoundingBox convertedBoundingBox = boundingBox.convert(getGeometryFactory());
    BoundingBox loadedBoundingBox;
    DataObjectQuadTree index;
    synchronized (this.sync) {
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
          if (!record.getState().equals(DataObjectState.Deleted)) {
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
        final DataObjectMetaData metaData = getMetaData();
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

  public String getTypePath() {
    return this.typePath;
  }

  @Override
  protected LayerRecord internalCancelChanges(final LayerRecord record) {
    if (record.getState() == DataObjectState.Deleted) {
      final RecordIdentifier id = getId(record);
      if (id != null) {
        this.deletedRecordIds.remove(id);
      }
    }
    return super.internalCancelChanges(record);
  }

  @Override
  public boolean isLayerRecord(final DataObject record) {
    if (record instanceof LayerRecord) {
      final LayerRecord layerDataObject = (LayerRecord)record;
      if (layerDataObject.getLayer() == this) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void postSaveChanges(final DataObjectState originalState,
    final LayerRecord record) {
    super.postSaveChanges(originalState, record);
    if (originalState == DataObjectState.New) {
      getCacheRecord(record);
    }
  }

  @Override
  protected boolean postSaveDeletedRecord(final LayerRecord record) {
    final boolean deleted = super.postSaveDeletedRecord(record);
    if (deleted) {
      final RecordIdentifier id = record.getIdentifier();
      this.deletedRecordIds.remove(id);
    }
    return deleted;
  }

  protected void preDeleteRecord(final LayerRecord record) {
  }

  public List<LayerRecord> query(final Map<String, ? extends Object> filter) {
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

  protected LayerRecord removeCacheRecord(final RecordIdentifier id,
    final LayerRecord record) {
    if (id != null && record != null && isLayerRecord(record)) {
      if (record.getState() == DataObjectState.New) {
        return record;
      } else if (record.getState() == DataObjectState.Deleted) {
        return record;
      } else {
        synchronized (this.recordIdToRecordMap) {
          final LayerRecord cachedRecord = this.recordIdToRecordMap.get(id);
          return cachedRecord;
        }
      }
    }
    return record;
  }

  @Override
  protected void removeForm(final LayerRecord record) {
    synchronized (this.formRecordIds) {
      final RecordIdentifier id = getId(record);
      if (id != null) {
        this.formRecordIds.remove(id);
        cleanCachedRecords();
      }
      super.removeForm(record);
    }
  }

  @Override
  protected void removeSelectedRecord(final LayerRecord record) {
    final DataObject cachedObject = getCacheRecord(record);
    if (cachedObject != null) {
      super.removeSelectedRecord(record);
    }
  }

  @Override
  public void revertChanges(final LayerRecord record) {
    final RecordIdentifier id = getId(record);
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
          cacheRecords(index.getAll());
          final List<LayerRecord> newObjects = getNewRecords();
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
  public <V extends JComponent> V showForm(final LayerRecord record) {
    synchronized (this.formRecordIds) {
      final RecordIdentifier id = getId(record);
      if (id == null) {
        return super.showForm(record);
      } else {
        this.formRecordIds.add(id);
        final LayerRecord cachedObject = getCacheRecord(id, record);
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
    cleanCachedRecords();
  }

}
