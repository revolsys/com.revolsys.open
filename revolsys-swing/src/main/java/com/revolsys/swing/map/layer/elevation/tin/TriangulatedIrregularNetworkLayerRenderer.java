package com.revolsys.swing.map.layer.elevation.tin;

import org.jeometry.common.awt.WebColors;

import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.view.ViewRenderer;

public class TriangulatedIrregularNetworkLayerRenderer
  extends AbstractLayerRenderer<TriangulatedIrregularNetworkLayer> {

  private final GeometryStyle style = GeometryStyle.polygon(WebColors.DarkBlue,
    WebColors.newAlpha(WebColors.Aqua, 50));

  public TriangulatedIrregularNetworkLayerRenderer(final TriangulatedIrregularNetworkLayer layer) {
    super("raster", "Raster", null);
    setLayer(layer);
  }

  @Override
  public void render(final ViewRenderer view, final TriangulatedIrregularNetworkLayer layer) {
    final double scaleForVisible = view.getScaleForVisible();
    if (layer.isVisible(scaleForVisible)) {
      if (!layer.isEditable()) {
        final TriangulatedIrregularNetwork tin = layer.getTin();
        if (tin != null) {
          for (final Triangle triangle : view
            .cancellable(tin.getTriangles(view.getBoundingBox()))) {
            final Geometry convertedTriangle = tin.convertGeometry(triangle);
            view.drawGeometry(convertedTriangle, this.style);
          }
        }
      }
    }
  }

  @Override
  public JsonObject toMap() {
    return JsonObject.EMPTY;
  }
}
