package com.revolsys.swing.map.layer.dataobject;

import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingWorker;

import com.revolsys.gis.algorithm.index.DataObjectQuadTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.PathUtil;
import com.revolsys.swing.SwingWorkerManager;
import com.vividsolutions.jts.geom.Polygon;

public class DataObjectStoreLayer extends AbstractDataObjectLayer {


  private final DataObjectStore dataStore;

  private final Object sync = new Object();

  private final String typePath;

  private DataObjectQuadTree index = new DataObjectQuadTree();

  private BoundingBox boundingBox = new BoundingBox();

  private BoundingBox loadingBoundingBox = new BoundingBox();

  private SwingWorker<DataObjectQuadTree, Void> loadingWorker;

  protected void setIndex(BoundingBox loadedBoundingBox,
    DataObjectQuadTree index) {
    synchronized (sync) {
      if (loadedBoundingBox == loadingBoundingBox) {
        this.index = index;
        clearLoading(loadedBoundingBox);
      }

    }
  }
public DataObjectStore getDataStore() {
  return dataStore;
}

  protected void clearLoading(BoundingBox loadedBoundingBox) {
    synchronized (sync) {
      if (loadedBoundingBox == loadingBoundingBox) {
        final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
        propertyChangeSupport.firePropertyChange("loaded", false, true);
        boundingBox = loadingBoundingBox;
        loadingBoundingBox = new BoundingBox();
        loadingWorker = null;
      }

    }
  }

  public DataObjectStoreLayer(final DataObjectStore store, final String typePath) {
    super(PathUtil.getName(typePath), store.getMetaData(typePath)
      .getGeometryFactory());
    this.dataStore = store;
    this.typePath = typePath;
  }


  @Override
  public BoundingBox getBoundingBox() {
    return getCoordinateSystem().getAreaBoundingBox();
  }

  public String getTypePath() {
    return typePath;
  }
  @Override
  public List<DataObject> getDataObjects(BoundingBox boundingBox) {
    if (boundingBox.isNull()) {
      return Collections.emptyList();
    } else {
      synchronized (sync) {
        boundingBox = new BoundingBox(boundingBox);
        boundingBox.expandPercent(0.2);
        if (!this.boundingBox.contains(boundingBox)
          && !loadingBoundingBox.contains(boundingBox)) {
          if (loadingWorker != null) {
            loadingWorker.cancel(true);
          }
          loadingBoundingBox = boundingBox;
          loadingWorker = createLoadingWorker(boundingBox);
          SwingWorkerManager.execute(loadingWorker);
        }
      }
      Polygon polygon = boundingBox.toPolygon();
      final GeometryFactory geometryFactory = getGeometryFactory();
      polygon = geometryFactory.project(polygon);

      return index.queryIntersects(polygon);
    }
  }
  protected LoadingWorker createLoadingWorker(BoundingBox boundingBox) {
    return new LoadingWorker(this, boundingBox);
  }

  @Override
  public DataObjectMetaData getMetaData() {
    return dataStore.getMetaData(typePath);
  }

}
