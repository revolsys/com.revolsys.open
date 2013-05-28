package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.menu.ChangeStyle;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.map.layer.dataobject.style.panel.GeometryStylePanel;
import com.revolsys.swing.map.util.GeometryShapeUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryStyleRenderer extends AbstractDataObjectLayerRenderer {

  static {
    MenuFactory menu = ObjectTreeModel.getMenu(GeometryStyleRenderer.class);
    menu.addMenuItem("style", new ChangeStyle());
  }

  public static Shape getShape(final Viewport2D viewport,
    final GeometryStyle style, final Geometry geometry) {
    final BoundingBox viewExtent = viewport.getBoundingBox();
    if (geometry != null) {
      if (!viewExtent.isNull()) {
        final BoundingBox geometryExtent = BoundingBox.getBoundingBox(geometry);
        if (geometryExtent.intersects(viewExtent)) {
          final GeometryFactory geometryFactory = viewport.getGeometryFactory();
          final Geometry convertedGeometry = geometryFactory.createGeometry(geometry);
          // TODO clipping
          return GeometryShapeUtil.toShape(viewport, convertedGeometry);
        }
      }
    }
    return null;
  }

  public static final void renderGeometry(final Viewport2D viewport,
    final Graphics2D graphics, final Geometry geometry,
    final GeometryStyle style) {
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      final Geometry part = geometry.getGeometryN(i);
      if (part instanceof Point) {
        final Point point = (Point)part;
        MarkerStyleRenderer.renderMarker(viewport, graphics, point, style);
      } else if (part instanceof LineString) {
        final LineString lineString = (LineString)part;
        renderLineString(viewport, graphics, lineString, style);
      } else if (part instanceof Polygon) {
        final Polygon polygon = (Polygon)part;
        renderPolygon(viewport, graphics, polygon, style);
      }
    }
  }

  public static final void renderLineString(final Viewport2D viewport,
    final Graphics2D graphics, final LineString lineString,
    final GeometryStyle style) {
    final Shape shape = getShape(viewport, style, lineString);
    if (shape != null) {
      final boolean savedUseModelUnits = viewport.isUseModelCoordinates();
      viewport.setUseModelCoordinates(true, graphics);
      final Paint paint = graphics.getPaint();
      try {
        style.setLineStyle(viewport, graphics);
        graphics.draw(shape);
      } finally {
        viewport.setUseModelCoordinates(savedUseModelUnits, graphics);
        graphics.setPaint(paint);
      }
    }
  }

  public static final void renderOutline(final Viewport2D viewport,
    final Graphics2D graphics, final Geometry geometry,
    final GeometryStyle style) {
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      final Geometry part = geometry.getGeometryN(i);
      if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        MarkerStyleRenderer.renderMarker(viewport, graphics, point, style);
      } else if (part instanceof LineString) {
        final LineString lineString = (LineString)part;
        renderLineString(viewport, graphics, lineString, style);
      } else if (part instanceof Polygon) {
        final Polygon polygon = (Polygon)part;
        renderLineString(viewport, graphics, polygon.getExteriorRing(), style);
        for (int j = 0; j < polygon.getNumInteriorRing(); j++) {
          final LineString ring = polygon.getInteriorRingN(j);
          renderLineString(viewport, graphics, ring, style);
        }
      }
    }
  }

  public static final void renderPolygon(final Viewport2D viewport,
    final Graphics2D graphics, final Polygon polygon, final GeometryStyle style) {
    final Shape shape = getShape(viewport, style, polygon);
    if (shape != null) {
      final boolean savedUseModelUnits = viewport.isUseModelCoordinates();
      viewport.setUseModelCoordinates(true, graphics);
      final Paint paint = graphics.getPaint();
      try {
        style.setFillStyle(viewport, graphics);
        graphics.fill(shape);
        style.setLineStyle(viewport, graphics);
        graphics.draw(shape);
      } finally {
        viewport.setUseModelCoordinates(savedUseModelUnits, graphics);
        graphics.setPaint(paint);
      }
    }
  }

  private GeometryStyle style;

  public GeometryStyleRenderer(final DataObjectLayer layer) {
    this(layer, new GeometryStyle());
  }

  public GeometryStyleRenderer(final DataObjectLayer layer,
    final GeometryStyle style) {
    this(layer, null, style);
  }

  public GeometryStyleRenderer(final DataObjectLayer layer,
    final LayerRenderer<?> parent, final GeometryStyle style) {
    super("geometryStyle", layer, parent);
    this.style = style;
  }

  public GeometryStyleRenderer(final DataObjectLayer layer,
    final LayerRenderer<?> parent, final Map<String, Object> geometryStyle) {
    super("geometryStyle", layer, parent, geometryStyle);
    final Map<String, Object> style = getAllDefaults();
    style.putAll(geometryStyle);
    this.style = new GeometryStyle(style);
  }

  public GeometryStyle getStyle() {
    return style;
  }

  @Override
  protected void renderObject(final Viewport2D viewport,
    final Graphics2D graphics, final BoundingBox visibleArea,
    final DataObjectLayer layer, final DataObject object) {
    final Geometry geometry = object.getGeometryValue();
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);
    renderGeometry(viewport, graphics, geometry, style);
  }

  public void setStyle(final GeometryStyle style) {
    this.style = style;
    getPropertyChangeSupport().firePropertyChange("style", null, style);
  }

  @SuppressWarnings("unchecked")
  public <V extends ValueField<?>> V createStylePanel() {
    return (V)new GeometryStylePanel(this);
  }

}
