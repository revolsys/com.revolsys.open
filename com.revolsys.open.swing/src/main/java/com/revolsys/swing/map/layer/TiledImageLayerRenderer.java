package com.revolsys.swing.map.layer;

import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.swing.SwingWorkerManager;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.raster.GeoReferencedImage;
import com.revolsys.swing.map.layer.raster.GeoReferencedImageLayerRenderer;

public class TiledImageLayerRenderer extends
  AbstractLayerRenderer<AbstractTiledImageLayer> implements
  PropertyChangeListener {

  private final Map<MapTile, MapTile> cachedTiles = new HashMap<MapTile, MapTile>();

  private TileLoaderProcess tileLoaderProcess;

  private GeometryFactory geometryFactory;

  private double resolution;

  public TiledImageLayerRenderer(final AbstractTiledImageLayer layer) {
    super("tiledImage", layer);
    layer.addPropertyChangeListener(this);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object newValue = event.getNewValue();
    if (newValue instanceof BoundingBox) {
      final BoundingBox newBoundingBox = (BoundingBox)newValue;
      synchronized (cachedTiles) {
        final List<MapTile> mapTiles = new ArrayList<MapTile>(
          cachedTiles.keySet());
        final GeometryFactory newGeometryFactory = newBoundingBox.getGeometryFactory();
        for (final MapTile mapTile : mapTiles) {
          final BoundingBox boundingBox = mapTile.getBoundingBox();
          final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
          if (!geometryFactory.equals(newGeometryFactory)
            || !newBoundingBox.intersects(boundingBox)) {
            cachedTiles.remove(boundingBox);
          }
        }
      }
    } else if (!"loading".equals(event.getPropertyName())) {
      synchronized (cachedTiles) {
        cachedTiles.clear();
        if (tileLoaderProcess != null) {
          tileLoaderProcess.cancel(true);
        }
      }
    }
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics,
    final AbstractTiledImageLayer layer) {
    final GeometryFactory geometryFactory = viewport.getGeometryFactory();
    final double resolution = layer.getResolution(viewport);
    synchronized (cachedTiles) {
      if (resolution != this.resolution
        || geometryFactory != this.geometryFactory) {
        this.resolution = resolution;
        this.geometryFactory = geometryFactory;
        cachedTiles.clear();
      }
    }
    final List<MapTile> tilesToLoad = new ArrayList<MapTile>();
    final List<MapTile> mapTiles = layer.getOverlappingMapTiles(viewport);
    for (final MapTile mapTile : mapTiles) {
      if (mapTile != null) {
        MapTile cachedTile = null;
        synchronized (cachedTiles) {
          cachedTile = cachedTiles.get(mapTile);
          if (cachedTile == null) {
            cachedTile = mapTile;
            cachedTiles.put(cachedTile, cachedTile);
            tilesToLoad.add(cachedTile);
          }
        }
        final GeoReferencedImage image = cachedTile.getImage(geometryFactory);
        GeoReferencedImageLayerRenderer.render(viewport, graphics, image);
      }
    }
    if (!tilesToLoad.isEmpty()) {
      boolean scheduled = false;
      if (tileLoaderProcess != null) {
        scheduled = tileLoaderProcess.addMapTiles(resolution, geometryFactory,
          tilesToLoad);
        if (!scheduled) {
          tileLoaderProcess.cancel(true);
        }
      }
      if (!scheduled) {
        tileLoaderProcess = layer.createTileLoaderProcess();
        tileLoaderProcess.addMapTiles(resolution, geometryFactory, tilesToLoad);
        SwingWorkerManager.execute(tileLoaderProcess);
      }
    }
  }

  public void setLoaded() {
    getLayer().firePropertyChange("loading", false, true);
  }
}
