package com.revolsys.swing.map.overlay.record;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.util.List;

import com.revolsys.awt.WebColors;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.view.ViewRenderer;

public class SelectedRecordsVertexRenderer {

  public static GeneralPath firstVertexShape() {
    final GeneralPath path = new GeneralPath(new Ellipse2D.Double(0, 0, 11, 11));
    path.moveTo(5, 4);
    path.lineTo(6, 5);
    path.lineTo(5, 6);
    path.lineTo(4, 5);
    path.lineTo(5, 4);
    return path;
  }

  public static GeneralPath lastVertexShape() {
    final GeneralPath path = new GeneralPath();
    path.moveTo(0, 0);
    path.lineTo(10, 5);
    path.lineTo(0, 10);
    path.lineTo(0, 0);
    path.closePath();
    return path;
  }

  public static GeneralPath vertexShape() {
    final GeneralPath path = new GeneralPath();
    path.moveTo(5, 0);
    path.lineTo(10, 5);
    path.lineTo(5, 10);
    path.lineTo(0, 10);
    path.lineTo(0, 0);
    path.closePath();
    path.moveTo(5, 4);
    path.lineTo(6, 5);
    path.lineTo(5, 6);
    path.lineTo(4, 5);
    path.lineTo(5, 4);
    return path;
  }

  private final MarkerStyle firstVertexStyle;

  private final GeometryStyle highlightStyle;

  private final MarkerStyle lastVertexStyle;

  private final MarkerStyle vertexStyle;

  public SelectedRecordsVertexRenderer(final Color color, final boolean opaque) {
    final Color fillColor = color;

    this.highlightStyle = GeometryStyle.polygon(WebColors.Black, 1, fillColor) //
      .setMarker("ellipse", 9, WebColors.Black, 1, fillColor);

    final GeneralPath vertexShape = vertexShape();
    this.vertexStyle = MarkerStyle.marker(vertexShape, 9, WebColors.Black, 1, color);
    this.vertexStyle.setMarkerOrientationType("auto");

    final GeneralPath firstVertexShape = firstVertexShape();
    this.firstVertexStyle = MarkerStyle.marker(firstVertexShape, 9, WebColors.Black, 1, color);
    this.firstVertexStyle.setMarkerOrientationType("auto");
    this.firstVertexStyle.setMarkerPlacementType("vertex(0)");
    this.firstVertexStyle.setMarkerHorizontalAlignment("center");

    final GeneralPath lastVertexShape = lastVertexShape();
    this.lastVertexStyle = MarkerStyle.marker(lastVertexShape, 9, WebColors.Black, 1, color);
    this.lastVertexStyle.setMarkerOrientationType("auto");
    this.lastVertexStyle.setMarkerPlacementType("vertex(n)");
    this.lastVertexStyle.setMarkerHorizontalAlignment("right");
  }

  public void paintSelected(final ViewRenderer view, final Graphics2D graphics,
    final GeometryFactory viewportGeometryFactory, Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      geometry = view.getGeometry(geometry);

      view.drawGeometryOutline(geometry, this.highlightStyle);

      if (!geometry.isEmpty()) {
        final List<LineString> lines = geometry.getGeometryComponents(LineString.class);
        for (final LineString line : lines) {
          view.drawMarkers(line, this.firstVertexStyle, this.lastVertexStyle, this.vertexStyle);
        }
      }
    }
  }

  public void setStyleColor(final Color color) {
    this.highlightStyle.setFill(color);
    this.vertexStyle.setMarkerFill(color);
    this.lastVertexStyle.setMarkerFill(color);
    this.lastVertexStyle.setMarkerFill(color);
  }
}
