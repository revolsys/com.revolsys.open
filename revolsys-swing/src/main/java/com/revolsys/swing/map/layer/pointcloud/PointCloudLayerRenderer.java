package com.revolsys.swing.map.layer.pointcloud;

import org.jeometry.common.awt.WebColors;

import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.view.ViewRenderer;

public class PointCloudLayerRenderer extends AbstractLayerRenderer<PointCloudLayer> {

  private static final GeometryStyle STYLE_BOUNDING_BOX = GeometryStyle.line(WebColors.Green, 1);

  public PointCloudLayerRenderer(final PointCloudLayer layer) {
    super("raster", "Raster", null);
    setLayer(layer);
  }

  @Override
  public void render(final ViewRenderer view, final PointCloudLayer layer) {
    // TODO cancellable
    final double scaleForVisible = view.getScaleForVisible();
    if (layer.isVisible(scaleForVisible)) {
      if (!layer.isEditable()) {
        final PointCloud<?> pointCloud = layer.getPointCloud();
        if (pointCloud != null) {
          view.drawBboxOutline(STYLE_BOUNDING_BOX, layer);
        }
      }
    }
  }

  @Override
  public JsonObject toMap() {
    return JsonObject.EMPTY;
  }
}
