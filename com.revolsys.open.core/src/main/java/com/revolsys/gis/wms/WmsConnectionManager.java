package com.revolsys.gis.wms;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.collection.map.Maps;

public class WmsConnectionManager {

  public static WmsConnectionManager get() {
    return INSTANCE;
  }

  private static final Logger LOG = LoggerFactory.getLogger(WmsConnectionManager.class);

  private static WmsConnectionManager INSTANCE = new WmsConnectionManager();

  private final Map<String, WmsClient> wmsConnections = new HashMap<String, WmsClient>();

  private final Preferences wmsConnectionsPrefereneces;

  public WmsConnectionManager() {
    this(Preferences.userRoot(), "com/revolsys/gis/wms/connections");
    final Preferences node = this.wmsConnectionsPrefereneces.node("BC Government Maps");
    node.put("connectionUrl", "http://openmaps.gov.bc.ca/mapserver/base2");
  }

  public WmsConnectionManager(final Preferences root,
    final String preferencesPath) {
    this.wmsConnectionsPrefereneces = root.node(preferencesPath);
  }

  public WmsConnectionManager(final String preferencesPath) {
    this(Preferences.userRoot(), preferencesPath);
  }

  public List<String> getConnectionNames() {
    try {
      final String[] names = this.wmsConnectionsPrefereneces.childrenNames();
      return Arrays.asList(names);
    } catch (final BackingStoreException e) {
      throw new RuntimeException(e);
    }
  }

  public List<WmsClient> getConnections() {
    final List<WmsClient> wmsConnections = new ArrayList<WmsClient>();
    final List<String> connectionNames = getConnectionNames();
    for (final String connectionName : connectionNames) {
      final WmsClient wmsConnection = getWmsConnection(connectionName);
      if (wmsConnection != null) {
        wmsConnections.add(wmsConnection);
      }
    }
    return wmsConnections;
  }

  private Preferences getPreferences(final String connectionName) {
    return this.wmsConnectionsPrefereneces.node(connectionName);
  }

  private WmsClient getWmsConnection(final String connectionName) {
    WmsClient wmsConnection = this.wmsConnections.get(connectionName);
    if (wmsConnection == null) {
      final Preferences preferences = getPreferences(connectionName);
      final Map<String, Object> config = Maps.toMap(preferences);
      final String connectionUrl = (String)config.get("connectionUrl");
      try {
        wmsConnection = new WmsClient(connectionName, connectionUrl);
      } catch (final MalformedURLException e) {
        LOG.error("Unable to get connection " + connectionUrl, e);
      }
      this.wmsConnections.put(connectionName, wmsConnection);
    }
    return wmsConnection;
  }

  @Override
  public String toString() {
    return "Web Map Service (WMS) Connections";
  }
}
