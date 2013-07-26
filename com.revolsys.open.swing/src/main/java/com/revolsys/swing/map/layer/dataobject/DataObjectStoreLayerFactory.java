package com.revolsys.swing.map.layer.dataobject;

import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreFactoryRegistry;
import com.revolsys.io.connection.ConnectionRegistry;
import com.revolsys.io.datastore.DataObjectStoreConnection;
import com.revolsys.io.datastore.DataObjectStoreConnectionManager;
import com.revolsys.swing.map.layer.AbstractLayerFactory;
import com.revolsys.swing.tree.datastore.AddDataStoreConnectionPanel;

public class DataObjectStoreLayerFactory extends
  AbstractLayerFactory<DataObjectStoreLayer> {

  public DataObjectStoreLayerFactory() {
    super("dataStore", "Data Store");
  }

  @Override
  public DataObjectStoreLayer createLayer(final Map<String, Object> properties) {
    @SuppressWarnings("unchecked")
    final Map<String, String> connectionProperties = (Map<String, String>)properties.get("connection");
    if (connectionProperties == null) {
      throw new IllegalArgumentException(
        "A data store layer requires a connectionProperties entry with a name or url, username, and password.");
    } else {
      final String name = connectionProperties.get("name");
      DataObjectStore dataStore;
      if (StringUtils.hasText(name)) {
        dataStore = DataObjectStoreConnectionManager.getConnection(name);
        if (dataStore == null) {
          final DataObjectStoreConnectionManager connectionManager = DataObjectStoreConnectionManager.get();
          final ConnectionRegistry<DataObjectStoreConnection> registry = connectionManager.getConnectionRegistry("User");
          dataStore = new AddDataStoreConnectionPanel(registry, name).showDialog();
        }
        // TODO if null add
      } else {
        dataStore = DataObjectStoreFactoryRegistry.createDataObjectStore(connectionProperties);
      }
      if (dataStore == null) {
        LoggerFactory.getLogger(getClass()).error(
          "Unable to create data store for layer: " + properties.get("name"));
        return null;
      } else {
        dataStore.initialize();
        final DataObjectStoreLayer layer = new DataObjectStoreLayer(dataStore);
        layer.setProperties(properties);
        if (layer.getMetaData() == null) {
          return null;
        } else {
          return layer;
        }
      }
    }
  }
}
