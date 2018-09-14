package com.revolsys.swing.map.overlay.record;

import java.awt.Color;

import com.revolsys.awt.WebColors;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.util.Property;

public class SelectedRecordsRenderer {
  private final GeometryStyle highlightStyle = GeometryStyle
    .polygon(WebColors.Lime, 5, WebColors.Lime) //
    .setMarker("ellipse", 5, WebColors.Lime, 3, WebColors.Black);

  private final GeometryStyle lineStyle = GeometryStyle.line(WebColors.Black);

  private final int alpha;

  public SelectedRecordsRenderer(final Color color, final int alpha) {
    this.alpha = alpha;
    setStyleColor(color);
  }

  public void paintSelected(final ViewRenderer view, final GeometryFactory viewportGeometryFactory,
    Geometry geometry) {
    geometry = view.getGeometry(geometry);
    if (Property.hasValue(geometry)) {
      view.drawGeometry(geometry, this.highlightStyle);
      if (!(geometry instanceof Punctual)) {
        view.drawGeometryOutline(geometry, this.lineStyle);
      }
    }
  }

  public void setStyleColor(final Color lineColor) {
    final Color fillColor = WebColors.newAlpha(lineColor, this.alpha);
    this.highlightStyle.setLineColor(lineColor);
    this.highlightStyle.setPolygonFill(fillColor);
    this.highlightStyle.setMarkerLineColor(lineColor);
  }
}
