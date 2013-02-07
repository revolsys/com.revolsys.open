package com.revolsys.swing.map.layer.dataobject;

import java.util.concurrent.CancellationException;

import javax.swing.SwingWorker;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.algorithm.index.DataObjectQuadTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.Reader;

public class LoadingWorker extends SwingWorker<DataObjectQuadTree, Void> {
  private final BoundingBox viewportBoundingBox;

  private DataObjectStoreLayer layer;

  public LoadingWorker(final DataObjectStoreLayer layer,
    final BoundingBox viewportBoundingBox) {
    super();
    this.layer = layer;
    this.viewportBoundingBox = viewportBoundingBox;

  }

  @Override
  protected DataObjectQuadTree doInBackground() throws Exception {
    String typePath = layer.getTypePath();
    final DataObjectQuadTree index = new DataObjectQuadTree();
    final GeometryFactory geometryFactory = layer.getGeometryFactory();
    final BoundingBox queryBoundingBox = viewportBoundingBox.convert(geometryFactory);
    final Reader<DataObject> reader = layer.getDataStore().query(typePath,
      queryBoundingBox);
    try {
      for (final DataObject object : reader) {
        if (isCancelled()) {
          return null;
        }
        index.insert(object);
      }
    } finally {
      reader.close();
    }
    return index;
  }

  public DataObjectStoreLayer getLayer() {
    return layer;
  }

  public BoundingBox getViewportBoundingBox() {
    return viewportBoundingBox;
  }

  @Override
  protected void done() {
    try {
      if (!isCancelled()) {
        DataObjectQuadTree index = get();
        layer.setIndex(viewportBoundingBox, index);
      }
    } catch (final CancellationException e) {
      layer.clearLoading(viewportBoundingBox);
    } catch (final Throwable t) {
      String typePath = layer.getTypePath();
      LoggerFactory.getLogger(getClass())
        .error("Unable to load " + typePath, t);
      layer.clearLoading(viewportBoundingBox);
    }
  }

  @Override
  public String toString() {
    String typePath = layer.getTypePath();
    return "Loading: " + typePath;
  }
}
