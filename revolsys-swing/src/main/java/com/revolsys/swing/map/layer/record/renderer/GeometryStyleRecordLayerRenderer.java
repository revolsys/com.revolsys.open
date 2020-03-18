package com.revolsys.swing.map.layer.record.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Map;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.jeometry.common.data.type.DataType;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.io.BaseCloseable;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.renderer.shape.LineStringShape;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.panel.GeometryStylePanel;
import com.revolsys.swing.map.layer.record.style.panel.GeometryStylePreview;
import com.revolsys.swing.map.overlay.record.SelectedRecordsRenderer;
import com.revolsys.swing.map.view.ViewRenderer;

public class GeometryStyleRecordLayerRenderer extends AbstractGeometryRecordLayerRenderer
  implements GeometryStyleLayerRenderer<AbstractRecordLayer> {

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

  private GeometryStyle style = new GeometryStyle();

  public GeometryStyleRecordLayerRenderer() {
    super("geometryStyle", "Geometry Style", ICON);
  }

  public GeometryStyleRecordLayerRenderer(final AbstractRecordLayer layer) {
    this(layer, new GeometryStyle());
  }

  public GeometryStyleRecordLayerRenderer(final AbstractRecordLayer layer,
    final GeometryStyle style) {
    this();
    setLayer(layer);
    setStyle(style);
  }

  public GeometryStyleRecordLayerRenderer(final LayerRenderer<?> parent) {
    this();
    setParent(parent);
  }

  public GeometryStyleRecordLayerRenderer(final LayerRenderer<?> parent,
    final GeometryStyle style) {
    this();
    setParent(parent);
    setStyle(style);
  }

  public GeometryStyleRecordLayerRenderer(final Map<String, ? extends Object> properties) {
    this();
    setProperties(properties);
  }

  @Override
  public GeometryStyleRecordLayerRenderer clone() {
    final GeometryStyleRecordLayerRenderer clone = (GeometryStyleRecordLayerRenderer)super.clone();
    if (this.style != null) {
      clone.setStyle(this.style.clone());
    }
    return clone;
  }

  @Override
  public DataType getGeometryType() {
    final FieldDefinition geometryField = getGeometryField();
    if (geometryField != null) {
      final DataType geometryDataType = geometryField.getDataType();
      if (GeometryDataTypes.GEOMETRY_COLLECTION.equals(geometryDataType)) {
        return GeometryDataTypes.GEOMETRY;
      } else if (GeometryDataTypes.MULTI_POINT.equals(geometryDataType)) {
        return GeometryDataTypes.POINT;
      } else if (GeometryDataTypes.MULTI_LINE_STRING.equals(geometryDataType)) {
        return GeometryDataTypes.LINE_STRING;
      } else if (GeometryDataTypes.MULTI_POLYGON.equals(geometryDataType)) {
        return GeometryDataTypes.POLYGON;
      } else {
        return geometryDataType;
      }
    }
    return null;
  }

  @Override
  public Icon getIcon() {
    Icon icon = super.getIcon();
    if (icon == ICON) {
      icon = newIcon();
      setIcon(icon);
    }
    return icon;
  }

  @Override
  public GeometryStyle getStyle() {
    return this.style;
  }

  @Override
  public Icon newIcon() {
    final AbstractRecordLayer layer = getLayer();
    if (layer == null) {
      return ICON;
    } else {
      final GeometryStyle geometryStyle = getStyle();
      Shape shape = null;
      final DataType geometryDataType = layer.getGeometryType();
      if (GeometryDataTypes.POINT.equals(geometryDataType)
        || GeometryDataTypes.MULTI_POINT.equals(geometryDataType)) {
        return this.style.getMarker().newIcon(geometryStyle);
      } else if (GeometryDataTypes.LINE_STRING.equals(geometryDataType)
        || GeometryDataTypes.MULTI_LINE_STRING.equals(geometryDataType)) {
        shape = new LineStringShape(GeometryStylePreview.getLineString(16));
      } else if (GeometryDataTypes.POLYGON.equals(geometryDataType)
        || GeometryDataTypes.MULTI_POLYGON.equals(geometryDataType)) {
        shape = getPolygonShape();
      } else {
        return ICON;
      }

      final BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
      final Graphics2D graphics = image.createGraphics();
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      if (GeometryDataTypes.POLYGON.equals(geometryDataType)
        || GeometryDataTypes.MULTI_POLYGON.equals(geometryDataType)) {
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

  @Override
  public GeometryStylePanel newStylePanel() {
    return new GeometryStylePanel(this);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source == this.style) {
      refreshIcon();
    }
    super.propertyChange(event);
  }

  @Override
  protected void renderRecord(final ViewRenderer view, final AbstractRecordLayer layer,
    final LayerRecord record, final Geometry geometry) {
  }

  @Override
  protected void renderRecordsDo(final ViewRenderer view, final AbstractRecordLayer layer,
    final List<LayerRecord> records) {
    if (!records.isEmpty()) {
      final boolean draw = this.style.getLineOpacity() > 0;
      final boolean fill = this.style.getPolygonFillOpacity() > 0;
      try (
        BaseCloseable geometryListCloseable = view.drawGeometriesCloseable(this.style, true, draw,
          fill)) {
        for (final LayerRecord record : records) {
          if (view.isCancelled()) {
            return;
          }
          if (isVisible(record)) {
            final Geometry geometry = record.getGeometry();
            view.addGeometry(geometry);
          }
        }
      }
    }
  }

  @Override
  protected void renderSelectedRecordsDo(final ViewRenderer view, final AbstractRecordLayer layer,
    final List<LayerRecord> records) {
    final DataType geometryType = layer.getGeometryType();
    if (geometryType == GeometryDataTypes.LINE_STRING
      || geometryType == GeometryDataTypes.MULTI_LINE_STRING) {
      final Quantity<Length> lineWidth = this.style.getLineWidth();
      final float width = (float)view.toDisplayValue(lineWidth);
      if (width <= SelectedRecordsRenderer.STYLE_SIZE) {
        return;
      }
    }
    if (geometryType == GeometryDataTypes.POINT || geometryType == GeometryDataTypes.MULTI_POINT) {
      if (this.style.getMarkerDx().getValue().intValue() == 0
        && this.style.getMarkerDy().getValue().intValue() == 0) {
        final Quantity<Length> markerWidth = this.style.getMarkerWidth();
        final float width = (float)view.toDisplayValue(markerWidth);
        if (width <= SelectedRecordsRenderer.STYLE_SIZE) {
          return;
        }

        final Quantity<Length> markerHeight = this.style.getMarkerHeight();
        final float height = (float)view.toDisplayValue(markerHeight);
        if (height <= SelectedRecordsRenderer.STYLE_SIZE) {
          return;
        }
      }
    }
    renderRecordsDo(view, layer, records);
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> properties) {
    super.setProperties(properties);
    if (this.style != null) {
      this.style.setProperties(properties);
    }
  }

  @Override
  public void setStyle(final GeometryStyle style) {
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
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    if (this.style != null) {
      final JsonObject styleMap = this.style.toMap();
      map.putAll(styleMap);
    }
    return map;
  }
}
