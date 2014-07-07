package com.revolsys.data.io;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PreDestroy;

import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.PathUtil;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.ExceptionUtil;

public class RecordStoreSchema extends AbstractObjectWithProperties {

  private Reference<AbstractRecordStore> recordStore;

  private Map<String, RecordDefinition> recordDefinitionCache = null;

  private String path;

  public RecordStoreSchema() {
  }

  public RecordStoreSchema(final AbstractRecordStore recordStore,
    final String path) {
    this.recordStore = new WeakReference<AbstractRecordStore>(recordStore);
    this.path = path;
  }

  public void addMetaData(final RecordDefinition recordDefinition) {
    addMetaData(recordDefinition.getPath(), recordDefinition);
  }

  protected void addMetaData(final String typePath,
    final RecordDefinition recordDefinition) {
    final Map<String, RecordDefinition> recordDefinitionCache = getRecordDefinitionCache();
    recordDefinitionCache.put(typePath.toUpperCase(), recordDefinition);
  }

  @Override
  @PreDestroy
  public synchronized void close() {
    if (recordDefinitionCache != null) {
      for (final RecordDefinition recordDefinition : recordDefinitionCache.values()) {
        recordDefinition.destroy();
      }
    }
    recordStore = null;
    recordDefinitionCache = null;
    path = null;
    super.close();
  }

  public synchronized RecordDefinition findMetaData(final String typePath) {
    final Map<String, RecordDefinition> recordDefinitionCache = getRecordDefinitionCache();
    final RecordDefinition recordDefinition = recordDefinitionCache.get(typePath);
    return recordDefinition;
  }

  @SuppressWarnings("unchecked")
  public <V extends RecordStore> V getRecordStore() {
    if (recordStore == null) {
      return null;
    } else {
      return (V)recordStore.get();
    }
  }

  public GeometryFactory getGeometryFactory() {
    final GeometryFactory geometryFactory = getProperty("geometryFactory");
    if (geometryFactory == null) {
      final AbstractRecordStore recordStore = getRecordStore();
      if (recordStore == null) {
        return GeometryFactory.floating3();
      } else {
        return recordStore.getGeometryFactory();
      }
    } else {
      return geometryFactory;
    }
  }

  public synchronized RecordDefinition getRecordDefinition(String typePath) {
    typePath = typePath.toUpperCase();
    if (typePath.startsWith(path + "/") || path.equals("/")) {
      final Map<String, RecordDefinition> recordDefinitionCache = getRecordDefinitionCache();
      final RecordDefinition recordDefinition = recordDefinitionCache.get(typePath.toUpperCase());
      return recordDefinition;
    } else {
      return null;
    }
  }

  protected synchronized Map<String, RecordDefinition> getRecordDefinitionCache() {
    if (recordDefinitionCache == null) {
      refreshMetaData();
    }
    return recordDefinitionCache;
  }

  public String getName() {
    final String path = getPath();
    return PathUtil.getName(path);
  }

  public String getPath() {
    return path;
  }

  public List<String> getTypeNames() {
    final Map<String, RecordDefinition> recordDefinitionCache = getRecordDefinitionCache();
    return new ArrayList<String>(recordDefinitionCache.keySet());
  }

  public List<RecordDefinition> getTypes() {
    final Map<String, RecordDefinition> recordDefinitionCache = getRecordDefinitionCache();
    return new ArrayList<RecordDefinition>(recordDefinitionCache.values());
  }

  public boolean isInitialized() {
    return recordDefinitionCache != null;
  }

  public synchronized void refreshMetaData() {
    recordDefinitionCache = new TreeMap<>();
    final AbstractRecordStore recordStore = getRecordStore();
    if (recordStore != null) {
      final Collection<RecordStoreExtension> extensions = recordStore.getRecordStoreExtensions();
      for (final RecordStoreExtension extension : extensions) {
        try {
          if (extension.isEnabled(recordStore)) {
            extension.preProcess(this);
          }
        } catch (final Throwable e) {
          ExceptionUtil.log(extension.getClass(),
            "Unable to pre-process schema " + this, e);
        }
      }
      recordStore.loadSchemaRecordDefinitions(this, recordDefinitionCache);
      for (final RecordStoreExtension extension : extensions) {
        try {
          if (extension.isEnabled(recordStore)) {
            extension.postProcess(this);
          }
        } catch (final Throwable e) {
          ExceptionUtil.log(extension.getClass(),
            "Unable to post-process schema " + this, e);
        }
      }
    }
  }

  @Override
  public String toString() {
    return path;
  }
}
