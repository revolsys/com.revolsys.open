package com.revolsys.swing.map.layer.dataobject;

import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreFactoryRegistry;
import com.revolsys.io.connection.ConnectionRegistry;
import com.revolsys.io.datastore.DataObjectStoreConnection;
import com.revolsys.io.datastore.DataObjectStoreConnectionManager;
import com.revolsys.io.map.AbstractMapObjectFactory;
import com.revolsys.swing.tree.datastore.AddDataStoreConnectionPanel;
import com.revolsys.util.ExceptionUtil;

public class DataObjectStoreLayerFactory extends AbstractMapObjectFactory {

  public DataObjectStoreLayerFactory() {
    super("dataStore", "Data Store");
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V toObject(final Map<String, ? extends Object> properties) {
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
      } else {
        dataStore = DataObjectStoreFactoryRegistry.createDataObjectStore(connectionProperties);
      }
      boolean exists = true;
      final String layerName = (String)properties.get("name");
      if (dataStore == null) {
        LoggerFactory.getLogger(getClass()).error(
          "Unable to create data store for layer: " + layerName);
        return null;
      }
      try {
        dataStore.initialize();
      } catch (final Throwable e) {
        ExceptionUtil.log(getClass(),
          "Unable to iniaitlize data store for layer " + layerName, e);
        exists = false;
      }
      final DataObjectStoreLayer layer = new DataObjectStoreLayer(dataStore,
        exists);
      layer.setProperties(properties);

      if (exists && layer.getMetaData() == null) {
        LoggerFactory.getLogger(getClass()).error(
          "Cannot find table for layer: " + layer);
        layer.setExists(false);
      }

      return (V)layer;
    }
  }
}
