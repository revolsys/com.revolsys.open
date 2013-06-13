package com.revolsys.swing.map.layer.dataobject;

import java.util.List;
import java.util.concurrent.CancellationException;

import javax.swing.SwingWorker;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.algorithm.index.DataObjectQuadTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.query.Query;

public class LoadingWorker extends SwingWorker<DataObjectQuadTree, Void> {
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
    final GeometryFactory geometryFactory = layer.getGeometryFactory();
    final BoundingBox queryBoundingBox = viewportBoundingBox.convert(geometryFactory);
    Query query = layer.getQuery();
    if (query != null) {
      query = query.clone();
      query.setBoundingBox(queryBoundingBox);
      final List<LayerDataObject> objects = layer.query(query);
      index.insertAll(objects);
    }
    return index;
  }

  @Override
  protected void done() {
    try {
      if (!isCancelled()) {
        final DataObjectQuadTree index = get();

        layer.setIndex(viewportBoundingBox, index);
      }
    } catch (final CancellationException e) {
      layer.clearLoading(viewportBoundingBox);
    } catch (final Throwable t) {
      final String typePath = layer.getTypePath();
      LoggerFactory.getLogger(getClass())
        .error("Unable to load " + typePath, t);
      layer.clearLoading(viewportBoundingBox);
    }
  }

  public DataObjectStoreLayer getLayer() {
    return layer;
  }

  public BoundingBox getViewportBoundingBox() {
    return viewportBoundingBox;
  }

  @Override
  public String toString() {
    final String typePath = layer.getTypePath();
    return "Loading: " + typePath;
  }
}
