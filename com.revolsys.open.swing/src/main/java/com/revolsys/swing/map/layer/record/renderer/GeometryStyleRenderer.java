package com.revolsys.swing.map.layer.record.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.panel.GeometryStylePanel;
import com.revolsys.swing.map.layer.record.style.panel.GeometryStylePreview;

public class GeometryStyleRenderer extends AbstractRecordLayerRenderer {

  private static final Icon ICON = Icons.getIcon("style_geometry");

  public static GeneralPath getLineShape() {
    final GeneralPath path = new GeneralPath();
    path.moveTo(0, 0);
    path.lineTo(15, 0);
    path.lineTo(0, 15);
    path.lineTo(15, 15);
    return path;
  }

  public static GeneralPath getPolygonShape() {
    final GeneralPath path = new GeneralPath();
    path.moveTo(0, 0);
    path.lineTo(7, 0);
    path.lineTo(15, 8);
    path.lineTo(15, 15);
    path.lineTo(8, 15);
    path.lineTo(0, 7);
    path.lineTo(0, 0);
    path.closePath();
    return path;
  }

  public static final void renderGeometry(final Viewport2D viewport, final Graphics2D graphics,
    final Geometry geometry, final GeometryStyle style) {
    if (geometry != null) {
      final BoundingBox viewExtent = viewport.getBoundingBox();
      if (!viewExtent.isEmpty()) {
        final GeometryFactory viewGeometryFactory = viewport.getGeometryFactory();
        for (int i = 0; i < geometry.getGeometryCount(); i++) {
          final Geometry part = geometry.getGeometry(i);
          final BoundingBox partExtent = part.getBoundingBox();
          if (partExtent.intersects(viewExtent)) {
            final Geometry convertedPart = part.convertGeometry(viewGeometryFactory);
            if (convertedPart instanceof Point) {
              final Point point = (Point)convertedPart;
              MarkerStyleRenderer.renderMarker(viewport, graphics, point, style, 0);
            } else if (convertedPart instanceof LineString) {
              final LineString lineString = (LineString)convertedPart;
              renderLineString(viewport, graphics, lineString, style);
            } else if (convertedPart instanceof Polygon) {
              final Polygon polygon = (Polygon)convertedPart;
              renderPolygon(viewport, graphics, polygon, style);
            }
          }
        }
      }
    }
  }

  public static final void renderGeometryOutline(final Viewport2D viewport,
    final Graphics2D graphics, final Geometry geometry, final GeometryStyle style) {
    if (geometry != null) {
      final BoundingBox viewExtent = viewport.getBoundingBox();
      if (!viewExtent.isEmpty()) {
        final GeometryFactory viewGeometryFactory = viewport.getGeometryFactory();
        for (int i = 0; i < geometry.getGeometryCount(); i++) {
          final Geometry part = geometry.getGeometry(i);
          final BoundingBox partExtent = part.getBoundingBox();
          if (partExtent.intersects(viewExtent)) {
            final Geometry convertedPart = part.convertGeometry(viewGeometryFactory);
            if (convertedPart instanceof Point) {
              final Point point = (Point)convertedPart;
              MarkerStyleRenderer.renderMarker(viewport, graphics, point, style, 0);
            } else if (convertedPart instanceof LineString) {
              final LineString lineString = (LineString)convertedPart;
              renderLineString(viewport, graphics, lineString, style);
            } else if (convertedPart instanceof Polygon) {
              final Polygon polygon = (Polygon)convertedPart;
              for (final LinearRing ring : polygon.rings()) {
                renderLineString(viewport, graphics, ring, style);
              }
            }
          }
        }
      }
    }
  }

  public static final void renderLineString(final Viewport2D viewport, final Graphics2D graphics,
    LineString line, final GeometryStyle style) {
    final GeometryFactory viewGeometryFactory = viewport.getGeometryFactory();
    line = line.convertGeometry(viewGeometryFactory, 2);
    if (!line.isEmpty()) {
      final Paint paint = graphics.getPaint();
      try {
        style.setLineStyle(viewport, graphics);
        graphics.draw(line);
      } finally {
        graphics.setPaint(paint);
      }
    }
  }

  public static final void renderPolygon(final Viewport2D viewport, final Graphics2D graphics,
    Polygon polygon, final GeometryStyle style) {
    final GeometryFactory viewGeometryFactory = viewport.getGeometryFactory();
    polygon = polygon.convertGeometry(viewGeometryFactory, 2);
    if (!polygon.isEmpty()) {
      final Paint paint = graphics.getPaint();
      try {
        style.setFillStyle(viewport, graphics);
        graphics.fill(polygon);
        style.setLineStyle(viewport, graphics);
        graphics.draw(polygon);
      } finally {
        graphics.setPaint(paint);
      }
    }
  }

  private GeometryStyle style = new GeometryStyle();

  public GeometryStyleRenderer(final AbstractRecordLayer layer) {
    this(layer, new GeometryStyle());
  }

  public GeometryStyleRenderer(final AbstractRecordLayer layer, final GeometryStyle style) {
    this(layer, null, style);
  }

  public GeometryStyleRenderer(final AbstractRecordLayer layer, final LayerRenderer<?> parent) {
    super("geometryStyle", "Geometry Style", layer, parent);
    setIcon(ICON);
  }

  public GeometryStyleRenderer(final AbstractRecordLayer layer, final LayerRenderer<?> parent,
    final GeometryStyle style) {
    super("geometryStyle", "Geometry Style", layer, parent);
    setStyle(style);
    setIcon(ICON);
  }

  @Override
  public GeometryStyleRenderer clone() {
    final GeometryStyleRenderer clone = (GeometryStyleRenderer)super.clone();
    if (this.style != null) {
      clone.setStyle(this.style.clone());
    }
    return clone;
  }

  @Override
  public Icon getIcon() {
    final AbstractRecordLayer layer = getLayer();
    if (layer == null) {
      return super.getIcon();
    } else {
      final GeometryStyle geometryStyle = getStyle();
      Shape shape = null;
      final DataType geometryDataType = layer.getGeometryType();
      if (DataTypes.POINT.equals(geometryDataType)
        || DataTypes.MULTI_POINT.equals(geometryDataType)) {
        return this.style.getMarker().getIcon(geometryStyle);
      } else if (DataTypes.LINE_STRING.equals(geometryDataType)
        || DataTypes.MULTI_LINE_STRING.equals(geometryDataType)) {
        shape = GeometryStylePreview.getLineShape(16);
      } else if (DataTypes.POLYGON.equals(geometryDataType)
        || DataTypes.POLYGON.equals(geometryDataType)) {
        shape = getPolygonShape();
      } else {
        return super.getIcon();
      }

      final BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
      final Graphics2D graphics = image.createGraphics();
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      if (DataTypes.POLYGON.equals(geometryDataType)) {
        graphics.setPaint(geometryStyle.getPolygonFill());
        graphics.fill(shape);
      }
      final Color color = geometryStyle.getLineColor();
      graphics.setColor(color);

      graphics.draw(shape);
      graphics.dispose();
      return new ImageIcon(image);

    }
  }

  public GeometryStyle getStyle() {
    return this.style;
  }

  @Override
  public GeometryStylePanel newStylePanel() {
    return new GeometryStylePanel(this);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source == this.style) {
      final Icon icon = getIcon();
      firePropertyChange("icon", null, icon);
    }
    super.propertyChange(event);
  }

  @Override
  public void renderRecord(final Viewport2D viewport, final BoundingBox visibleArea,
    final AbstractLayer layer, final LayerRecord record) {
    final Geometry geometry = record.getGeometry();
    viewport.drawGeometry(geometry, this.style);
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> properties) {
    super.setProperties(properties);
    if (this.style != null) {
      this.style.setProperties(properties);
    }
  }

  public void setStyle(final GeometryStyle style) {
    if (this.style != null) {
      this.style.removePropertyChangeListener(this);
    }
    this.style = style;
    if (this.style != null) {
      this.style.addPropertyChangeListener(this);
    }
    firePropertyChange("style", null, style);
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    if (this.style != null) {
      final Map<String, Object> styleMap = this.style.toMap();
      map.putAll(styleMap);
    }
    return map;
  }
}
