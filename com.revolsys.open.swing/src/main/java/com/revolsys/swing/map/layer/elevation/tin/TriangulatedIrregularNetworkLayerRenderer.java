package com.revolsys.swing.map.layer.elevation.tin;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;

public class TriangulatedIrregularNetworkLayerRenderer extends AbstractLayerRenderer<TriangulatedIrregularNetworkLayer> {

  private final GeometryStyle style = GeometryStyle.polygon(WebColors.Aqua,
    WebColors.newAlpha(WebColors.Aqua, 50));

  public TriangulatedIrregularNetworkLayerRenderer(final TriangulatedIrregularNetworkLayer layer) {
    super("raster", layer);
  }

  @Override
  public void render(final Viewport2D viewport, final TriangulatedIrregularNetworkLayer layer) {
    final double scaleForVisible = viewport.getScaleForVisible();
    if (layer.isVisible(scaleForVisible)) {
      if (!layer.isEditable()) {
        final TriangulatedIrregularNetwork tin = layer.getTin();
        if (tin != null) {
          for (final Triangle triangle : tin.getTriangles(viewport.getBoundingBox())) {
            viewport.drawGeometry(triangle, this.style);
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
