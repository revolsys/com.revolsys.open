package com.revolsys.gis.data.io;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.vividsolutions.jts.geom.Envelope;

public class DataObjectStoreCache {
  public static DataObjectStoreCache getCache(
    final DataObjectStore dataStore) {
    return new DataObjectStoreCache(dataStore);
  }

  private final Map<Envelope, List> cachedObejcts = Collections.synchronizedMap(new HashMap<Envelope, List>());

  private final DataObjectStore dataStore;

  private final Map<Envelope, DataStoreQueryTask> loadTasks = new LinkedHashMap<Envelope, DataStoreQueryTask>();

  private QName typeName;

  public DataObjectStoreCache(
    final DataObjectStore dataStore) {
    this.dataStore = dataStore;
  }

  private void addEnvelope(
    final Envelope envelope) {
    synchronized (loadTasks) {
      if (!loadTasks.containsKey(envelope)) {
        loadTasks.put(envelope, new DataStoreQueryTask(dataStore, typeName,
          envelope));
      }
    }
  }

  public List getObjects(
    final Envelope envelope) {
    final List objects = cachedObejcts.get(envelope);
    if (objects == null) {
      addEnvelope(envelope);
    }
    return objects;
  }

  public void removeObjects(
    final Envelope envelope) {
    synchronized (loadTasks) {
      final DataStoreQueryTask task = loadTasks.get(envelope);
      if (task != null) {
        task.cancel();
        loadTasks.remove(task);
      }
    }
    cachedObejcts.remove(envelope);
  }

}
