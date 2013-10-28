package com.revolsys.swing.map.layer;

import java.util.LinkedList;

import javax.swing.SwingWorker;

import com.revolsys.io.datastore.DataObjectStoreConnectionRegistry;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.ExceptionUtil;

public class LayerInitializer extends SwingWorker<Void, Void> {

  private static final int MAX_WORKERS = 5;

  private static final LinkedList<Layer> LAYERS_TO_INITIALIZE = new LinkedList<Layer>();

  private static final LinkedList<Layer> LAYERS_CURRENTLY_INITIALIZING = new LinkedList<Layer>();

  private static int instanceCount;

  public static void initialize(final Layer layer) {
    synchronized (LAYERS_TO_INITIALIZE) {
      if (!LAYERS_CURRENTLY_INITIALIZING.contains(layer)
        && !LAYERS_TO_INITIALIZE.contains(layer) && !layer.isInitialized()) {
        LAYERS_TO_INITIALIZE.add(layer);
        if (instanceCount < MAX_WORKERS) {
          instanceCount++;
          final LayerInitializer initializer = new LayerInitializer();
          Invoke.worker(initializer);
        }
      }
    }
  }

  private final DataObjectStoreConnectionRegistry dataStoreRegistry;

  public LayerInitializer() {
    dataStoreRegistry = DataObjectStoreConnectionRegistry.getForThread();
  }

  @Override
  protected Void doInBackground() throws Exception {
    try {
      DataObjectStoreConnectionRegistry.setForThread(dataStoreRegistry);
      while (true) {
        Layer layer;
        synchronized (LAYERS_TO_INITIALIZE) {
          if (LAYERS_TO_INITIALIZE.isEmpty()) {
            instanceCount--;
            return null;
          } else {
            layer = LAYERS_TO_INITIALIZE.removeFirst();
            LAYERS_CURRENTLY_INITIALIZING.add(layer);
          }
        }
        try {
          layer.initialize();
        } catch (final Throwable e) {
          ExceptionUtil.log(layer.getClass(), "Unable to iniaitlize layer: "
            + layer.getName(), e);
        } finally {
          LAYERS_CURRENTLY_INITIALIZING.remove(layer);
        }
      }
    } finally {
      DataObjectStoreConnectionRegistry.setForThread(null);
    }
  }

  @Override
  public String toString() {
    return "Initializing layers";
  }
}
