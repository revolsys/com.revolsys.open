package com.revolsys.swing.map.overlay;

import java.awt.Color;
import java.awt.Graphics2D;

import com.revolsys.awt.WebColors;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.record.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;

public class SelectedRecordsRenderer {
  private final GeometryStyle highlightStyle;

  public SelectedRecordsRenderer(final Color color) {
    final Color lineColor = WebColors.setAlpha(color, 150);
    final Color fillColor = WebColors.setAlpha(color, 50);

    this.highlightStyle = GeometryStyle.polygon(lineColor, 5, fillColor) //
      .setMarker("ellipse", 9, lineColor, 0, lineColor);
  }

  public void paintSelected(final Viewport2D viewport, final Graphics2D graphics,
    final GeometryFactory viewportGeometryFactory, Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      geometry = viewport.getGeometry(geometry);
      GeometryStyleRenderer.renderGeometry(viewport, graphics, geometry, this.highlightStyle);
    }
  }
}
