package com.revolsys.swing.map.layer.record.renderer;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.beans.PropertyChangeEvent;
import java.util.Map;

import javax.swing.Icon;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.coordinates.PointWithOrientation;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.io.BaseCloseable;
import com.revolsys.logging.Logs;
import com.revolsys.swing.Icons;
import com.revolsys.swing.component.Form;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.marker.Marker;
import com.revolsys.swing.map.layer.record.style.panel.MarkerStylePanel;
import com.revolsys.util.Property;

public class MarkerStyleRenderer extends AbstractRecordLayerRenderer {
  private static final Icon ICON = Icons.getIcon("style_marker");

  public static <G extends Geometry> G getGeometry(final Viewport2D viewport, final G geometry) {
    final BoundingBox viewExtent = viewport.getBoundingBox();
    if (geometry != null) {
      if (!viewExtent.isEmpty()) {
        final BoundingBox geometryExtent = geometry.getBoundingBox();
        if (geometryExtent.intersects(viewExtent)) {
          final GeometryFactory geometryFactory = viewport.getGeometryFactory();
          return geometry.convertGeometry(geometryFactory);
        }
      }
    }
    return null;
  }

  public static PointWithOrientation getMarkerLocation(final Viewport2D viewport,
    final Geometry geometry, final MarkerStyle style) {
    final String placementType = style.getMarkerPlacementType();
    return getPointWithOrientation(viewport, geometry, placementType);
  }

  public static void renderMarker(final Viewport2D viewport, final Geometry geometry,
    final MarkerStyle style) {
    try (
      BaseCloseable transformClosable = viewport.setUseModelCoordinates(false)) {
      @SuppressWarnings("deprecation")
      final Graphics2D graphics = viewport.getGraphics();
      if (graphics != null && geometry != null) {
        if ("vertices".equals(style.getMarkerPlacementType())) {
          renderMarkerVertices(viewport, graphics, geometry, style);
        } else if ("segments".equals(style.getMarkerPlacementType())) {
          renderMarkerSegments(viewport, graphics, geometry, style);
        } else {
          for (int i = 0; i < geometry.getGeometryCount(); i++) {
            final Geometry part = geometry.getGeometry(i);
            if (part instanceof Point) {
              final Point point = (Point)part;
              renderMarker(viewport, graphics, point, style, 0);
            } else if (part instanceof LineString) {
              final LineString line = (LineString)part;
              renderMarker(viewport, graphics, line, style);
            } else if (part instanceof Polygon) {
              final Polygon polygon = (Polygon)part;
              renderMarker(viewport, graphics, polygon, style);
            }
          }
        }
      }
    }
  }

  private static void renderMarker(final Viewport2D viewport, final Graphics2D graphics,
    final LineString line, final MarkerStyle style) {
    final PointWithOrientation point = getMarkerLocation(viewport, line, style);
    if (point != null) {
      final double orientation = point.getOrientation();
      renderMarker(viewport, graphics, point, style, orientation);
    }
  }

  /**
   * Point must be in the same geometry factory as the view.
   *
   * @param viewport
   * @param graphics
   * @param point
   * @param style
   */
  public static void renderMarker(final Viewport2D viewport, final Graphics2D graphics, Point point,
    final MarkerStyle style, final double orientation) {
    point = getGeometry(viewport, point);
    if (Property.hasValue(point)) {
      final Paint paint = graphics.getPaint();
      try (
        BaseCloseable transformClosable = viewport.setUseModelCoordinates(graphics, false)) {
        final Marker marker = style.getMarker();
        final double x = point.getX();
        final double y = point.getY();
        marker.render(viewport, graphics, style, x, y, orientation);
      } catch (final Throwable e) {
        Logs.debug(MarkerStyleRenderer.class, "Unable to render marker: " + style, e);
      } finally {
        graphics.setPaint(paint);
      }
    }
  }

  private static void renderMarker(final Viewport2D viewport, final Graphics2D graphics,
    final Polygon polygon, final MarkerStyle style) {
    final Point point = polygon.getPointWithin();
    renderMarker(viewport, graphics, point, style, 0);
  }

  public static void renderMarkers(final Viewport2D viewport, final Graphics2D graphics,
    LineString line, final MarkerStyle styleFirst, final MarkerStyle styleLast,
    final MarkerStyle styleVertex) {
    line = viewport.convertGeometry(line, 2);
    if (Property.hasValue(line)) {
      for (final Vertex vertex : line.vertices()) {
        MarkerStyle style;
        if (vertex.isFrom()) {
          style = styleFirst;
        } else if (vertex.isTo()) {
          style = styleLast;
        } else {
          style = styleVertex;
        }
        final double orientation = vertex.getOrientaton();
        renderMarker(viewport, graphics, vertex, style, orientation);
      }
    }
  }

  public static final void renderMarkerSegments(final Viewport2D viewport,
    final Graphics2D graphics, Geometry geometry, final MarkerStyle style) {
    geometry = getGeometry(viewport, geometry);
    if (Property.hasValue(geometry)) {
      final String orientationType = style.getMarkerOrientationType();
      if ("none".equals(orientationType)) {
        for (final Segment segment : geometry.segments()) {
          final Point point = segment.midPoint();
          renderMarker(viewport, graphics, point, style, 0);
        }
      } else {
        for (final Segment segment : geometry.segments()) {
          final Point point = segment.midPoint();
          final double orientation = segment.getOrientaton();
          renderMarker(viewport, graphics, point, style, orientation);
        }
      }
    }
  }

  public static final void renderMarkerVertices(final Viewport2D viewport,
    final Graphics2D graphics, Geometry geometry, final MarkerStyle style) {
    geometry = getGeometry(viewport, geometry);
    if (Property.hasValue(geometry)) {
      final String orientationType = style.getMarkerOrientationType();
      if ("none".equals(orientationType)) {
        for (final Vertex vertex : geometry.vertices()) {
          renderMarker(viewport, graphics, vertex, style, 0);
        }
      } else {
        for (final Vertex vertex : geometry.vertices()) {
          final double orientation = vertex.getOrientaton();
          renderMarker(viewport, graphics, vertex, style, orientation);
        }
      }
    }
  }

  private MarkerStyle style = new MarkerStyle();

  public MarkerStyleRenderer(final AbstractRecordLayer layer, final LayerRenderer<?> parent) {
    super("markerStyle", "Marker Style", layer, parent);
    setIcon(ICON);
  }

  public MarkerStyleRenderer(final AbstractRecordLayer layer, final LayerRenderer<?> parent,
    final MarkerStyle style) {
    super("markerStyle", "Marker Style", layer, parent);
    setStyle(style);
  }

  public MarkerStyleRenderer(final AbstractRecordLayer layer, final MarkerStyle style) {
    this(layer, null, style);
  }

  public MarkerStyleRenderer(final Map<String, ? extends Object> properties) {
    super("markerStyle", "Marker Style");
    setIcon(ICON);
    setProperties(properties);
  }

  @Override
  public MarkerStyleRenderer clone() {
    final MarkerStyleRenderer clone = (MarkerStyleRenderer)super.clone();
    if (this.style != null) {
      clone.setStyle(this.style.clone());
    }
    return clone;
  }

  public MarkerStyle getStyle() {
    return this.style;
  }

  @Override
  public Icon newIcon() {
    if (this.style == null) {
      return ICON;
    } else {
      return this.style.newIcon();
    }
  }

  @Override
  public Form newStylePanel() {
    return new MarkerStylePanel(this);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source == this.style) {
      final Icon icon = this.style.newIcon();
      setIcon(icon);
    }
    super.propertyChange(event);
  }

  @Override
  public void renderRecord(final Viewport2D viewport, final BoundingBox visibleArea,
    final AbstractLayer layer, final LayerRecord record) {
    if (isVisible(record)) {
      final Geometry geometry = record.getGeometry();
      renderMarker(viewport, geometry, this.style);
    }
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> properties) {
    super.setProperties(properties);
    if (this.style != null) {
      this.style.setProperties(properties);
      final Icon icon = this.style.newIcon();
      setIcon(icon);
    }
  }

  public void setStyle(final MarkerStyle style) {
    if (this.style != null) {
      this.style.removePropertyChangeListener(this);
    }
    this.style = style;
    if (this.style != null) {
      this.style.addPropertyChangeListener(this);
    }
    firePropertyChange("style", null, style);
    refreshIcon();
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    if (this.style != null) {
      final Map<String, Object> styleMap = this.style.toMap();
      map.putAll(styleMap);
    }
    return map;
  }
}
