package com.revolsys.swing.map.layer.raster;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.io.BaseCloseable;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.util.Cancellable;

public class GeoreferencedImageLayerRenderer
  extends AbstractLayerRenderer<GeoreferencedImageLayer> {

  private static final GeometryStyle STYLE_DIFFERENT_COORDINATE_SYSTEM = GeometryStyle
    .line(WebColors.Red, 4);

  public static void render(final Viewport2D viewport, final Graphics2D graphics,
    final GeoreferencedImage image, final boolean useTransform) {
    if (image != null) {
      final BoundingBox viewBoundingBox = viewport.getBoundingBox();
      final int viewWidth = viewport.getViewWidthPixels();
      final int viewHeight = viewport.getViewHeightPixels();
      image.drawImage(graphics, viewBoundingBox, viewWidth, viewHeight, useTransform,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }
  }

  public static void renderAlpha(final Viewport2D viewport, final Graphics2D graphics,
    final GeoreferencedImage image, final boolean useTransform, final double alpha) {
    final Composite composite = graphics.getComposite();
    try (
      BaseCloseable transformCloseable = viewport.setUseModelCoordinates(graphics, false)) {
      AlphaComposite alphaComposite = AlphaComposite.SrcOver;
      if (alpha < 1) {
        alphaComposite = alphaComposite.derive((float)alpha);
      }
      graphics.setComposite(alphaComposite);
      render(viewport, graphics, image, useTransform);
    } finally {
      graphics.setComposite(composite);
    }
  }

  public static void renderDifferentCoordinateSystem(final Viewport2D viewport,
    final Graphics2D graphics, final BoundingBox boundingBox) {
    if (!boundingBox.isSameCoordinateSystem(viewport)) {
      try (
        BaseCloseable transformCloseable = viewport.setUseModelCoordinates(true)) {
        final Polygon polygon = boundingBox.toPolygon(0);
        viewport.drawGeometryOutline(polygon, STYLE_DIFFERENT_COORDINATE_SYSTEM);
      }
    }
  }

  public GeoreferencedImageLayerRenderer(final GeoreferencedImageLayer layer) {
    super("raster", layer);
  }

  @Override
  public void render(final Viewport2D viewport, final Cancellable cancellable,
    final GeoreferencedImageLayer layer) {
    final double scaleForVisible = viewport.getScaleForVisible();
    if (layer.isVisible(scaleForVisible)) {
      if (!layer.isEditable()) {
        final GeoreferencedImage image = layer.getImage();
        if (image != null) {
          BoundingBox boundingBox = layer.getBoundingBox();
          if (boundingBox == null || boundingBox.isEmpty()) {
            boundingBox = layer.fitToViewport();
          }
          final Graphics2D graphics = viewport.getGraphics();
          if (graphics != null) {
            if (!cancellable.isCancelled()) {
              renderAlpha(viewport, graphics, image, true, layer.getOpacity() / 255.0);
            }
            if (!cancellable.isCancelled()) {
              renderDifferentCoordinateSystem(viewport, graphics, boundingBox);
            }
          }
        }
      }
    }
  }

  @Override
  public MapEx toMap() {
    return MapEx.EMPTY;
  }
}
