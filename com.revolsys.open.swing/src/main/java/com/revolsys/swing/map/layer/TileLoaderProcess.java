package com.revolsys.swing.map.layer;

import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.SwingWorker;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.swing.SwingWorkerManager;
import com.revolsys.swing.map.Viewport2D;

public class TileLoaderProcess extends SwingWorker<BufferedImage, Void> {
  private Viewport2D viewport;

  private TiledImageLayerRenderer renderer;

  private MapTile mapTile;

  private double scale;

  @Override
  protected BufferedImage doInBackground() throws Exception {
    final MapTile mapTile = getMapTile();
    try {
      final BufferedImage image = mapTile.loadImage();
      mapTile.setImage(image);
      renderer.setLoaded(viewport, mapTile);
      return image;
    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error("Unable to load " + mapTile, e);
      return null;
    }
  }

  @Override
  protected void done() {

  }

  public Image execute(final Viewport2D viewport, final double scale,
    final MapTile mapTile, final TiledImageLayerRenderer renderer) {
    this.viewport = viewport;
    this.scale = scale;
    this.mapTile = mapTile;
    this.renderer = renderer;
    SwingWorkerManager.execute(this);
    return null;
  }

  public BoundingBox getBoundingBox() {
    return mapTile.getBoundingBox();
  }

  public MapTile getMapTile() {
    return mapTile;
  }

  public TiledImageLayerRenderer getRenderer() {
    return renderer;
  }

  public double getScale() {
    return scale;
  }

  public Viewport2D getViewport() {
    return viewport;
  }

  @Override
  public String toString() {
    return "Loading map tile " + getMapTile();
  }
}
