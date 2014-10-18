package com.revolsys.swing.map.overlay;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.util.List;

import com.revolsys.awt.WebColors;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.record.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.record.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;

public class SelectedRecordsRenderer {

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

  private final MarkerStyle erroStyle;

  private final GeometryStyle highlightStyle;

  private final GeometryStyle outlineStyle;

  private final MarkerStyle vertexStyle;

  private final MarkerStyle lastVertexStyle;

  private final MarkerStyle firstVertexStyle;

  {
  }

  public SelectedRecordsRenderer(final Color outlineColor,
    final Color selectColor) {

    final Color selectColorTransparent = WebColors.setAlpha(selectColor, 127);
    final Color outlineColorTransparent = WebColors.setAlpha(outlineColor, 127);

    this.erroStyle = MarkerStyle.marker("ellipse", 7, WebColors.Yellow, 1,
      WebColors.Red);

    this.highlightStyle = GeometryStyle.polygon(selectColor, 3,
      selectColorTransparent);
    MarkerStyle.setMarker(this.highlightStyle, "ellipse", 6,
      outlineColorTransparent, 1, selectColorTransparent);

    this.outlineStyle = GeometryStyle.line(outlineColor);
    MarkerStyle.setMarker(this.outlineStyle, "ellipse", 6,
      outlineColorTransparent, 1, selectColorTransparent);

    this.vertexStyle = MarkerStyle.marker(vertexShape(), 9, outlineColor, 1,
      selectColor);
    this.vertexStyle.setMarkerOrientationType("auto");

    this.firstVertexStyle = MarkerStyle.marker(firstVertexShape(), 9,
      outlineColor, 1, selectColor);
    this.firstVertexStyle.setMarkerOrientationType("auto");
    this.firstVertexStyle.setMarkerPlacement("point(0)");
    this.firstVertexStyle.setMarkerHorizontalAlignment("center");

    this.lastVertexStyle = MarkerStyle.marker(lastVertexShape(), 9,
      outlineColor, 1, selectColor);
    this.lastVertexStyle.setMarkerOrientationType("auto");
    this.lastVertexStyle.setMarkerPlacement("point(n)");
    this.lastVertexStyle.setMarkerHorizontalAlignment("right");

  }

  public void paintSelected(final Viewport2D viewport,
    final GeometryFactory viewportGeometryFactory, final Geometry geometry) {
    final Graphics2D graphics = viewport.getGraphics();
    if (graphics != null) {
      paintSelected(viewport, graphics, viewportGeometryFactory, geometry);
    }
  }

  public void paintSelected(final Viewport2D viewport,
    final Graphics2D graphics, final GeometryFactory viewportGeometryFactory,
    Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      geometry = viewport.getGeometry(geometry);
      GeometryStyleRenderer.renderGeometry(viewport, graphics, geometry,
        this.highlightStyle);
      GeometryStyleRenderer.renderOutline(viewport, graphics, geometry,
        this.outlineStyle);

      if (!geometry.isEmpty()) {
        final List<LineString> lines = geometry.getGeometryComponents(LineString.class);
        for (final LineString line : lines) {
          MarkerStyleRenderer.renderMarkers(viewport, graphics, line,
            this.firstVertexStyle, this.lastVertexStyle, this.vertexStyle);
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
