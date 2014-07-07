package com.revolsys.data.io;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.jts.geom.BoundingBox;

public class RecordStoreCache {
  public static RecordStoreCache getCache(final RecordStore recordStore) {
    return new RecordStoreCache(recordStore);
  }

  private final Map<BoundingBox, List> cachedObejcts = Collections.synchronizedMap(new HashMap<BoundingBox, List>());

  private final RecordStore recordStore;

  private final Map<BoundingBox, DataStoreQueryTask> loadTasks = new LinkedHashMap<BoundingBox, DataStoreQueryTask>();

  private String typePath;

  public RecordStoreCache(final RecordStore recordStore) {
    this.recordStore = recordStore;
  }

  private void addBoundingBox(final BoundingBox boundingBox) {
    synchronized (loadTasks) {
      if (!loadTasks.containsKey(boundingBox)) {
        loadTasks.put(boundingBox, new DataStoreQueryTask(recordStore, typePath,
          boundingBox));
      }
    }
  }

  public List getObjects(final BoundingBox boundingBox) {
    final List objects = cachedObejcts.get(boundingBox);
    if (objects == null) {
      addBoundingBox(boundingBox);
    }
    return objects;
  }

  public void removeObjects(final BoundingBox boundingBox) {
    synchronized (loadTasks) {
      final DataStoreQueryTask task = loadTasks.get(boundingBox);
      if (task != null) {
        task.cancel();
        loadTasks.remove(task);
      }
    }
    cachedObejcts.remove(boundingBox);
  }

}
