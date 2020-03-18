package com.revolsys.swing.map.layer.record;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingWorker;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;

public class BoundingBoxRecordLayer extends AbstractRecordLayer {

  private BoundingBox boundingBox;

  private boolean loading = false;

  private SwingWorker worker;

  private final Class<?> workerClass;

  public BoundingBoxRecordLayer(final String type, final String name, final Class<?> workerClass,
    final GeometryFactory geometryFactory) {
    super(type);
    setName(name);
    setFieldNamesSets(null);
    setGeometryFactory(geometryFactory);

    this.workerClass = workerClass;
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
              Logs.error(this, "Worker Constructor not found", e);
            } catch (final InvocationTargetException e) {
              Logs.error(this, "Unable to construct loader class", e.getTargetException());
            } catch (final Throwable e) {
              Logs.error(this, "Unable to construct loader class", e);
            }
          }
        }
        return getRecordsIndex(boundingBox);
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
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    map.remove("style");
    return map;
  }
}
