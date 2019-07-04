package com.revolsys.swing.map;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import javax.swing.SwingWorker;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.util.Cancellable;

public class ViewportCacheBoundingBox implements BoundingBoxProxy, Cancellable {
  private BoundingBox boundingBox = BoundingBox.empty();

  private GeometryFactory geometryFactory = GeometryFactory.DEFAULT_2D;

  private GeometryFactory geometryFactory2dFloating = GeometryFactory.DEFAULT_2D;

  private int viewWidthPixels;

  private int viewHeightPixels;

  private double unitsPerPixel;

  private double metresPerPixel;

  private double scale = 1;

  private double modelUnitsPerViewUnit = 1;

  private final List<Future<?>> tasks = new LinkedList<>();

  private boolean cancelled = false;

  private final Map<Layer, Map<Object, Object>> cachedItemsByLayer = new HashMap<>();

  public ViewportCacheBoundingBox() {
  }

  public ViewportCacheBoundingBox(final int width, final int height) {
    this.geometryFactory = GeometryFactory.DEFAULT_2D;
    this.boundingBox = this.geometryFactory.newBoundingBox(0, 0, width, height);
    this.geometryFactory2dFloating = GeometryFactory.DEFAULT_2D;
    this.viewWidthPixels = width;
    this.viewHeightPixels = height;
    this.unitsPerPixel = 1;
    this.metresPerPixel = 1;
    this.scale = 1;
    this.modelUnitsPerViewUnit = 1;
  }

  public ViewportCacheBoundingBox(final Viewport2D viewport) {
    this.boundingBox = viewport.getBoundingBox();
    this.geometryFactory = viewport.getGeometryFactory();
    this.geometryFactory2dFloating = viewport.getGeometryFactory2dFloating();
    this.viewWidthPixels = (int)Math.ceil(viewport.getViewWidthPixels());
    this.viewHeightPixels = (int)Math.ceil(viewport.getViewHeightPixels());
    this.unitsPerPixel = viewport.getUnitsPerPixel();
    this.metresPerPixel = viewport.getMetresPerPixel();
    this.scale = viewport.getScale();
    this.modelUnitsPerViewUnit = this.boundingBox.getHeight() / this.viewHeightPixels;
  }

  public void addTask(final Future<?> task) {
    synchronized (this.tasks) {
      if (!this.cancelled) {
        if (task instanceof SwingWorker) {
          final SwingWorker<?, ?> worker = (SwingWorker<?, ?>)task;
          worker.addPropertyChangeListener(e -> {
            if (task.isDone()) {
              this.tasks.remove(task);
            }
          });
        }
        if (!task.isDone()) {
          this.tasks.add(task);
        }
      }
    }
  }

  public void cancel() {
    synchronized (this.tasks) {
      this.cancelled = true;
      for (final Future<?> task : this.tasks) {
        task.cancel(true);
      }
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @SuppressWarnings("unchecked")
  public <V> V getCachedItem(final Layer layer, final Object key) {
    final Map<Object, Object> cachedItems = this.cachedItemsByLayer.get(layer);
    if (cachedItems == null) {
      return null;
    } else {
      return (V)cachedItems.get(key);
    }
  }

  @SuppressWarnings("unchecked")
  public <V> V getCachedItem(final Layer layer, final Object key, final Supplier<V> constructor) {
    Map<Object, Object> cachedItems = this.cachedItemsByLayer.get(layer);
    if (cachedItems == null) {
      cachedItems = new HashMap<>();
    }
    Object item = cachedItems.get(key);
    if (item == null) {
      item = constructor.get();
      cachedItems.put(key, item);
    }
    return (V)item;
  }

  @SuppressWarnings("unchecked")
  public <V> V getCachedItemFuture(final Layer layer, final Object key,
    final Supplier<Future<V>> futureConstructor) {
    Map<Object, Object> cachedItems = this.cachedItemsByLayer.get(layer);
    if (cachedItems == null) {
      cachedItems = new HashMap<>();
      this.cachedItemsByLayer.put(layer, cachedItems);
    }
    Future<V> future = (Future<V>)cachedItems.get(key);
    if (future == null) {
      future = futureConstructor.get();
      this.tasks.add(future);
      cachedItems.put(key, future);
    }

    if (future.isDone() && !future.isCancelled()) {
      try {
        return future.get();
      } catch (final InterruptedException e) {
        return null;
      } catch (final ExecutionException e) {
        throw Exceptions.wrap(e);
      }
    }
    return null;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public GeometryFactory getGeometryFactory2dFloating() {
    return this.geometryFactory2dFloating;
  }

  public double getMetresPerPixel() {
    return this.metresPerPixel;
  }

  public double getModelUnitsPerViewUnit() {
    return this.modelUnitsPerViewUnit;
  }

  public double getScale() {
    return this.scale;
  }

  public double getUnitsPerPixel() {
    return this.unitsPerPixel;
  }

  public int getViewHeightPixels() {
    return this.viewHeightPixels;
  }

  public int getViewWidthPixels() {
    return this.viewWidthPixels;
  }

  @Override
  public boolean isCancelled() {
    return this.cancelled;
  }

  @Override
  public String toString() {
    return getBoundingBox() + " " + this.viewWidthPixels + "x" + this.viewHeightPixels;
  }
}
