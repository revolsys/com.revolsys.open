package com.revolsys.swing.map.layer;

import java.util.LinkedList;

import javax.swing.SwingWorker;

import com.revolsys.io.datastore.DataObjectStoreConnectionRegistry;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.ExceptionUtil;

public class LayerInitializer extends SwingWorker<Void, Void> {

  private static final LinkedList<Layer> INITIALIZING_LAYERS = new LinkedList<Layer>();

  private static int instanceCount;

  public static void initialize(final Layer layer) {
    synchronized (INITIALIZING_LAYERS) {
      if (!INITIALIZING_LAYERS.contains(layer) && !layer.isInitialized()) {
        INITIALIZING_LAYERS.add(layer);
        if (instanceCount < 2) {
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
        synchronized (INITIALIZING_LAYERS) {
          if (INITIALIZING_LAYERS.isEmpty()) {
            instanceCount--;
            return null;
          } else {
            layer = INITIALIZING_LAYERS.removeFirst();

          }
        }
        try {
          layer.initialize();
        } catch (final Throwable e) {
          ExceptionUtil.log(layer.getClass(), "Unable to iniaitlize layer: "
            + layer.getName(), e);
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
