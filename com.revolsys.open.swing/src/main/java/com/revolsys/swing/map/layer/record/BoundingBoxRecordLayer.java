package com.revolsys.swing.map.layer.record;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import com.revolsys.datatype.DataType;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;

public class BoundingBoxRecordLayer extends AbstractRecordLayer {
  private static final Logger LOG = Logger.getLogger(BoundingBoxRecordLayer.class);

  private BoundingBox boundingBox;

  private boolean loading = false;

  private SwingWorker worker;

  private final Class<?> workerClass;

  public BoundingBoxRecordLayer(final String type, final String name, final Class<?> workerClass,
    final GeometryFactory geometryFactory) {
    super(name, geometryFactory);
    setType(type);
    this.workerClass = workerClass;
  }

  @Override
  public BoundingBoxRecordLayer clone() {
    final BoundingBoxRecordLayer clone = (BoundingBoxRecordLayer)super.clone();
    clone.boundingBox = null;
    clone.worker = null;
    return clone;
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  @Override
  public List<LayerRecord> getRecords(BoundingBox boundingBox) {
    if (hasGeometryField()) {
      boundingBox = convertBoundingBox(boundingBox);
      if (Property.hasValue(boundingBox)) {
        synchronized (getSync()) {
          if (this.loading) {
            if (!boundingBox.equals(this.boundingBox)) {
              this.boundingBox = null;
              this.worker.cancel(true);
              this.loading = false;
            }
          }
          if (this.boundingBox == null || !boundingBox.equals(this.boundingBox) && !this.loading) {
            this.loading = true;
            this.boundingBox = boundingBox;
            firePropertyChange("visible", super.isVisible(), false);
            try {
              final Constructor<?> constructor = this.workerClass
                .getConstructor(BoundingBoxRecordLayer.class, BoundingBox.class);
              this.worker = (SwingWorker)constructor.newInstance(this, boundingBox);
              Invoke.worker(this.worker);
            } catch (final NoSuchMethodException e) {
              LOG.error("Worker Constructor not found", e);
            } catch (final InvocationTargetException e) {
              LOG.error("Unable to construct loader class", e.getTargetException());
            } catch (final Throwable e) {
              LOG.error("Unable to construct loader class", e);
            }
          }
        }
        return (List)getIndex().queryIntersects(boundingBox);
      }
    }
    return Collections.emptyList();
  }

  @Override
  public boolean isVisible() {
    return !this.loading && super.isVisible();
  }

  public void setIndexRecords(final BoundingBox boundingBox, final List<LayerRecord> records) {
    synchronized (getSync()) {
      if (DataType.equal(this.boundingBox, boundingBox)) {
        setIndexRecords(records);
        this.worker = null;
        this.loading = false;
      }
    }
    firePropertyChange("refresh", false, true);
    firePropertyChange("visible", false, isVisible());
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    map.remove("style");
    return map;
  }
}
