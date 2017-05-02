package com.revolsys.swing.map.layer.raster;

import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.AbstractTiledImageLayer;
import com.revolsys.swing.map.layer.MapTile;
import com.revolsys.swing.parallel.RunnableSwingWorkerManager;
import com.revolsys.util.Cancellable;
import com.revolsys.util.Property;

public class TiledImageLayerRenderer extends AbstractLayerRenderer<AbstractTiledImageLayer>
  implements PropertyChangeListener {

  public static final String TILES_LOADED = "loading";

  private static RunnableSwingWorkerManager tileLoaderManager = new RunnableSwingWorkerManager(
    "Load Map Tiles");

  private final Map<MapTile, MapTile> cachedTiles = new HashMap<>();

  private GeometryFactory geometryFactory;

  private final List<Runnable> loadingTasks = new ArrayList<>();

  private double resolution;

  public TiledImageLayerRenderer(final AbstractTiledImageLayer layer) {
    super("tiledImage", layer);
    Property.addListener(layer, this);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object newValue = event.getNewValue();
    if (newValue instanceof BoundingBoxDoubleGf) {
      final BoundingBox newBoundingBox = (BoundingBox)newValue;
      synchronized (this.cachedTiles) {
        final List<MapTile> mapTiles = new ArrayList<>(this.cachedTiles.keySet());
        final GeometryFactory newGeometryFactory = newBoundingBox.getGeometryFactory();
        for (final MapTile mapTile : mapTiles) {
          final BoundingBox boundingBox = mapTile.getBoundingBox();
          final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
          if (!geometryFactory.equals(newGeometryFactory)
            || !newBoundingBox.intersects(boundingBox)) {
            this.cachedTiles.remove(boundingBox);
          }
        }
      }
    } else if (!TILES_LOADED.equals(event.getPropertyName())) {
      synchronized (this.cachedTiles) {
        this.cachedTiles.clear();
        // if (this.tileLoaderProcess != null) {
        // this.tileLoaderProcess.cancel(true);
        // }
      }
    }
  }

  @Override
  public void render(final Viewport2D viewport, final Cancellable cancellable,
    final AbstractTiledImageLayer layer) {
    final GeometryFactory geometryFactory = viewport.getGeometryFactory();
    final double resolution = layer.getResolution(viewport);
    synchronized (this.cachedTiles) {
      if (resolution != this.resolution || geometryFactory != this.geometryFactory) {
        this.resolution = resolution;
        this.geometryFactory = geometryFactory;
        this.cachedTiles.clear();
        tileLoaderManager.removeTasks(this.loadingTasks);
        this.loadingTasks.clear();
      }
    }
    final List<Runnable> tasks = new ArrayList<>();
    final List<MapTile> mapTiles = layer.getOverlappingMapTiles(viewport);
    for (final MapTile mapTile : cancellable.cancellable(mapTiles)) {
      if (mapTile != null) {
        MapTile cachedTile = null;
        synchronized (this.cachedTiles) {
          cachedTile = this.cachedTiles.get(mapTile);
          if (cachedTile == null) {
            cachedTile = mapTile;
            this.cachedTiles.put(cachedTile, cachedTile);
            final Runnable task = new TileLoadTask(this, cancellable, geometryFactory, cachedTile);
            tasks.add(task);
          }
        }
        if (!cancellable.isCancelled()) {
          final GeoreferencedImage image = cachedTile.getImage(geometryFactory);
          final Graphics2D graphics = viewport.getGraphics();
          if (graphics != null) {
            GeoreferencedImageLayerRenderer.render(viewport, graphics, image, false);
          }
        }
      }
    }
    synchronized (this.loadingTasks) {
      this.loadingTasks.addAll(tasks);
      tileLoaderManager.addTasks(tasks);
    }
  }

  public void setLoaded(final TileLoadTask tileLoadTask) {
    this.loadingTasks.remove(tileLoadTask);
    final AbstractTiledImageLayer layer = getLayer();
    if (layer != null) {
      layer.firePropertyChange(TILES_LOADED, false, true);
    }
  }

  @Override
  public MapEx toMap() {
    return MapEx.EMPTY;
  }
}
