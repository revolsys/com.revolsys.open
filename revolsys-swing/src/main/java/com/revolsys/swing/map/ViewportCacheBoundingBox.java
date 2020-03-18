package com.revolsys.swing.map;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.SwingWorker;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.parallel.AbstractSwingWorker;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Cancellable;

public class ViewportCacheBoundingBox implements BoundingBoxProxy, Cancellable {
  public static final ViewportCacheBoundingBox EMPTY = new ViewportCacheBoundingBox();

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

  private ViewportCacheBoundingBox() {
    this.cancelled = true;
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

  public synchronized void clearCache(final Layer layer) {
    this.cachedItemsByLayer.remove(layer);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @SuppressWarnings("unchecked")
  public <V> V getCachedItem(final Layer layer, final Object key) {
    if (hasDimension()) {
      final Map<Object, Object> cachedItems;
      synchronized (this.cachedItemsByLayer) {
        cachedItems = this.cachedItemsByLayer.get(layer);
      }
      if (cachedItems == null) {
        return null;
      } else {
        synchronized (cachedItems) {
          return (V)cachedItems.get(key);
        }
      }
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public <V> V getCachedItem(final Layer layer, final Object key, final Supplier<V> constructor) {
    if (hasDimension()) {
      final Map<Object, Object> cachedItems = getCachedItems(layer);
      synchronized (cachedItems) {
        Object item = cachedItems.get(key);
        if (item == null) {
          item = constructor.get();
          cachedItems.put(key, item);
        }
        return (V)item;
      }
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public <V> V getCachedItemFuture(final String taskName, final Layer layer, final Object key,
    final Supplier<V> constructor, final Consumer<Throwable> errorHandler) {
    if (hasDimension()) {
      final Map<Object, Object> cachedItems = getCachedItems(layer);

      synchronized (cachedItems) {
        final Object cachedItem = cachedItems.get(key);
        SwingWorker<V, Object> worker;
        if (cachedItem instanceof SwingWorker) {
          worker = (SwingWorker<V, Object>)cachedItem;
        } else if (cachedItem == null) {
          worker = null;
        } else {
          return (V)cachedItem;
        }

        if (worker == null || worker.isCancelled()) {
          worker = new AbstractSwingWorker<>() {
            @Override
            public String getTaskTitle() {
              return taskName;
            }

            @Override
            protected V handleBackground() {
              final V value = constructor.get();
              synchronized (cachedItems) {
                cachedItems.put(key, value);
              }
              return value;
            }

            protected void handleException(final Throwable exception) {
              if (!isCancelled()) {
                errorHandler.accept(exception);
              }
            }

            protected void handleFinished() {
              ViewportCacheBoundingBox.this.tasks.remove(this);
              layer.firePropertyChange("redraw", false, true);
            }
          };
          cachedItems.put(key, worker);
          this.tasks.add(worker);
          Invoke.worker(worker);
        }
        if (worker.isDone()) {
          try {
            return worker.get();
          } catch (InterruptedException | ExecutionException e) {

          }
        }

        return null;
      }
    } else {
      return null;
    }
  }

  private Map<Object, Object> getCachedItems(final Layer layer) {
    synchronized (this.cachedItemsByLayer) {
      Map<Object, Object> cachedItems = this.cachedItemsByLayer.get(layer);
      if (cachedItems == null) {
        cachedItems = new HashMap<>();
        this.cachedItemsByLayer.put(layer, cachedItems);
      }
      return cachedItems;
    }
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

  public boolean hasDimension() {
    return !this.cancelled && this.viewHeightPixels != 0 && this.viewWidthPixels != 0;
  }

  @Override
  public boolean isCancelled() {
    return this.cancelled;
  }

  public synchronized boolean isDimension(final double viewWidthPixels,
    final double viewHeightPixels) {
    return this.viewWidthPixels == (int)Math.ceil(viewWidthPixels)
      && this.viewHeightPixels == (int)Math.ceil(viewHeightPixels);
  }

  public void setCachedItem(final Layer layer, final Object key, final Object item) {
    if (hasDimension()) {
      final Map<Object, Object> cachedItems = getCachedItems(layer);
      synchronized (cachedItems) {
        cachedItems.put(key, item);
      }
    }
  }

  @Override
  public String toString() {
    return getBoundingBox() + " " + this.viewWidthPixels + "x" + this.viewHeightPixels;
  }

}
