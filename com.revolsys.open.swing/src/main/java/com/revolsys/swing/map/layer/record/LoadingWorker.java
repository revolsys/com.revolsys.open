package com.revolsys.swing.map.layer.record;

import java.util.Collections;
import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.PathName;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.functions.F;
import com.revolsys.record.schema.FieldDefinition;
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
  protected List<LayerRecord> handleBackground() throws Exception {
    try {
      final GeometryFactory geometryFactory = this.layer.getGeometryFactory();
      final BoundingBox queryBoundingBox = this.viewportBoundingBox.convert(geometryFactory);
      Query query = this.layer.getQuery();
      final FieldDefinition geometryField = this.layer.getGeometryField();
      if (query != null && geometryField != null && !queryBoundingBox.isEmpty()) {
        query = query.clone();
        query.and(F.envelopeIntersects(geometryField, queryBoundingBox));
        final List<LayerRecord> records = this.layer.query(query);
        return records;
      }
      return Collections.emptyList();
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
    this.layer.clearLoading(this.viewportBoundingBox);
  }

  @Override
  protected void handleDone(final List<LayerRecord> records) {
    this.layer.setIndexRecords(this.viewportBoundingBox, records);
  }

  @Override
  protected void handleException(final Throwable exception) {
    super.handleException(exception);
    this.layer.clearLoading(this.viewportBoundingBox);
  }

  @Override
  public String toString() {
    final PathName typePath = this.layer.getTypePath();
    return "Load " + typePath;
  }
}
