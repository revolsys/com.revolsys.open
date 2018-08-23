package com.revolsys.swing.map.layer.pointcloud;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.view.ViewRenderer;

public class PointCloudLayerRenderer extends AbstractLayerRenderer<PointCloudLayer> {

  private static final GeometryStyle STYLE_BOUNDING_BOX = GeometryStyle.line(WebColors.Green, 1);

  public PointCloudLayerRenderer(final PointCloudLayer layer) {
    super("raster", layer);
  }

  @Override
  public void render(final ViewRenderer viewport, final PointCloudLayer layer) {
    // TODO cancellable
    final double scaleForVisible = viewport.getScaleForVisible();
    if (layer.isVisible(scaleForVisible)) {
      if (!layer.isEditable()) {
        final PointCloud<?> pointCloud = layer.getPointCloud();
        if (pointCloud != null) {
          final BoundingBox boundingBox = layer.getBoundingBox();
          final Polygon polygon = boundingBox.toPolygon(0);
          viewport.drawGeometryOutline(polygon, STYLE_BOUNDING_BOX);
        }
      }
    }
  }

  @Override
  public MapEx toMap() {
    return MapEx.EMPTY;
  }
}
