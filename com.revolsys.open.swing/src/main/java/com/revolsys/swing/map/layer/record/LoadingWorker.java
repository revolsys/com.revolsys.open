package com.revolsys.swing.map.layer.record;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;

import org.slf4j.LoggerFactory;

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

  @Override
  protected List<LayerRecord> doInBackground() throws Exception {
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

  public AbstractLayer getLayer() {
    return this.layer;
  }

  public BoundingBox getViewportBoundingBox() {
    return this.viewportBoundingBox;
  }

  @Override
  public String toString() {
    final PathName typePath = this.layer.getTypePath();
    return "Loading: " + typePath;
  }

  @Override
  protected void uiTask() {
    try {
      if (!isCancelled()) {
        final List<LayerRecord> records = get();

        this.layer.setIndexRecords(this.viewportBoundingBox, records);
      }
    } catch (final CancellationException e) {
      this.layer.clearLoading(this.viewportBoundingBox);
    } catch (final Throwable t) {
      final PathName typePath = this.layer.getTypePath();
      LoggerFactory.getLogger(getClass()).error("Unable to load " + typePath, t);
      this.layer.clearLoading(this.viewportBoundingBox);
    }
  }
}
