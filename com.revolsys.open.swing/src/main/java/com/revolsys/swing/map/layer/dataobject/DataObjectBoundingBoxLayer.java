package com.revolsys.swing.map.layer.dataobject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import com.revolsys.gis.algorithm.index.DataObjectQuadTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.gis.cs.projection.GeometryOperation;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.jts.geom.Polygon;

public class DataObjectBoundingBoxLayer extends AbstractDataObjectLayer {
  private static final Logger LOG = Logger.getLogger(DataObjectBoundingBoxLayer.class);

  private boolean loading = false;

  private final Object sync = new Object();

  private BoundingBox boundingBox;

  private final Class<?> workerClass;

  private SwingWorker worker;

  public DataObjectBoundingBoxLayer(final String type, final String name,
    final Class<?> workerClass, final GeometryFactory geometryFactory) {
    super(name, geometryFactory);
    setType(type);
    this.workerClass = workerClass;
  }

  @Override
  public List<LayerDataObject> doQuery(final BoundingBox boundingBox) {
    if (boundingBox.isEmpty()) {
      return Collections.emptyList();
    } else {
      synchronized (this.sync) {
        if (this.loading) {
          if (!boundingBox.equals(this.boundingBox)) {
            this.boundingBox = null;
            this.worker.cancel(true);
            this.loading = false;
          }
        }
        if (this.boundingBox == null || !boundingBox.equals(this.boundingBox)
          && !this.loading) {
          this.loading = true;
          this.boundingBox = boundingBox;
          firePropertyChange("visible", super.isVisible(), false);
          try {
            final Constructor<?> constructor = this.workerClass.getConstructor(
              DataObjectBoundingBoxLayer.class, BoundingBox.class);
            this.worker = (SwingWorker)constructor.newInstance(this,
              boundingBox);
            Invoke.worker(this.worker);
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
      return (List)getIndex().queryIntersects(polygon);
    }
  }

  @Override
  protected List<LayerDataObject> doQuery(final Query query) {
    return Collections.emptyList();
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @Override
  public boolean isVisible() {
    return !this.loading && super.isVisible();
  }

  public void setIndex(final BoundingBox boundingBox,
    final DataObjectQuadTree index) {
    synchronized (this.sync) {
      if (EqualsRegistry.equal(this.boundingBox, boundingBox)) {
        setIndex(index);
        this.worker = null;
        this.loading = false;
      }
    }
    firePropertyChange("index", null, index);
    firePropertyChange("visible", false, isVisible());
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    map.remove("style");
    return map;
  }
}
