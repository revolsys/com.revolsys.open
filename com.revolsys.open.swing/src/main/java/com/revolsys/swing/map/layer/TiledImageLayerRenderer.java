package com.revolsys.swing.map.layer;

import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.raster.GeoReferencedImage;
import com.revolsys.swing.map.layer.raster.GeoReferencedImageLayerRenderer;
import com.revolsys.swing.parallel.Invoke;

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
      synchronized (this.cachedTiles) {
        final List<MapTile> mapTiles = new ArrayList<MapTile>(
          this.cachedTiles.keySet());
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
    } else if (!"loading".equals(event.getPropertyName())) {
      synchronized (this.cachedTiles) {
        this.cachedTiles.clear();
        if (this.tileLoaderProcess != null) {
          this.tileLoaderProcess.cancel(true);
        }
      }
    }
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics,
    final AbstractTiledImageLayer layer) {
    final GeometryFactory geometryFactory = viewport.getGeometryFactory();
    final double resolution = layer.getResolution(viewport);
    synchronized (this.cachedTiles) {
      if (resolution != this.resolution
        || geometryFactory != this.geometryFactory) {
        this.resolution = resolution;
        this.geometryFactory = geometryFactory;
        this.cachedTiles.clear();
      }
    }
    final List<MapTile> tilesToLoad = new ArrayList<MapTile>();
    final List<MapTile> mapTiles = layer.getOverlappingMapTiles(viewport);
    for (final MapTile mapTile : mapTiles) {
      if (mapTile != null) {
        MapTile cachedTile = null;
        synchronized (this.cachedTiles) {
          cachedTile = this.cachedTiles.get(mapTile);
          if (cachedTile == null) {
            cachedTile = mapTile;
            this.cachedTiles.put(cachedTile, cachedTile);
            tilesToLoad.add(cachedTile);
          }
        }
        final GeoReferencedImage image = cachedTile.getImage(geometryFactory);
        GeoReferencedImageLayerRenderer.render(viewport, graphics, image);
      }
    }
    if (!tilesToLoad.isEmpty()) {
      boolean scheduled = false;
      if (this.tileLoaderProcess != null) {
        scheduled = this.tileLoaderProcess.addMapTiles(resolution,
          geometryFactory, tilesToLoad);
        if (!scheduled) {
          this.tileLoaderProcess.cancel(true);
        }
      }
      if (!scheduled) {
        this.tileLoaderProcess = layer.createTileLoaderProcess();
        this.tileLoaderProcess.addMapTiles(resolution, geometryFactory,
          tilesToLoad);
        Invoke.worker(this.tileLoaderProcess);
      }
    }
  }

  public void setLoaded() {
    getLayer().firePropertyChange("loading", false, true);
  }

  @Override
  public Map<String, Object> toMap() {
    return Collections.emptyMap();
  }
}
