package com.revolsys.swing.map.layer.record;

import java.util.List;

import org.jeometry.common.io.PathName;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.record.query.Query;
import com.revolsys.swing.map.ViewportCacheBoundingBox;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.parallel.AbstractSwingWorker;

public class LoadingWorker extends AbstractSwingWorker<List<LayerRecord>, Void> {
  private final RecordStoreLayer layer;

  private final ViewportCacheBoundingBox cache;

  public LoadingWorker(final RecordStoreLayer layer, final ViewportCacheBoundingBox cache) {
    this.layer = layer;
    this.cache = cache;

  }

  public BoundingBox getBoundingBox() {
    return this.cache.getBoundingBox();
  }

  public AbstractLayer getLayer() {
    return this.layer;
  }

  @Override
  protected List<LayerRecord> handleBackground() {
    try {
      final BoundingBox boundingBox = getBoundingBox();
      final Query query = this.layer.newBoundingBoxQuery(boundingBox);
      query.setCancellable(this);
      final List<LayerRecord> records = this.layer.getRecords(query);
      this.layer.setIndexRecords(this, records);
      return records;
    } catch (final Exception e) {
      if (this.layer.isDeleted() || isCancelled()) {
        return null;
      } else {
        throw e;
      }
    }
  }

  @Override
  protected void handleCancelled() {
    this.layer.cancelLoading(this);
  }

  @Override
  protected void handleDone(final List<LayerRecord> records) {
  }

  @Override
  protected void handleException(final Throwable exception) {
    super.handleException(exception);
    this.layer.cancelLoading(this);
  }

  @Override
  public String toString() {
    final PathName typePath = this.layer.getPathName();
    return "Load " + typePath;
  }
}
