package com.revolsys.swing.map.layer.raster;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.util.Collections;
import java.util.Map;

import com.revolsys.awt.WebColors;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.io.BaseCloseable;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.record.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;

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
      image.drawImage(graphics, viewBoundingBox, viewWidth, viewHeight, useTransform);
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
    if (!boundingBox.getGeometryFactory().isSameCoordinateSystem(viewport.getGeometryFactory())) {
      GeometryStyleRenderer.renderOutline(viewport, graphics, boundingBox.toPolygon(0),
        STYLE_DIFFERENT_COORDINATE_SYSTEM);
    }
  }

  public GeoreferencedImageLayerRenderer(final GeoreferencedImageLayer layer) {
    super("raster", layer);
  }

  @Override
  public void render(final Viewport2D viewport, final GeoreferencedImageLayer layer) {
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
            renderAlpha(viewport, graphics, image, true, layer.getOpacity() / 255.0);
            renderDifferentCoordinateSystem(viewport, graphics, boundingBox);
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
