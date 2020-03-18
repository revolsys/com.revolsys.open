package com.revolsys.swing.map.layer.record.style.marker;

import java.util.Collection;

import javax.measure.quantity.Length;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.jeometry.coordinatesystem.model.unit.CustomUnits;

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.swing.map.ImageViewport;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.view.ViewRenderer;

import tech.units.indriya.ComparableQuantity;
import tech.units.indriya.quantity.Quantities;

public interface Marker extends MapSerializer {
  default Icon getIcon() {
    return null;
  }

  default String getName() {
    return null;
  }

  default String getTitle() {
    return null;
  }

  default boolean isUseMarkerName() {
    return false;
  }

  default Icon newIcon(MarkerStyle style) {
    style = style.clone();
    final ComparableQuantity<Length> size = Quantities.getQuantity(15, CustomUnits.PIXEL);
    style.setMarkerWidth(size);
    style.setMarkerHeight(size);
    try (
      final ImageViewport viewport = new ImageViewport(16, 16)) {
      final ViewRenderer view = viewport.newViewRenderer();
      final MarkerRenderer markerRenderer = newMarkerRenderer(view, style);
      markerRenderer.renderMarkerPoint(new PointDoubleXY(8, 8));
      return new ImageIcon(viewport.getImage());
    }
  }

  MarkerRenderer newMarkerRenderer(ViewRenderer view, MarkerStyle style);

  default void renderPoints(final ViewRenderer view, final MarkerStyle style,
    final Collection<? extends Point> points) {
    if (!points.isEmpty()) {
      try (
        MarkerRenderer markerRenderer = newMarkerRenderer(view, style)) {
        markerRenderer.renderMarkers(points);
      }
    }
  }
}
