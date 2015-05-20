package com.revolsys.data.record.schema;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PreDestroy;

import com.revolsys.collection.EmptyReference;
import com.revolsys.data.io.RecordStoreExtension;
import com.revolsys.io.Path;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.Property;

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
    recordStore = new EmptyReference<>();
  }

  public void addElement(final RecordStoreSchemaElement element) {
    refreshIfNeeded();
    final String upperPath = element.getPath().toUpperCase();
    if (Path.isParent(getPath(), upperPath)) {
      elementsByPath.put(upperPath, element);
      if (element instanceof RecordDefinition) {
        final RecordDefinition recordDefinition = (RecordDefinition)element;
        recordDefinitionsByPath.put(upperPath, recordDefinition);
        final AbstractRecordStore recordStore = getRecordStore();
        recordStore.addRecordDefinitionProperties((RecordDefinitionImpl)recordDefinition);
      }
      if (element instanceof RecordStoreSchema) {
        final RecordStoreSchema schema = (RecordStoreSchema)element;
        schemasByPath.put(upperPath, schema);
      }
    } else {
      throw new IllegalArgumentException(getPath() + " is not a parent of "
        + upperPath);
    }
  }

  @Override
  @PreDestroy
  public synchronized void close() {
    if (recordDefinitionsByPath != null) {
      for (final RecordDefinition recordDefinition : recordDefinitionsByPath.values()) {
        recordDefinition.destroy();
      }
    }
    recordStore = new WeakReference<AbstractRecordStore>(null);
    recordDefinitionsByPath.clear();
    elementsByPath.clear();
    schemasByPath.clear();
    super.close();
  }

  public RecordStoreSchema createSchema(final String path) {
    final RecordStoreSchemaElement element = getElement(path);
    if (element == null) {
      final RecordStoreSchema schema = new RecordStoreSchema(this, path);
      addElement(schema);
      return schema;
    } else if (element instanceof RecordStoreSchema) {
      final RecordStoreSchema schema = (RecordStoreSchema)element;
      return schema;
    } else {
      throw new IllegalArgumentException("Non schema element with path " + path
        + " already exists");
    }
  }

  public synchronized RecordDefinition findRecordDefinition(final String path) {
    refreshIfNeeded();
    final RecordDefinition recordDefinition = recordDefinitionsByPath.get(path);
    return recordDefinition;
  }

  @SuppressWarnings("unchecked")
  public <V extends RecordStoreSchemaElement> V getElement(String path) {
    if (path == null) {
      return null;
    } else {
      refreshIfNeeded();
      RecordStoreSchemaElement childElement = elementsByPath.get(path);
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
              childElement = elementsByPath.get(childElementPath);
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
    refreshIfNeeded();
    return new ArrayList<>(recordDefinitionsByPath.values());
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends RecordStore> V getRecordStore() {
    final RecordStoreSchema schema = getSchema();
    if (schema == null) {
      return (V)recordStore.get();
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

  public List<String> getSchemaPaths() {
    refreshIfNeeded();
    return new ArrayList<>(schemasByPath.keySet());
  }

  public List<RecordStoreSchema> getSchemas() {
    refreshIfNeeded();
    return new ArrayList<>(schemasByPath.values());
  }

  public List<String> getTypeNames() {
    refreshIfNeeded();
    return new ArrayList<>(recordDefinitionsByPath.keySet());
  }

  private boolean isEqual(final RecordStoreSchemaElement oldElement,
    final RecordStoreSchemaElement newElement) {
    if (oldElement == newElement) {
      return true;
    } else if (oldElement instanceof RecordStoreSchema) {
      if (newElement instanceof RecordStoreSchema) {
        return true;
      }
    } else if (oldElement instanceof RecordDefinition) {
      final RecordDefinition oldRecordDefinition = (RecordDefinition)oldElement;
      if (newElement instanceof RecordDefinition) {
        final RecordDefinition newRecordDefinition = (RecordDefinition)newElement;
        if (Property.equals(newRecordDefinition, oldRecordDefinition,
          "idFieldNames")) {
          if (Property.equals(newRecordDefinition, oldRecordDefinition,
            "idFieldIndexes")) {
            if (Property.equals(newRecordDefinition, oldRecordDefinition,
              "geometryFieldNames")) {
              if (Property.equals(newRecordDefinition, oldRecordDefinition,
                "geometryFieldIndexes")) {
                if (Property.equals(newRecordDefinition, oldRecordDefinition,
                  "fields")) {
                  return true;
                }
              }
            }
          }
        }
      }
    }
    return false;
  }

  public boolean isInitialized() {
    return initialized;
  }

  public synchronized void refresh() {
    initialized = true;
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

      final Map<String, ? extends RecordStoreSchemaElement> elementsByPath = recordStore.refreshSchemaElements(this);

      final Set<String> removedPaths = new HashSet<>(
        this.elementsByPath.keySet());
      for (final Entry<String, ? extends RecordStoreSchemaElement> entry : elementsByPath.entrySet()) {
        final String path = entry.getKey();
        final String upperPath = path.toUpperCase();
        removedPaths.remove(upperPath);
        final RecordStoreSchemaElement newElement = entry.getValue();
        final RecordStoreSchemaElement oldElement = this.elementsByPath.get(path);
        if (oldElement == null) {
          addElement(newElement);
        } else {
          replaceElement(upperPath, oldElement, newElement);
        }
      }
      for (final String upperPath : removedPaths) {
        removeElement(upperPath);
      }

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
    if (!initialized) {
      refresh();
    }
  }

  private void removeElement(final String upperPath) {
    elementsByPath.remove(upperPath);
    recordDefinitionsByPath.remove(upperPath);
    schemasByPath.remove(upperPath);
  }

  private void replaceElement(final String upperPath,
    final RecordStoreSchemaElement oldElement,
    final RecordStoreSchemaElement newElement) {
    if (!isEqual(oldElement, newElement)) {
      removeElement(upperPath.toUpperCase());
      addElement(newElement);
    }
  }
}
