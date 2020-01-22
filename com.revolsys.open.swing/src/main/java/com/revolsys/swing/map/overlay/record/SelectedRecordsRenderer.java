package com.revolsys.swing.map.overlay.record;

import java.awt.Color;

import javax.measure.quantity.Length;

import org.jeometry.common.awt.WebColors;
import org.jeometry.coordinatesystem.model.unit.CustomUnits;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.record.Record;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.util.Property;

import tech.units.indriya.ComparableQuantity;
import tech.units.indriya.quantity.Quantities;

public class SelectedRecordsRenderer {

  public static final int STYLE_SIZE = 5;

  private final GeometryStyle highlightStyle = GeometryStyle
    .polygon(WebColors.Lime, STYLE_SIZE, WebColors.Lime) //
    .setMarker("ellipse", STYLE_SIZE, WebColors.Lime, 3, WebColors.Black);

  private final GeometryStyle lineStyle = GeometryStyle.line(WebColors.Black);

  private int alpha;

  public SelectedRecordsRenderer() {
  }

  public SelectedRecordsRenderer(final Color color, final int alpha) {
    setAlpha(alpha);
    setHighlightColor(color);
  }

  public void paintSelected(final ViewRenderer view, final GeometryFactory viewportGeometryFactory,
    Geometry geometry) {
    geometry = view.getGeometry(geometry);
    if (Property.hasValue(geometry)) {
      view.drawGeometry(geometry, this.highlightStyle);
      if (!(geometry instanceof Punctual)) {
        view.drawGeometryOutline(this.lineStyle, geometry);
      }
    }
  }

  public void paintSelected(final ViewRenderer view, final Record record) {
    if (record != null) {
      final Geometry geometry = record.getGeometry();
      final GeometryFactory geometryFactory = view.getGeometryFactory();
      paintSelected(view, geometryFactory, geometry);
    }
  }

  public SelectedRecordsRenderer setAlpha(final int alpha) {
    this.alpha = alpha;
    return this;
  }

  public SelectedRecordsRenderer setHighlightColor(final Color color) {
    final Color fillColor = WebColors.newAlpha(color, this.alpha);
    this.highlightStyle.setLineColor(color);
    this.highlightStyle.setPolygonFill(fillColor);
    this.highlightStyle.setMarkerLineColor(color);
    return this;
  }

  public SelectedRecordsRenderer setHighlightLineWidth(final int width) {
    final ComparableQuantity<Length> lineWidth = Quantities.getQuantity(width, CustomUnits.PIXEL);
    this.highlightStyle.setLineWidth(lineWidth);
    this.highlightStyle.setMarkerLineWidth(lineWidth);
    return this;
  }

  public SelectedRecordsRenderer setLineColor(final Color color) {
    this.lineStyle.setLineColor(color);
    return this;
  }

  public SelectedRecordsRenderer setLineWidth(final int width) {
    final ComparableQuantity<Length> lineWidth = Quantities.getQuantity(width, CustomUnits.PIXEL);
    this.lineStyle.setLineWidth(lineWidth);
    return this;
  }
}
