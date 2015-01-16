package com.revolsys.swing.map.layer;

import java.util.LinkedList;

import javax.swing.SwingWorker;

import com.revolsys.io.datastore.RecordStoreConnectionRegistry;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.ExceptionUtil;

public class LayerInitializer extends SwingWorker<Void, Void> {

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

  private static final int MAX_WORKERS = 5;

  private static final LinkedList<Layer> LAYERS_TO_INITIALIZE = new LinkedList<Layer>();

  private static final LinkedList<Layer> LAYERS_CURRENTLY_INITIALIZING = new LinkedList<Layer>();

  private static int instanceCount;

  private final RecordStoreConnectionRegistry recordStoreRegistry;

  private Layer layer;

  public LayerInitializer() {
    this.recordStoreRegistry = RecordStoreConnectionRegistry.getForThread();
  }

  @Override
  protected Void doInBackground() throws Exception {
    try {
      RecordStoreConnectionRegistry.setForThread(this.recordStoreRegistry);
      while (true) {
        synchronized (LAYERS_TO_INITIALIZE) {
          if (LAYERS_TO_INITIALIZE.isEmpty()) {
            instanceCount--;
            return null;
          } else {
            this.layer = LAYERS_TO_INITIALIZE.removeFirst();
            LAYERS_CURRENTLY_INITIALIZING.add(this.layer);
          }
        }
        try {
          this.layer.initialize();
        } catch (final Throwable e) {
          ExceptionUtil.log(this.layer.getClass(), "Unable to iniaitlize layer: "
              + this.layer.getName(), e);
        } finally {
          LAYERS_CURRENTLY_INITIALIZING.remove(this.layer);
        }
      }
    } finally {
      RecordStoreConnectionRegistry.setForThread(null);
      this.layer = null;
    }
  }

  @Override
  public String toString() {
    if (this.layer == null) {
      return "Initializing layers";
    } else {
      return "Initializing layer: " + this.layer.getPath();
    }
  }
}
