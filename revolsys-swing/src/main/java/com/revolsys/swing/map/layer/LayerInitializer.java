package com.revolsys.swing.map.layer;

import java.util.LinkedList;

import org.jeometry.common.logging.Logs;

import com.revolsys.record.io.RecordStoreConnectionRegistry;
import com.revolsys.swing.parallel.AbstractSwingWorker;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.parallel.MaxThreadsSwingWorker;

public class LayerInitializer extends AbstractSwingWorker<Void, Void>
  implements MaxThreadsSwingWorker {
  private static final LinkedList<Layer> LAYERS_CURRENTLY_INITIALIZING = new LinkedList<>();

  private static final LinkedList<Layer> LAYERS_TO_INITIALIZE = new LinkedList<>();

  public static void initialize(final Layer layer) {
    synchronized (LAYERS_TO_INITIALIZE) {
      if (!LAYERS_CURRENTLY_INITIALIZING.contains(layer) && !LAYERS_TO_INITIALIZE.contains(layer)
        && !layer.isInitialized()) {
        // LAYERS_TO_INITIALIZE.add(layer);
        // if (instanceCount < MAX_WORKERS) {
        // instanceCount++;
        final LayerInitializer initializer = new LayerInitializer(layer);
        Invoke.worker(initializer);
      }
      // }
    }
  }

  private final Layer layer;

  private final RecordStoreConnectionRegistry recordStoreRegistry;

  public LayerInitializer(final Layer layer) {
    super(false);
    this.layer = layer;
    this.recordStoreRegistry = RecordStoreConnectionRegistry.getForThread();
  }

  @Override
  public int getMaxThreads() {
    return 5;
  }

  @Override
  protected Void handleBackground() {
    try {
      RecordStoreConnectionRegistry.setForThread(this.recordStoreRegistry);
      synchronized (LAYERS_TO_INITIALIZE) {

        // if (LAYERS_TO_INITIALIZE.isEmpty()) {
        // instanceCount--;
        // return null;
        // } else {
        // this.layer = LAYERS_TO_INITIALIZE.removeFirst();
        LAYERS_TO_INITIALIZE.remove(this.layer);
        LAYERS_CURRENTLY_INITIALIZING.add(this.layer);
        // }
      }
      try {
        this.layer.initialize();
      } catch (final Throwable e) {
        Logs.error(this.layer.getClass(), "Unable to iniaitlize layer: " + this.layer.getName(), e);
      } finally {
        LAYERS_CURRENTLY_INITIALIZING.remove(this.layer);
      }
      return null;
    } finally {
      RecordStoreConnectionRegistry.setForThread(null);
    }
  }

  @Override
  public String toString() {
    // if (this.layer == null) {
    // return "INITIALIZING layers";
    // } else {
    return "INITIALIZING layer: " + this.layer.getPath();
    // }
  }
}
