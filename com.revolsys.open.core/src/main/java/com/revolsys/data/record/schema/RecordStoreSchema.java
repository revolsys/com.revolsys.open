package com.revolsys.data.record.schema;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PreDestroy;

import com.revolsys.collection.EmptyReference;
import com.revolsys.data.io.RecordStoreExtension;
import com.revolsys.data.io.RecordStoreSchemaElement;
import com.revolsys.io.Path;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.ExceptionUtil;

public class RecordStoreSchema extends AbstractRecordStoreSchemaElement {

  private Reference<AbstractRecordStore> recordStore;

  private final Map<String, RecordStoreSchemaElement> elementsByPath = new TreeMap<>();

  private final Map<String, RecordDefinition> recordDefinitionsByPath = new TreeMap<>();

  private final Map<String, RecordStoreSchema> schemasByPath = new TreeMap<>();

  private boolean initialized = false;

  public RecordStoreSchema(final AbstractRecordStore recordStore) {
    super("/");
    this.recordStore = new WeakReference<>(recordStore);
  }

  protected RecordStoreSchema(final AbstractRecordStore recordStore,
    final String path) {
    super(path);
    this.recordStore = new WeakReference<>(recordStore);
  }

  public RecordStoreSchema(final RecordStoreSchema schema, final String path) {
    super(schema, path);
    this.recordStore = new EmptyReference<>();
  }

  protected void addMetaData(final String typePath,
    final RecordDefinition recordDefinition) {
    final Map<String, RecordDefinition> recordDefinitionCache = getRecordDefinitionsByPath();
    recordDefinitionCache.put(typePath.toUpperCase(), recordDefinition);
  }

  public void addRecordDefinition(final RecordDefinition recordDefinition) {
    addMetaData(recordDefinition.getPath(), recordDefinition);
  }

  protected void addSchemaElement(final RecordStoreSchemaElement element) {
    if (element != null) {
      final String path = element.getPath().toUpperCase();
      this.elementsByPath.put(path, element);
      if (element instanceof RecordDefinition) {
        final RecordDefinition recordDefinition = (RecordDefinition)element;
        this.recordDefinitionsByPath.put(path, recordDefinition);
      }
      if (element instanceof RecordStoreSchema) {
        final RecordStoreSchema schema = (RecordStoreSchema)element;
        this.schemasByPath.put(path, schema);
      }
    }
  }

  @Override
  @PreDestroy
  public synchronized void close() {
    if (this.recordDefinitionsByPath != null) {
      for (final RecordDefinition recordDefinition : this.recordDefinitionsByPath.values()) {
        recordDefinition.destroy();
      }
    }
    this.recordStore = new WeakReference<AbstractRecordStore>(null);
    this.recordDefinitionsByPath.clear();
    this.elementsByPath.clear();
    this.schemasByPath.clear();
    super.close();
  }

  public synchronized RecordDefinition findRecordDefinition(final String path) {
    final Map<String, RecordDefinition> recordDefinitionCache = getRecordDefinitionsByPath();
    final RecordDefinition recordDefinition = recordDefinitionCache.get(path);
    return recordDefinition;
  }

  @SuppressWarnings("unchecked")
  public <V extends RecordStoreSchemaElement> V getElement(String path) {
    if (path == null) {
      return null;
    } else {
      RecordStoreSchemaElement childElement = this.elementsByPath.get(path);
      if (childElement == null) {
        path = Path.cleanUpper(path);
        if (equalPath(path)) {
          return (V)this;
        } else {
          final String schemaPath = getPath();
          if (Path.isAncestor(schemaPath, path)) {
            synchronized (this) {
              refreshIfNeeded();
              final String childElementPath = Path.getChildPath(schemaPath,
                path);
              childElement = this.elementsByPath.get(childElementPath);
              if (childElement == null) {
                return null;
              } else if (childElement.equalPath(path)) {
                return (V)childElement;
              } else if (childElement instanceof RecordStoreSchema) {
                final RecordStoreSchema childSchema = (RecordStoreSchema)childElement;
                return childSchema.getElement(path);
              } else {
                return null;
              }
            }
          } else {
            final RecordStoreSchema parent = getSchema();
            if (parent == null) {
              return null;
            } else {
              return parent.getElement(path);
            }
          }
        }
      } else {
        return (V)childElement;
      }
    }
  }

  public List<RecordStoreSchemaElement> getElements() {
    refreshIfNeeded();
    final List<RecordStoreSchemaElement> elements = new ArrayList<>();
    elements.addAll(getSchemas());
    elements.addAll(getRecordDefinitions());
    return elements;
  }

  public GeometryFactory getGeometryFactory() {
    final GeometryFactory geometryFactory = getProperty("geometryFactory");
    if (geometryFactory == null) {
      final RecordStore recordStore = getRecordStore();
      if (recordStore == null) {
        return GeometryFactory.floating3();
      } else {
        return recordStore.getGeometryFactory();
      }
    } else {
      return geometryFactory;
    }
  }

  public synchronized RecordDefinition getRecordDefinition(final String path) {
    final RecordStoreSchemaElement element = getElement(path);
    if (element instanceof RecordDefinition) {
      return (RecordDefinition)element;
    } else {
      return null;
    }
  }

  public List<RecordDefinition> getRecordDefinitions() {
    final Map<String, RecordDefinition> recordDefinitionCache = getRecordDefinitionsByPath();
    return new ArrayList<RecordDefinition>(recordDefinitionCache.values());
  }

  protected synchronized Map<String, RecordDefinition> getRecordDefinitionsByPath() {
    refreshIfNeeded();
    return this.recordDefinitionsByPath;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends RecordStore> V getRecordStore() {
    final RecordStoreSchema schema = getSchema();
    if (schema == null) {
      return (V)this.recordStore.get();
    } else {
      return schema.getRecordStore();
    }
  }

  public RecordStoreSchema getSchema(final String path) {
    final RecordStoreSchemaElement element = getElement(path);
    if (element instanceof RecordStoreSchema) {
      return (RecordStoreSchema)element;
    } else {
      return null;
    }
  }

  public List<RecordStoreSchema> getSchemas() {
    refreshIfNeeded();
    return new ArrayList<>(this.schemasByPath.values());
  }

  public List<String> getTypeNames() {
    final Map<String, RecordDefinition> recordDefinitionCache = getRecordDefinitionsByPath();
    return new ArrayList<String>(recordDefinitionCache.keySet());
  }

  public boolean isInitialized() {
    return this.recordDefinitionsByPath != null;
  }

  public synchronized void refresh() {
    this.initialized = true;
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
      recordStore.refreshSchema(this);
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

  protected void refreshIfNeeded() {
    if (!this.initialized) {
      refresh();
    }
  }
}
