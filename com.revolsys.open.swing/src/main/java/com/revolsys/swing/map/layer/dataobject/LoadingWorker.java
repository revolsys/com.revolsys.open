package com.revolsys.swing.map.layer.dataobject;

import java.util.List;
import java.util.concurrent.CancellationException;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.algorithm.index.DataObjectQuadTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.gis.data.query.Query;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.parallel.AbstractSwingWorker;

public class LoadingWorker extends
  AbstractSwingWorker<DataObjectQuadTree, Void> {
  private final BoundingBox viewportBoundingBox;

  private final DataObjectStoreLayer layer;

  public LoadingWorker(final DataObjectStoreLayer layer,
    final BoundingBox viewportBoundingBox) {
    this.layer = layer;
    this.viewportBoundingBox = viewportBoundingBox;

  }

  @Override
  protected DataObjectQuadTree doInBackground() throws Exception {
    final DataObjectQuadTree index = new DataObjectQuadTree();
    final GeometryFactory geometryFactory = this.layer.getGeometryFactory();
    final BoundingBox queryBoundingBox = this.viewportBoundingBox.convert(geometryFactory);
    Query query = this.layer.getQuery();
    if (query != null) {
      query = query.clone();
      query.setBoundingBox(queryBoundingBox);
      final List<LayerDataObject> records = this.layer.query(query);
      index.insertAll(records);
    }
    return index;
  }

  public AbstractLayer getLayer() {
    return this.layer;
  }

  public BoundingBox getViewportBoundingBox() {
    return this.viewportBoundingBox;
  }

  @Override
  public String toString() {
    final String typePath = this.layer.getTypePath();
    return "Loading: " + typePath;
  }

  @Override
  protected void uiTask() {
    try {
      if (!isCancelled()) {
        final DataObjectQuadTree index = get();

        this.layer.setIndex(this.viewportBoundingBox, index);
      }
    } catch (final CancellationException e) {
      this.layer.clearLoading(this.viewportBoundingBox);
    } catch (final Throwable t) {
      final String typePath = this.layer.getTypePath();
      LoggerFactory.getLogger(getClass())
        .error("Unable to load " + typePath, t);
      this.layer.clearLoading(this.viewportBoundingBox);
    }
  }
}
