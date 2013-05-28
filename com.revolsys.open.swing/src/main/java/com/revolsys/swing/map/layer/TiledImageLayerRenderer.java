package com.revolsys.swing.map.layer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.raster.GeoReferencedImageLayerRenderer;
import com.vividsolutions.jts.geom.Point;

public class TiledImageLayerRenderer extends
  AbstractLayerRenderer<AbstractTiledImageLayer> implements
  PropertyChangeListener {

  private final Map<MapTile, MapTile> cachedTiles = new HashMap<MapTile, MapTile>();

  private final Map<MapTile, TileLoaderProcess> imageLoading = new HashMap<MapTile, TileLoaderProcess>();

  private double scale = -1;

  public TiledImageLayerRenderer(final AbstractTiledImageLayer layer) {
    super("tiledImage", layer);
    layer.addPropertyChangeListener(this);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    final Object newValue = evt.getNewValue();
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

      synchronized (imageLoading) {
        final List<MapTile> mapTiles = new ArrayList<MapTile>(
          imageLoading.keySet());
        final GeometryFactory newGeometryFactory = newBoundingBox.getGeometryFactory();
        for (final MapTile mapTile : mapTiles) {
          final BoundingBox boundingBox = mapTile.getBoundingBox();
          final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
          if (!geometryFactory.equals(newGeometryFactory)
            || !newBoundingBox.intersects(boundingBox)) {
            final TileLoaderProcess process = imageLoading.remove(boundingBox);
            process.cancel(true);
          }
        }
      }
    } else if (!"loading".equals(evt.getPropertyName())) {
      synchronized (cachedTiles) {
        cachedTiles.clear();
      }
      synchronized (imageLoading) {
        final ArrayList<TileLoaderProcess> processes = new ArrayList<TileLoaderProcess>(
          imageLoading.values());
        for (final TileLoaderProcess process : processes) {
          process.cancel(true);
        }
        imageLoading.clear();
      }

    }
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics,
    final AbstractTiledImageLayer layer) {
    synchronized (cachedTiles) {
      final double viewportScale = viewport.getScale();
      if (viewportScale != scale) {
        cachedTiles.clear();
        this.scale = viewportScale;
      }
    }
    for (final MapTile mapTile : layer.getOverlappingEnvelopes(viewport)) {
      if (mapTile != null) {
        MapTile cachedTile = null;

        synchronized (imageLoading) {
          cachedTile = cachedTiles.get(mapTile);
          if (cachedTile == null) {
            cachedTile = mapTile;
            cachedTiles.put(cachedTile, cachedTile);
            final TileLoaderProcess process = layer.getTileLoaderProcess();
            imageLoading.put(cachedTile, process);
            process.execute(viewport, scale, cachedTile, this);

          }
        }
        GeoReferencedImageLayerRenderer.render(viewport, graphics, cachedTile);
      }
    }
  }

  public void setLoaded(final Viewport2D viewport, final MapTile mapTile) {
    synchronized (imageLoading) {
      imageLoading.remove(mapTile);
    }

    BoundingBox imageBoundingBox = mapTile.getBoundingBox();
    GeometryFactory imageGeometryFactory = imageBoundingBox.getGeometryFactory();
    GeometryFactory viewGeometryFactory = viewport.getGeometryFactory();
    int imageSrid = imageGeometryFactory.getSRID();
    if (imageSrid > 0 && imageSrid != viewGeometryFactory.getSRID()) {
      BufferedImage image = mapTile.getImage();
      double minX = imageBoundingBox.getMinX();
      double minY = imageBoundingBox.getMinY();
      int imageWidth = mapTile.getImageWidth();
      int imageHeight = mapTile.getImageHeight();
      double width = imageBoundingBox.getWidth();
      double height = imageBoundingBox.getHeight();
      double pixelWidth = width / imageWidth;
      double pixelHeight = height / imageHeight;

      BoundingBox newImageBoundingBox = imageBoundingBox.convert(viewGeometryFactory);

      double newMinX = newImageBoundingBox.getMinX();
      double newMaxX = newImageBoundingBox.getMaxX();
      double newMinY = newImageBoundingBox.getMinY();
      double newMaxY = newImageBoundingBox.getMaxY();
      double newPixelSize = viewport.getModelUnitsPerViewUnit();
      int newImageWidth = (int)((newMaxX - newMinX) / newPixelSize);
      int newImageHeight = (int)((newMaxY - newMinY) / newPixelSize);

      final BufferedImage newImage = new BufferedImage(newImageWidth,
        newImageHeight, BufferedImage.TYPE_INT_ARGB);
      for (int i = 0; i < newImageWidth; i++) {
        double newImageX = newMinX + i * newPixelSize;
        for (int j = 0; j < newImageHeight; j++) {
          double newImageY = newMaxY - j * newPixelSize;
          Point newImagePoint = viewGeometryFactory.createPoint(newImageX,
            newImageY);
          Point imagePoint = imageGeometryFactory.copy(newImagePoint);
          double imageX = imagePoint.getX();
          double imageY = imagePoint.getY();
          int imageI = (int)((imageX - minX) / pixelWidth);
          int imageJ = imageHeight - (int)((imageY - minY) / pixelHeight);
          if (imageI > -1 && imageI < imageWidth) {
            if (imageJ > -1 && imageJ < imageHeight) {
              int rgb = image.getRGB(imageI, imageJ);
              if (rgb != -1) {
                // TODO better interpolation
                newImage.setRGB(i, j, rgb);
              }
            }
          }
        }
      }

      MapTile viewMapTile = new MapTile(newImageBoundingBox, newImageWidth,
        newImageHeight);
      viewMapTile.setImage(newImage);
      cachedTiles.put(mapTile, viewMapTile);
    }
    AbstractTiledImageLayer layer = getLayer();
    PropertyChangeSupport propertyChangeSupport = layer.getPropertyChangeSupport();
    propertyChangeSupport.firePropertyChange("loading", false, true);
    viewport.update();
  }
}
