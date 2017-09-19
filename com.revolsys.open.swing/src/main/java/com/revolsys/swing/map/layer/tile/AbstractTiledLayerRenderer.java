package com.revolsys.swing.map.layer.tile;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.parallel.RunnableSwingWorkerManager;
import com.revolsys.util.BooleanCancellable;
import com.revolsys.util.Cancellable;
import com.revolsys.util.Property;

public abstract class AbstractTiledLayerRenderer<D, T extends AbstractMapTile<D>>
  extends AbstractLayerRenderer<AbstractTiledLayer<D, T>> implements PropertyChangeListener {

  public static final String TILES_LOADED = "loading";

  private static RunnableSwingWorkerManager tileLoaderManager = new RunnableSwingWorkerManager(
    "Load Map Tiles");

  private final Map<T, T> cachedTiles = new HashMap<>();

  private BooleanCancellable cancellable = new BooleanCancellable();

  private GeometryFactory geometryFactory;

  private final List<Runnable> loadingTasks = new ArrayList<>();

  private double resolution;

  public AbstractTiledLayerRenderer(final String type, final AbstractTiledLayer<D, T> layer) {
    super(type, layer);
    Property.addListener(layer, this);
  }

  public AbstractTiledLayerRenderer(final String type, final String name) {
    super(type, name);
  }

  protected void clearCachedTiles() {
    synchronized (this.cachedTiles) {
      this.cachedTiles.clear();
      tileLoaderManager.removeTasks(this.loadingTasks);
      this.loadingTasks.clear();
      this.cancellable.cancel();
      this.cancellable = new BooleanCancellable();
    }
  }

  public T getCachedTile(final T mapTile) {
    return this.cachedTiles.get(mapTile);
  }

  public double getResolution() {
    return this.resolution;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object newValue = event.getNewValue();
    if (newValue instanceof BoundingBox) {
      final BoundingBox newBoundingBox = (BoundingBox)newValue;
      synchronized (this.cachedTiles) {
        final List<T> mapTiles = new ArrayList<>(this.cachedTiles.keySet());
        final GeometryFactory newGeometryFactory = newBoundingBox.getGeometryFactory();
        for (final T mapTile : mapTiles) {
          final BoundingBox boundingBox = mapTile.getBoundingBox();
          final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
          if (!geometryFactory.equals(newGeometryFactory)
            || !newBoundingBox.intersects(boundingBox)) {
            this.cachedTiles.remove(mapTile);
          }
        }
      }
    } else if (!TILES_LOADED.equals(event.getPropertyName())) {
      clearCachedTiles();
    }
    if (!(event.getSource() instanceof Layer)) {
      firePropertyChange(event);
    }
  }

  @Override
  public void render(final Viewport2D viewport, final Cancellable cancellable,
    final AbstractTiledLayer<D, T> layer) {
    final GeometryFactory viewportGeometryFactory = viewport.getGeometryFactory();
    final double layerResolution = layer.getResolution(viewport);
    synchronized (this.cachedTiles) {
      if (layerResolution != this.resolution || viewportGeometryFactory != this.geometryFactory) {
        this.resolution = layerResolution;
        this.geometryFactory = viewportGeometryFactory;
        clearCachedTiles();
      }
    }
    final List<Runnable> tasks = new ArrayList<>();
    final List<T> mapTiles = layer.getOverlappingMapTiles(viewport);
    for (final T mapTile : cancellable.cancellable(mapTiles)) {
      if (mapTile != null) {
        T cachedTile = null;
        synchronized (this.cachedTiles) {
          cachedTile = getCachedTile(mapTile);
          if (cachedTile == null) {
            cachedTile = mapTile;
            this.cachedTiles.put(cachedTile, cachedTile);
            final Runnable task = new TileLoadTask<D, T>(this, this.cancellable,
              viewportGeometryFactory, cachedTile);
            tasks.add(task);
          }
        }
        if (!cancellable.isCancelled()) {
          renderTile(viewport, cancellable, cachedTile);
        }
      }
    }
    synchronized (this.loadingTasks) {
      this.loadingTasks.addAll(tasks);
      tileLoaderManager.setDescription("Load tiles: " + layer.getPath());
      tileLoaderManager.addTasks(tasks);
    }
  }

  protected abstract void renderTile(final Viewport2D viewport, final Cancellable cancellable,
    final T tile);

  public void setLoaded(final TileLoadTask<D, T> tileLoadTask) {
    this.loadingTasks.remove(tileLoadTask);
    final AbstractTiledLayer<D, T> layer = getLayer();
    if (layer != null) {
      layer.firePropertyChange(TILES_LOADED, false, true);
    }
  }

}
