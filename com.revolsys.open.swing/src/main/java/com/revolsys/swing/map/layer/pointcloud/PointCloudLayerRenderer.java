package com.revolsys.swing.map.layer.pointcloud;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.io.BaseCloseable;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;

public class PointCloudLayerRenderer extends AbstractLayerRenderer<PointCloudLayer> {

  private static final GeometryStyle STYLE_BOUNDING_BOX = GeometryStyle.line(WebColors.Green, 1);

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
    if (!boundingBox.isSameCoordinateSystem(viewport)) {
      try (
        BaseCloseable transformCloseable = viewport.setUseModelCoordinates(true)) {
        final Polygon polygon = boundingBox.toPolygon(0);
        viewport.drawGeometryOutline(polygon, STYLE_BOUNDING_BOX);
      }
    }
  }

  public PointCloudLayerRenderer(final PointCloudLayer layer) {
    super("raster", layer);
  }

  @Override
  public void render(final Viewport2D viewport, final PointCloudLayer layer) {
    final double scaleForVisible = viewport.getScaleForVisible();
    if (layer.isVisible(scaleForVisible)) {
      if (!layer.isEditable()) {
        final PointCloud<?> pointCloud = layer.getPointCloud();
        if (pointCloud != null) {
          final BoundingBox boundingBox = layer.getBoundingBox();
          final Graphics2D graphics = viewport.getGraphics();
          if (graphics != null) {
            try (
              BaseCloseable transformCloseable = viewport.setUseModelCoordinates(true)) {
              final Polygon polygon = boundingBox.toPolygon(0);
              viewport.drawGeometryOutline(polygon, STYLE_BOUNDING_BOX);
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
