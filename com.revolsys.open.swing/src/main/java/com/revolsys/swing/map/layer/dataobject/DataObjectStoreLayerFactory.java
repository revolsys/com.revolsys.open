package com.revolsys.swing.map.layer.dataobject;

import java.lang.reflect.Proxy;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreConnections;
import com.revolsys.gis.data.io.DataObjectStoreFactoryRegistry;
import com.revolsys.gis.data.io.DelegatingDataObjectStoreHandler;
import com.revolsys.swing.map.layer.AbstractLayerFactory;

public class DataObjectStoreLayerFactory extends
  AbstractLayerFactory<DataObjectStoreLayer> {

  public DataObjectStoreLayerFactory() {
    super("dataStore", "Data Store");
  }

  @Override
  public DataObjectStoreLayer createLayer(final Map<String, Object> properties) {
    @SuppressWarnings("unchecked")
    final Map<String, String> connectionProperties = (Map<String, String>)properties.get("connectionProperties");
    if (connectionProperties == null) {
      throw new IllegalArgumentException(
        "A data store layer requires a connectionProperties entry with a name or url, username, and password.");
    } else {
      final String name = connectionProperties.get("name");
      DataObjectStore dataStore;
      if (StringUtils.hasText(name)) {
        final DataObjectStoreConnections connections = DataObjectStoreConnections.get();
        dataStore = connections.getDataObjectStore(name);
        if (dataStore == null) {
          connections.createConnection(name, connectionProperties);
          dataStore = connections.getDataObjectStore(name);
        }
        if (dataStore instanceof Proxy) {
          final DelegatingDataObjectStoreHandler handler = (DelegatingDataObjectStoreHandler)Proxy.getInvocationHandler(dataStore);
          dataStore = handler.getDataStore();
        }
      } else {
        dataStore = DataObjectStoreFactoryRegistry.createDataObjectStore(connectionProperties);
      }
      dataStore.initialize();
      final DataObjectStoreLayer layer = new DataObjectStoreLayer(dataStore);
      layer.setProperties(properties);
      return layer;
    }
  }

}
