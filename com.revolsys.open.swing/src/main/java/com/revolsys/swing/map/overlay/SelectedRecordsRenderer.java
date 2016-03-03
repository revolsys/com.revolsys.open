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

public class SelectedRecordsRenderer {
  private final GeometryStyle highlightStyle;

  private final GeometryStyle lineStyle = GeometryStyle.line(WebColors.Black);

  public SelectedRecordsRenderer(final Color color, final Color markerLineColor,
    final boolean opaque) {
    final Color lineColor = color;
    Color fillColor = color;
    final int lineWidth = 5;
    if (!opaque) {
      fillColor = WebColors.setAlpha(fillColor, 50);
    }
    this.highlightStyle = GeometryStyle.polygon(lineColor, lineWidth, fillColor) //
      .setMarker("ellipse", 5, lineColor, 3, WebColors.Black);
  }

  public void paintSelected(final Viewport2D viewport, final Graphics2D graphics,
    final GeometryFactory viewportGeometryFactory, Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      geometry = viewport.getGeometry(geometry);
      GeometryStyleRenderer.renderGeometry(viewport, graphics, geometry, this.highlightStyle);
      if (!(geometry instanceof Punctual)) {
        GeometryStyleRenderer.renderGeometryOutline(viewport, graphics, geometry, this.lineStyle);
      }
    }
  }
}
