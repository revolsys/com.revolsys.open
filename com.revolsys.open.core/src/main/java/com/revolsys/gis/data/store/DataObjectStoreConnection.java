package com.revolsys.gis.data.store;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreFactoryRegistry;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.util.CollectionUtil;

public class DataObjectStoreConnection implements MapSerializer {
  private Map<String, Object> config;

  private String name;

  private String resourceName;

  private DataObjectStore dataStore;

  public DataObjectStoreConnection(final String name,
    final DataObjectStore dataStore) {
    this.name = name;
    this.dataStore = dataStore;
  }

  public DataObjectStoreConnection(final String resourceName,
    final Map<String, ? extends Object> config) {
    super();
    this.resourceName = resourceName;
    this.config = new LinkedHashMap<String, Object>(config);
    name = CollectionUtil.getString(config, "name");
    if (!StringUtils.hasText(name)) {
      name = FileUtil.getBaseName(resourceName);
    }
  }

  public DataObjectStore getDataStore() {
    synchronized (this) {
      if (dataStore == null) {
        try {
          final Map<String, Object> connectionProperties = CollectionUtil.get(
            config, "connection", Collections.<String, Object> emptyMap());
          if (connectionProperties.isEmpty()) {
            LoggerFactory.getLogger(getClass()).error(
              "Data store must include a 'connection' map property: "
                + resourceName);
          } else {
            dataStore = DataObjectStoreFactoryRegistry.createDataObjectStore(connectionProperties);
            dataStore.initialize();
          }
        } catch (final Throwable e) {
          LoggerFactory.getLogger(getClass()).error(
            "Error creating data store from: " + resourceName, e);
        }
      }
    }
    return dataStore;
  }

  public String getName() {
    return name;
  }

  public List<DataObjectStoreSchema> getSchemas() {
    final DataObjectStore dataStore = getDataStore();
    if (dataStore == null) {
      return Collections.emptyList();
    } else {
      return dataStore.getSchemas();
    }
  }

  @Override
  public Map<String, Object> toMap() {
    return config;
  }

  @Override
  public String toString() {
    return name;
  }
}
