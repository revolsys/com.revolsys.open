package com.revolsys.swing.map.layer.raster;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import com.revolsys.awt.WebColors;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.vividsolutions.jts.geom.Point;

public class GeoReferencedImageLayerRenderer extends
  AbstractLayerRenderer<GeoReferencedImageLayer> {
  public static void render(final Viewport2D viewport,
    final Graphics2D graphics, final GeoReferencedImage geoReferencedImage) {
    if (geoReferencedImage != null) {
      final BoundingBox boundingBox = geoReferencedImage.getBoundingBox();
      render(viewport, graphics, geoReferencedImage, boundingBox);
    }
  }

  public static void render(final Viewport2D viewport,
    final Graphics2D graphics, final GeoReferencedImage geoReferencedImage,
    final BoundingBox boundingBox) {
    if (geoReferencedImage != null) {
      final BufferedImage image = geoReferencedImage.getImage();
      render(viewport, graphics, geoReferencedImage, image, boundingBox);
    }
  }

  public static void render(final Viewport2D viewport,
    final Graphics2D graphics, final GeoReferencedImage geoReferencedImage,
    BufferedImage image, final BoundingBox boundingBox) {
    if (geoReferencedImage != null) {
      if (image != null) {
        final int imageWidth = geoReferencedImage.getImageWidth();
        final int imageHeight = geoReferencedImage.getImageHeight();
        if (imageWidth != -1 && imageHeight != -1) {
          if (boundingBox != null && !boundingBox.isNull()) {
            final Point point = boundingBox.getTopLeftPoint();
            final double minX = point.getX();
            final double maxY = point.getY();

            final AffineTransform transform = graphics.getTransform();
            try {
              final double[] location = viewport.toViewCoordinates(minX, maxY);
              final double screenX = location[0];
              final double screenY = location[1];
              graphics.translate(screenX, screenY);
              final double imageScreenWidth = viewport.toDisplayValue(boundingBox.getWidthLength());
              final double imageScreenHeight = viewport.toDisplayValue(boundingBox.getHeightLength());

              final double xScaleFactor = imageScreenWidth / imageWidth;
              final double yScaleFactor = imageScreenHeight / imageHeight;
              graphics.scale(xScaleFactor, yScaleFactor);
              graphics.drawImage(image, 0, 0, null);
            } finally {
              graphics.setTransform(transform);
            }
          }
        }
      }
    }
  }

  public GeoReferencedImageLayerRenderer(final GeoReferencedImageLayer layer) {
    super("raster", layer);
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics,
    final GeoReferencedImageLayer layer) {
    final double scale = viewport.getScale();
    if (layer.isVisible(scale)) {
      if (!layer.isEditable()) {
        final GeoReferencedImage image = layer.getImage();
        final BoundingBox boundingBox = layer.getBoundingBox();
        if (boundingBox == null || boundingBox.isNull()) {
          layer.fitToViewport();
        }

        render(viewport, graphics, image);
      }
    }
  }
}
