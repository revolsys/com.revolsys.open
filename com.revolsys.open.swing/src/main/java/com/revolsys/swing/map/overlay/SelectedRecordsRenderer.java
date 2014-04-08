package com.revolsys.swing.map.overlay;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.util.List;

import org.jdesktop.swingx.color.ColorUtil;

import com.revolsys.awt.WebColors;
import com.revolsys.gis.jts.IsSimpleOp;
import com.revolsys.gis.jts.IsValidOp;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.map.layer.dataobject.style.MarkerStyle;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.operation.valid.TopologyValidationError;

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

    final Color selectColorTransparent = ColorUtil.setAlpha(selectColor, 127);
    final Color outlineColorTransparent = ColorUtil.setAlpha(outlineColor, 127);

    erroStyle = MarkerStyle.marker("ellipse", 7, WebColors.Yellow, 1,
      WebColors.Red);

    highlightStyle = GeometryStyle.polygon(selectColor, 3,
      selectColorTransparent);
    MarkerStyle.setMarker(highlightStyle, "ellipse", 6,
      outlineColorTransparent, 1, selectColorTransparent);

    outlineStyle = GeometryStyle.line(outlineColor);
    MarkerStyle.setMarker(outlineStyle, "ellipse", 6, outlineColorTransparent,
      1, selectColorTransparent);

    vertexStyle = MarkerStyle.marker(vertexShape(), 9, outlineColor, 1,
      selectColor);
    vertexStyle.setMarkerOrientationType("auto");

    firstVertexStyle = MarkerStyle.marker(firstVertexShape(), 9, outlineColor,
      1, selectColor);
    firstVertexStyle.setMarkerOrientationType("auto");
    firstVertexStyle.setMarkerPlacement("point(0)");
    firstVertexStyle.setMarkerHorizontalAlignment("center");

    lastVertexStyle = MarkerStyle.marker(lastVertexShape(), 9, outlineColor, 1,
      selectColor);
    lastVertexStyle.setMarkerOrientationType("auto");
    lastVertexStyle.setMarkerPlacement("point(n)");
    lastVertexStyle.setMarkerHorizontalAlignment("right");

  }

  public void paintSelected(final Viewport2D viewport,
    final com.revolsys.jts.geom.GeometryFactory viewportGeometryFactory, final Graphics2D graphics,
    Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      geometry = viewport.getGeometry(geometry);

      GeometryStyleRenderer.renderGeometry(viewport, graphics, geometry,
        highlightStyle);
      GeometryStyleRenderer.renderOutline(viewport, graphics, geometry,
        outlineStyle);

      if (!geometry.isEmpty()) {
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
          final Geometry part = geometry.getGeometryN(i);
          if (part instanceof LineString) {
            final LineString lineString = (LineString)part;
            final CoordinatesList points = CoordinatesListUtil.get(lineString);
            MarkerStyleRenderer.renderMarkers(viewport, graphics, points,
              firstVertexStyle, lastVertexStyle, vertexStyle);
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            final List<CoordinatesList> pointsList = CoordinatesListUtil.getAll(polygon);
            for (final CoordinatesList points : pointsList) {
              MarkerStyleRenderer.renderMarkers(viewport, graphics, points,
                firstVertexStyle, lastVertexStyle, vertexStyle);
            }
          }
        }
      }

      final IsValidOp validOp = new IsValidOp(geometry);
      if (validOp.isValid()) {
        final IsSimpleOp simpleOp = new IsSimpleOp(geometry);
        if (!simpleOp.isSimple()) {
          for (final Coordinates coordinates : simpleOp.getNonSimplePoints()) {
            final Point point = viewportGeometryFactory.createPoint(coordinates);
            MarkerStyleRenderer.renderMarker(viewport, graphics, point,
              erroStyle);
          }
        }
      } else {
        for (final TopologyValidationError error : validOp.getErrors()) {
          final Point point = viewportGeometryFactory.createPoint(error.getCoordinate());
          MarkerStyleRenderer.renderMarker(viewport, graphics, point, erroStyle);
        }
      }
    }
  }
}
