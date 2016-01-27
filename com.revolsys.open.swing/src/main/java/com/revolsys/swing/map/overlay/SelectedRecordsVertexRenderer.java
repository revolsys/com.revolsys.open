package com.revolsys.swing.map.overlay;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.util.List;

import com.revolsys.awt.WebColors;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.record.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;

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

  private final MarkerStyle errorStyle = MarkerStyle.marker("ellipse", 7, WebColors.Yellow, 1,
    WebColors.Red);

  private final MarkerStyle firstVertexStyle;

  private final GeometryStyle highlightStyle;

  private final MarkerStyle lastVertexStyle;

  private final MarkerStyle vertexStyle;

  public SelectedRecordsVertexRenderer(final Color color) {
    final Color fillColor = WebColors.setAlpha(color, 75);

    this.highlightStyle = GeometryStyle.polygon(WebColors.Black, 1, fillColor) //
      .setMarker("ellipse", 9, WebColors.Black, 1, fillColor);

    this.vertexStyle = MarkerStyle.marker(vertexShape(), 9, WebColors.Black, 1, color);
    this.vertexStyle.setMarkerOrientationType("auto");

    this.firstVertexStyle = MarkerStyle.marker(firstVertexShape(), 9, WebColors.Black, 1, color);
    this.firstVertexStyle.setMarkerOrientationType("auto");
    this.firstVertexStyle.setMarkerPlacementType("point(0)");
    this.firstVertexStyle.setMarkerHorizontalAlignment("center");

    this.lastVertexStyle = MarkerStyle.marker(lastVertexShape(), 9, WebColors.Black, 1, color);
    this.lastVertexStyle.setMarkerOrientationType("auto");
    this.lastVertexStyle.setMarkerPlacementType("point(n)");
    this.lastVertexStyle.setMarkerHorizontalAlignment("right");
  }

  public void paintSelected(final Viewport2D viewport, final Graphics2D graphics,
    final GeometryFactory viewportGeometryFactory, Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      geometry = viewport.getGeometry(geometry);

      viewport.drawGeometryOutline(geometry, this.highlightStyle);

      if (!geometry.isEmpty()) {
        final List<LineString> lines = geometry.getGeometryComponents(LineString.class);
        for (final LineString line : lines) {
          MarkerStyleRenderer.renderMarkers(viewport, graphics, line, this.firstVertexStyle,
            this.lastVertexStyle, this.vertexStyle);
        }
      }

      // if (geometry.getVertexCount() < 100) {
      // try {
      // final IsValidOp validOp = new IsValidOp(geometry, false);
      // if (validOp.isValid()) {
      // final IsSimpleOp simpleOp = new IsSimpleOp(geometry, false);
      // if (!simpleOp.isSimple()) {
      // for (final Point coordinates : simpleOp.getNonSimplePoints()) {
      // final Point point = viewportGeometryFactory.point(coordinates);
      // MarkerStyleRenderer.renderMarker(viewport, graphics, point,
      // this.erroStyle);
      // }
      // }
      // } else {
      // for (final GeometryValidationError error : validOp.getErrors()) {
      // final Point point =
      // viewportGeometryFactory.point(error.getErrorPoint());
      // MarkerStyleRenderer.renderMarker(viewport, graphics, point,
      // this.erroStyle);
      // }
      // }
      // } catch (final Throwable e) {
      // }
      // }
    }
  }
}
