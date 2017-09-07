package com.revolsys.swing.map.layer.pointcloud;

import java.awt.Graphics2D;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.io.BaseCloseable;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.util.Cancellable;

public class PointCloudLayerRenderer extends AbstractLayerRenderer<PointCloudLayer> {

  private static final GeometryStyle STYLE_BOUNDING_BOX = GeometryStyle.line(WebColors.Green, 1);

  public PointCloudLayerRenderer(final PointCloudLayer layer) {
    super("raster", layer);
  }

  @Override
  public void render(final Viewport2D viewport, final Cancellable cancellable,
    final PointCloudLayer layer) {
    // TODO cancellable
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
