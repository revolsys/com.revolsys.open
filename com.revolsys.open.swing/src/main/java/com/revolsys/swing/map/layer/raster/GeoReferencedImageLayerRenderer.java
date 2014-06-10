package com.revolsys.swing.map.layer.raster;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Map;

import javax.media.jai.PlanarImage;

import com.revolsys.awt.WebColors;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;

public class GeoReferencedImageLayerRenderer extends
  AbstractLayerRenderer<GeoReferencedImageLayer> {

  public static void render(final Viewport2D viewport,
    final Graphics2D graphics, final GeoReferencedImage geoReferencedImage,
    final BoundingBox boundingBox, final boolean useTransform) {
    if (geoReferencedImage != null) {
      final PlanarImage jaiImage = geoReferencedImage.getJaiImage();
      if (geoReferencedImage != null) {
        if (jaiImage != null) {
          final int imageWidth = geoReferencedImage.getImageWidth();
          final int imageHeight = geoReferencedImage.getImageHeight();
          if (imageWidth != -1 && imageHeight != -1) {
            if (boundingBox != null && !boundingBox.isEmpty()) {
              final Point point = boundingBox.getTopLeftPoint();
              final double minX = point.getX();
              final double maxY = point.getY();

              final Composite composite = graphics.getComposite();
              final AffineTransform graphicsTransform = graphics.getTransform();
              try {
                final double[] location = viewport.toViewCoordinates(minX, maxY);
                final double screenX = location[0];
                final double screenY = location[1];
                final double imageScreenWidth = Math.ceil(Viewport2D.toDisplayValue(
                  viewport, boundingBox.getWidthLength()));
                final double imageScreenHeight = Math.ceil(Viewport2D.toDisplayValue(
                  viewport, boundingBox.getHeightLength()));
                graphics.setColor(WebColors.LimeGreen);
                graphics.drawRect((int)screenX, (int)screenY,
                  (int)imageScreenWidth, (int)imageScreenHeight);
                if (imageScreenWidth > 0 && imageScreenHeight > 0) {
                  graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                  if (imageScreenWidth > 0 && imageScreenHeight > 0) {

                    final double scaleX = imageScreenWidth / imageWidth;
                    final double scaleY = imageScreenHeight / imageHeight;

                    final AffineTransform imageTransform = new AffineTransform(
                      scaleX, 0, 0, scaleY, 0, 0);
                    if (useTransform) {
                      final AffineTransform geoTransform = geoReferencedImage.getAffineTransformation(boundingBox);
                      imageTransform.concatenate(geoTransform);
                    }

                    graphics.translate(screenX, screenY);
                    graphics.setComposite(AlphaComposite.SrcOver.derive(0.5f));
                    try {
                      graphics.drawRenderedImage(jaiImage, imageTransform);
                    } catch (final Throwable e) {
                    }
                  }
                }
              } catch (final NegativeArraySizeException e) {
              } catch (final OutOfMemoryError e) {
              } finally {
                graphics.setComposite(composite);
                graphics.setTransform(graphicsTransform);
              }
            }
          }
        }
      }
    }
  }

  public static void render(final Viewport2D viewport,
    final Graphics2D graphics, final GeoReferencedImage geoReferencedImage,
    final BufferedImage image, final BoundingBox boundingBox) {
    if (geoReferencedImage != null) {
      if (image != null) {
        final int imageWidth = geoReferencedImage.getImageWidth();
        final int imageHeight = geoReferencedImage.getImageHeight();
        if (imageWidth != -1 && imageHeight != -1) {
          if (boundingBox != null && !boundingBox.isEmpty()) {
            final Point point = boundingBox.getTopLeftPoint();
            final double minX = point.getX();
            final double maxY = point.getY();

            final AffineTransform transform = graphics.getTransform();
            try {
              final double[] location = viewport.toViewCoordinates(minX, maxY);
              final double screenX = location[0];
              final double screenY = location[1];
              graphics.translate(screenX, screenY);
              final int imageScreenWidth = (int)Math.ceil(Viewport2D.toDisplayValue(
                viewport, boundingBox.getWidthLength()));
              final int imageScreenHeight = (int)Math.ceil(Viewport2D.toDisplayValue(
                viewport, boundingBox.getHeightLength()));
              if (imageScreenWidth > 0 && imageScreenHeight > 0) {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                  RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                if (imageScreenWidth > 0 && imageScreenHeight > 0) {
                  graphics.drawImage(image, 0, 0, imageScreenWidth,
                    imageScreenHeight, null);
                }
              }
            } catch (final NegativeArraySizeException e) {
            } catch (final OutOfMemoryError e) {
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
        if (image != null) {
          BoundingBox boundingBox = layer.getBoundingBox();
          if (boundingBox == null || boundingBox.isEmpty()) {
            boundingBox = layer.fitToViewport();
          }
          if (viewport.getBoundingBox().intersects(boundingBox)) {
            final GeometryFactory viewGeometryFactory = viewport.getGeometryFactory();
            // final GeoReferencedImage convertedImage = image.getImage(
            // viewGeometryFactory.getCoordinateSystem(),
            // image.getResolution());
            // TODO projection (it's slow and takes lots of memory
            final BoundingBox convertedBoundingBox = boundingBox.convert(viewGeometryFactory);
            render(viewport, graphics, image, convertedBoundingBox, true);
          }
        }

      }
    }
  }

  @Override
  public Map<String, Object> toMap() {
    return Collections.emptyMap();
  }
}
