package com.revolsys.swing.map.layer.dataobject;

import java.beans.PropertyChangeSupport;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import com.revolsys.gis.algorithm.index.DataObjectQuadTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.GeometryOperation;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.Reader;
import com.revolsys.swing.SwingWorkerManager;
import com.vividsolutions.jts.geom.Polygon;

public class DataObjectBoundingBoxLayer extends AbstractDataObjectLayer {
  private static final Logger LOG = Logger.getLogger(DataObjectBoundingBoxLayer.class);

  private DataObjectQuadTree index;

  private Reader<DataObject> reader;

  private boolean loading = false;

  private final Object sync = new Object();

  private BoundingBox boundingBox;

  private final Class<?> workerClass;

  private SwingWorker worker;

  public DataObjectBoundingBoxLayer(final String name,
    final Class<?> workerClass, final GeometryFactory geometryFactory) {
    super(name, geometryFactory);
    this.workerClass = workerClass;
  }

  @Override
  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  @Override
  public List<DataObject> getDataObjects(final BoundingBox boundingBox) {
    if (boundingBox.isNull()) {
      return Collections.emptyList();
    } else {
      synchronized (sync) {
        if (loading) {
          if (!boundingBox.equals(this.boundingBox)) {
            this.boundingBox = null;
            worker.cancel(true);
            loading = false;
          }
        }
        if (this.boundingBox == null || !boundingBox.equals(this.boundingBox)
          && !loading) {
          loading = true;
          this.boundingBox = boundingBox;
          getPropertyChangeSupport().firePropertyChange("visible",
            super.isVisible(), false);
          try {
            final Constructor<?> constructor = workerClass.getConstructor(
              DataObjectBoundingBoxLayer.class, BoundingBox.class);
            worker = (SwingWorker)constructor.newInstance(this, boundingBox);
            SwingWorkerManager.execute(worker);
          } catch (final NoSuchMethodException e) {
            LOG.error("Worker Constructor not found", e);
          } catch (final InvocationTargetException e) {
            LOG.error("Unable to construct loader class",
              e.getTargetException());
          } catch (final Throwable e) {
            LOG.error("Unable to construct loader class", e);
          }
        }
      }
      if (index != null) {
        Polygon polygon = boundingBox.toPolygon();
        final GeometryFactory geometryFactory = getGeometryFactory();
        final GeometryFactory bboxGeometryFactory = boundingBox.getGeometryFactory();
        if (geometryFactory != null
          && !geometryFactory.equals(bboxGeometryFactory)) {
          final GeometryOperation operation = ProjectionFactory.getGeometryOperation(
            bboxGeometryFactory, geometryFactory);
          if (operation != null) {
            polygon = operation.perform(polygon);
          }
        }
        return index.queryIntersects(polygon);
      } else {
        return Collections.emptyList();
      }
    }
  }

  @Override
  public boolean isVisible() {
    return !loading && super.isVisible();
  }

  public void setIndex(final BoundingBox boundingBox,
    final DataObjectQuadTree index) {
    synchronized (sync) {
      if (this.boundingBox.equals(boundingBox)) {
        this.index = index;
        worker = null;
        loading = false;
      }
    }
    final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
    propertyChangeSupport.firePropertyChange("index", null, index);
    propertyChangeSupport.firePropertyChange("visible", false, isVisible());
  }

}
