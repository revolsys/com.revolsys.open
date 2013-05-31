package com.revolsys.swing.map.layer;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.SwingWorker;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.cs.GeometryFactory;

public class TileLoaderProcess extends SwingWorker<Void, Void> {
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
    if (running) {
      if (this.resolution < 0 || resolution == this.resolution) {
        if (this.geometryFactory == null
          || this.geometryFactory == geometryFactory) {
          this.mapTiles.addAll(mapTiles);
          this.resolution = resolution;
          this.geometryFactory = geometryFactory;
        }
      }
    }
    return running;
  }

  @Override
  protected Void doInBackground() throws Exception {
    do {
      mapTile = getNextMapTile();
      if (mapTile != null) {
        try {
          mapTile.loadImage(geometryFactory);
        } catch (final Throwable e) {
          LoggerFactory.getLogger(getClass()).error(
            "Unable to load " + mapTile, e);
        }
      }
    } while (!isCancelled() && mapTile != null);
    if (!isCancelled()) {
      renderer.setLoaded();
    }
    return null;
  }

  @Override
  protected void done() {

  }

  private synchronized MapTile getNextMapTile() {
    final MapTile mapTile = mapTiles.poll();
    if (mapTile == null) {
      running = false;
    }
    return mapTile;
  }

  @Override
  public String toString() {
    return "Loading map tiles for " + this.layer;
  }
}
