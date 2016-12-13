package com.revolsys.swing.map.layer.record;

import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.io.PathName;
import com.revolsys.record.query.Query;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.parallel.AbstractSwingWorker;

public class LoadingWorker extends AbstractSwingWorker<List<LayerRecord>, Void> {
  private final RecordStoreLayer layer;

  private final BoundingBox viewportBoundingBox;

  public LoadingWorker(final RecordStoreLayer layer, final BoundingBox viewportBoundingBox) {
    this.layer = layer;
    this.viewportBoundingBox = viewportBoundingBox;

  }

  public AbstractLayer getLayer() {
    return this.layer;
  }

  public BoundingBox getViewportBoundingBox() {
    return this.viewportBoundingBox;
  }

  @Override
  protected List<LayerRecord> handleBackground() {
    try {
      final Query query = this.layer.newBoundingBoxQuery(this.viewportBoundingBox);
      // TODO cancellable
      final List<LayerRecord> records = this.layer.getRecords(query);
      this.layer.setIndexRecords(this.viewportBoundingBox, records);
      return records;
    } catch (final Exception e) {
      if (this.layer.isDeleted()) {
        return null;
      } else {
        throw e;
      }
    }
  }

  @Override
  protected void handleCancelled() {
    this.layer.cancelLoading(this.viewportBoundingBox);
  }

  @Override
  protected void handleDone(final List<LayerRecord> records) {
  }

  @Override
  protected void handleException(final Throwable exception) {
    super.handleException(exception);
    this.layer.cancelLoading(this.viewportBoundingBox);
  }

  @Override
  public String toString() {
    final PathName typePath = this.layer.getPathName();
    return "Load " + typePath;
  }
}
