package com.revolsys.swing.map.layer;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.swing.parallel.AbstractSwingWorker;

public class TileLoaderProcess extends AbstractSwingWorker<Void, Void> {
  private final TiledImageLayerRenderer renderer;

  private MapTile mapTile;

  private boolean running = true;

  private final AbstractTiledImageLayer layer;

  private final Queue<MapTile> mapTiles = new LinkedList<MapTile>();

  private GeometryFactory geometryFactory;

  private double resolution = -1;

  public TileLoaderProcess(final AbstractTiledImageLayer layer) {
    this.layer = layer;
    this.renderer = layer.getRenderer();
  }

  public synchronized boolean addMapTiles(final double resolution,
    final GeometryFactory geometryFactory, final List<MapTile> mapTiles) {
    if (this.running) {
      if (this.resolution < 0 || resolution == this.resolution) {
        if (this.geometryFactory == null
          || this.geometryFactory == geometryFactory) {
          this.mapTiles.addAll(mapTiles);
          this.resolution = resolution;
          this.geometryFactory = geometryFactory;
        }
      }
    }
    return this.running;
  }

  @Override
  protected Void doInBackground() throws Exception {
    do {
      this.mapTile = getNextMapTile();
      if (this.mapTile != null) {
        try {
          this.mapTile.loadImage(this.geometryFactory);
        } catch (final Throwable e) {
          LoggerFactory.getLogger(getClass()).error(
            "Unable to load " + this.mapTile, e);
        }
      }
    } while (!isCancelled() && this.mapTile != null);
    if (!isCancelled()) {
      this.renderer.setLoaded();
    }
    return null;
  }

  private synchronized MapTile getNextMapTile() {
    final MapTile mapTile = this.mapTiles.poll();
    if (mapTile == null) {
      this.running = false;
    }
    return mapTile;
  }

  @Override
  public String toString() {
    return "Loading map tiles for " + this.layer;
  }

  @Override
  protected void uiTask() {

  }
}
