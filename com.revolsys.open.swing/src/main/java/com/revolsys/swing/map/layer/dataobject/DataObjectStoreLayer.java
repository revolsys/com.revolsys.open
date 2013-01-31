package com.revolsys.swing.map.layer.dataobject;

import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;

import javax.swing.SwingWorker;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.algorithm.index.DataObjectQuadTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Reader;
import com.revolsys.swing.SwingWorkerManager;
import com.vividsolutions.jts.geom.Polygon;

public class DataObjectStoreLayer extends AbstractDataObjectLayer {

  private final DataObjectStore store;

  private final Object sync = new Object();

  private final String typePath;

  private DataObjectQuadTree index = new DataObjectQuadTree();

  private BoundingBox boundingBox = new BoundingBox();

  public DataObjectStoreLayer(final DataObjectStore store, final String typePath) {
    super(PathUtil.getName(typePath), store.getMetaData(typePath)
      .getGeometryFactory());
    this.store = store;
    this.typePath = typePath;
  }

  public DataObjectStoreLayer(final String name, final DataObjectStore store,
    final String typePath, final GeometryFactory geometryFactory) {
    super(name, geometryFactory);
    this.store = store;
    this.typePath = typePath;
  }

  @Override
  public BoundingBox getBoundingBox() {
    return getCoordinateSystem().getAreaBoundingBox();
  }

  private BoundingBox loadingBoundingBox = new BoundingBox();

  private SwingWorker<DataObjectQuadTree, Void> loadingWorker;

  private class LoadingWorker extends SwingWorker<DataObjectQuadTree, Void> {
    private BoundingBox viewportBoundingBox;

    private LoadingWorker(BoundingBox viewportBoundingBox) {
      super();
      this.viewportBoundingBox = viewportBoundingBox;
    }

    @Override
    protected DataObjectQuadTree doInBackground() throws Exception {
      final DataObjectQuadTree index = new DataObjectQuadTree();
      GeometryFactory geometryFactory = getGeometryFactory();
      BoundingBox queryBoundingBox = viewportBoundingBox.convert(geometryFactory);
      Reader<DataObject> reader = store.query(typePath, queryBoundingBox);
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

    @Override
    protected void done() {
      synchronized (sync) {

        try {
          if (!isCancelled()) {
            index = get();
            final DataObjectStoreLayer layer = DataObjectStoreLayer.this;
            PropertyChangeSupport propertyChangeSupport = layer.getPropertyChangeSupport();
            propertyChangeSupport.firePropertyChange("loaded", false, true);
          }
        } catch (CancellationException e) {
        } catch (final Throwable t) {
          LoggerFactory.getLogger(getClass()).error(
            "Unable to load " + typePath, t);
        } finally {
          boundingBox = loadingBoundingBox;
          loadingBoundingBox = new BoundingBox();
          loadingWorker = null;
        }
      }
    }

    public String toString() {
      return "Loading: " + typePath;
    }
  };

  @Override
  public List<DataObject> getDataObjects(final BoundingBox boundingBox) {
    if (boundingBox.isNull()) {
      return Collections.emptyList();
    } else {
      synchronized (sync) {
        if (!this.boundingBox.contains(boundingBox)
          && !loadingBoundingBox.contains(boundingBox)) {
          if (loadingWorker != null) {
            loadingWorker.cancel(true);
          }
          loadingBoundingBox = boundingBox;
          loadingWorker = new LoadingWorker(boundingBox);
          SwingWorkerManager.execute(loadingWorker);
        }
      }
      Polygon polygon = boundingBox.toPolygon();
      final GeometryFactory geometryFactory = getGeometryFactory();
      polygon = geometryFactory.project(polygon);

      return index.queryIntersects(polygon);
    }
  }

  public DataObjectMetaData getMetaData() {
    return store.getMetaData(typePath);
  }
}
