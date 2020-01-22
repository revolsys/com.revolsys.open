package com.revolsys.swing.map.overlay.record;

import java.awt.Color;

import org.jeometry.common.awt.WebColors;
import org.jeometry.common.function.BiFunctionDouble;
import org.jeometry.coordinatesystem.model.unit.CustomUnits;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.vertex.AbstractVertex;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.marker.GeometryMarker;
import com.revolsys.swing.map.layer.record.style.marker.MarkerRenderer;
import com.revolsys.swing.map.view.ViewRenderer;

import tech.units.indriya.quantity.Quantities;

public class SelectedRecordsVertexRenderer {

  public static final String FIRST_VERTEX_SHAPE = "ellipse";

  public static String LAST_VERTEX_SHAPE = "solidArrow";

  public static String CENTRE_SHAPE = "crossLine";

  public static BiFunctionDouble<Geometry> VERTEX_SHAPE = (width, height) -> {
    return GeometryMarker.GEOMETRY_FACTORY.polygon(GeometryMarker.GEOMETRY_FACTORY.linearRing(2 //
    , 0.0, 0.0 //
    , 0.0, height //
    , width * 2 / 3, height //
    , width, height * 0.5 //
    , width * 2 / 3, 0 //
    , 0.0, 0.0 //
    ));
  };

  private final MarkerStyle firstVertexStyle;

  private final GeometryStyle highlightStyle;

  private final MarkerStyle lastVertexStyle;

  private final MarkerStyle vertexStyle;

  private final MarkerStyle centreStyle = MarkerStyle.marker(CENTRE_SHAPE, 3, WebColors.Black, 1,
    WebColors.Black);

  public SelectedRecordsVertexRenderer(final Color color, final boolean opaque) {
    final Color fillColor = color;

    this.highlightStyle = GeometryStyle.polygon(WebColors.Black, 1, fillColor) //
      .setMarker(FIRST_VERTEX_SHAPE, 9, WebColors.Black, 1, fillColor);

    this.vertexStyle = MarkerStyle.marker(VERTEX_SHAPE, 9, WebColors.Black, 1, color);
    this.vertexStyle.setMarkerOrientationType("auto");
    this.vertexStyle.setMarkerWidth(Quantities.getQuantity(11, CustomUnits.PIXEL));

    // final GeneralPath firstVertexShape = firstVertexShape();
    this.firstVertexStyle = MarkerStyle.marker(FIRST_VERTEX_SHAPE, 9, WebColors.Black, 1, color);
    this.firstVertexStyle.setMarkerOrientationType("auto");
    this.firstVertexStyle.setMarkerPlacementType("vertex(0)");
    this.firstVertexStyle.setMarkerHorizontalAlignment("center");

    this.lastVertexStyle = MarkerStyle.marker(LAST_VERTEX_SHAPE, 9, WebColors.Black, 1, color);
    this.lastVertexStyle.setMarkerOrientationType("auto");
    this.lastVertexStyle.setMarkerPlacementType("vertex(n)");
    this.lastVertexStyle.setMarkerHorizontalAlignment("right");
  }

  public void paintSelected(final ViewRenderer view, Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      geometry = view.getGeometry(geometry);

      view.drawGeometryOutline(this.highlightStyle, geometry);

      if (geometry != null && !geometry.isEmpty()) {
        geometry.forEachGeometryComponent(LineString.class, line -> {
          if (line != null) {
            line = view.convertGeometry(line);
            try (
              MarkerRenderer centreRenderer = this.centreStyle.newMarkerRenderer(view)) {
              try (
                MarkerRenderer vertexRenderer = this.firstVertexStyle.newMarkerRenderer(view)) {
                final AbstractVertex vertex = line.getVertex(0);
                vertexRenderer.renderMarkerVertex(vertex);
                centreRenderer.renderMarkerVertex(vertex);
              }
              try (
                MarkerRenderer vertexRenderer = this.vertexStyle.newMarkerRenderer(view)) {
                for (final Vertex vertex : line.vertices()) {
                  if (!vertex.isFrom() && !vertex.isTo()) {
                    vertexRenderer.renderMarkerVertex(vertex);
                    centreRenderer.renderMarkerVertex(vertex);
                  }
                }
              }
              try (
                MarkerRenderer vertexRenderer = this.lastVertexStyle.newMarkerRenderer(view)) {
                final AbstractVertex vertex = line.getVertex(line.getLastVertexIndex());
                vertexRenderer.renderMarkerVertex(vertex);
              }
            }
          }
        });
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
