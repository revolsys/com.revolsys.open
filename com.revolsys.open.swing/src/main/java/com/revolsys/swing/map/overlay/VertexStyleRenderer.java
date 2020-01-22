package com.revolsys.swing.map.overlay;

import java.awt.Color;
import java.awt.Graphics2D;

import org.jeometry.common.awt.WebColors;
import org.jeometry.coordinatesystem.model.unit.CustomUnits;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.overlay.record.SelectedRecordsVertexRenderer;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.util.Property;

import tech.units.indriya.quantity.Quantities;

public class VertexStyleRenderer {
  private final MarkerStyle fromVertexStyle;

  private final MarkerStyle toVertexStyle;

  private final MarkerStyle vertexStyle;

  private final MarkerStyle centreStyle = MarkerStyle
    .marker(SelectedRecordsVertexRenderer.CENTRE_SHAPE, 2, WebColors.Black, 1, WebColors.Black);

  public VertexStyleRenderer(final Color color) {
    this.vertexStyle = MarkerStyle.marker(SelectedRecordsVertexRenderer.VERTEX_SHAPE, 9,
      WebColors.Black, 1, color);
    this.vertexStyle.setMarkerOrientationType("auto");
    this.vertexStyle.setMarkerWidth(Quantities.getQuantity(11, CustomUnits.PIXEL));

    this.fromVertexStyle = MarkerStyle.marker(SelectedRecordsVertexRenderer.FIRST_VERTEX_SHAPE, 9,
      WebColors.Black, 1, color);
    this.fromVertexStyle.setMarkerOrientationType("auto");
    this.fromVertexStyle.setMarkerPlacementType("vertex(0)");
    this.fromVertexStyle.setMarkerHorizontalAlignment("center");

    this.toVertexStyle = MarkerStyle.marker(SelectedRecordsVertexRenderer.LAST_VERTEX_SHAPE, 9,
      WebColors.Black, 1, color);
    this.toVertexStyle.setMarkerOrientationType("auto");
    this.toVertexStyle.setMarkerPlacementType("vertex(n)");
    this.toVertexStyle.setMarkerHorizontalAlignment("right");
  }

  public void paintSelected(final ViewRenderer view, final Graphics2D graphics,
    final GeometryFactory viewportGeometryFactory, final Vertex vertex) {
    if (Property.hasValue(vertex)) {
      MarkerStyle style;
      final boolean to = vertex.isTo();
      if (vertex.isFrom()) {
        style = this.fromVertexStyle;
      } else if (to) {
        style = this.toVertexStyle;
      } else {
        style = this.vertexStyle;
      }
      final double orientation = vertex.getOrientaton();
      view.drawMarker(style, vertex, orientation);
      if (!to) {
        view.renderMarker(this.centreStyle, vertex);
      }
    }
  }
}
