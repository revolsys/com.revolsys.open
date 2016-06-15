package com.revolsys.swing.map.overlay;

import java.awt.Color;
import java.awt.Graphics2D;

import com.revolsys.awt.WebColors;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.record.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.util.Property;

public class SelectedRecordsRenderer {
  private final GeometryStyle highlightStyle = GeometryStyle
    .polygon(WebColors.Lime, 5, WebColors.Lime) //
    .setMarker("ellipse", 5, WebColors.Lime, 3, WebColors.Black);

  private final GeometryStyle lineStyle = GeometryStyle.line(WebColors.Black);

  private final boolean opaque;

  public SelectedRecordsRenderer(final Color color, final boolean opaque) {
    this.opaque = opaque;
    setStyleColor(color);
  }

  public void paintSelected(final Viewport2D viewport, final Graphics2D graphics,
    final GeometryFactory viewportGeometryFactory, Geometry geometry) {
    geometry = viewport.getGeometry(geometry);
    if (Property.hasValue(geometry)) {
      GeometryStyleRenderer.renderGeometry(viewport, graphics, geometry, this.highlightStyle);
      if (!(geometry instanceof Punctual)) {
        GeometryStyleRenderer.renderGeometryOutline(viewport, graphics, geometry, this.lineStyle);
      }
    }
  }

  public void setStyleColor(final Color lineColor) {
    final Color fillColor;
    if (this.opaque) {
      fillColor = WebColors.newAlpha(lineColor, 50);
    } else {
      fillColor = lineColor;
    }
    this.highlightStyle.setLineColor(lineColor);
    this.highlightStyle.setPolygonFill(fillColor);
    this.highlightStyle.setMarkerLineColor(lineColor);
  }
}
