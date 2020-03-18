package com.revolsys.swing.map.layer.record.renderer;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.Icons;
import com.revolsys.swing.component.Form;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.marker.MarkerRenderer;
import com.revolsys.swing.map.layer.record.style.panel.MarkerStylePanel;
import com.revolsys.swing.map.view.ViewRenderer;

public class MarkerStyleRenderer extends AbstractGeometryRecordLayerRenderer {
  private static final Icon ICON = Icons.getIcon("style_marker");

  private MarkerStyle style = new MarkerStyle();

  public MarkerStyleRenderer() {
    super("markerStyle", "Marker Style", ICON);
  }

  public MarkerStyleRenderer(final AbstractRecordLayer layer, final MarkerStyle style) {
    this();
    setLayer(layer);
    setStyle(style);
  }

  public MarkerStyleRenderer(final LayerRenderer<?> parent) {
    this();
    setParent(parent);
  }

  public MarkerStyleRenderer(final Map<String, ? extends Object> properties) {
    this();
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
  protected void renderRecord(final ViewRenderer view, final AbstractRecordLayer layer,
    final LayerRecord record, final Geometry geometry) {
  }

  @Override
  protected void renderRecordsDo(final ViewRenderer view, final AbstractRecordLayer layer,
    final List<LayerRecord> records) {
    if (!records.isEmpty()) {
      try (
        MarkerRenderer renderer = this.style.newMarkerRenderer(view)) {
        for (final LayerRecord record : records) {
          if (view.isCancelled()) {
            return;
          }
          if (isVisible(record)) {
            final Geometry geometry = record.getGeometry();
            final Geometry viewGeometry = view.convertGeometry(geometry, 2);
            renderer.renderMarkerGeometry(viewGeometry);
          }
        }
      }
    }
  }

  @Override
  protected void renderSelectedRecordsDo(final ViewRenderer view, final AbstractRecordLayer layer,
    final List<LayerRecord> records) {
    if (isVisible(view)) {
      super.renderSelectedRecordsDo(view, layer, records);
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
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    if (this.style != null) {
      final Map<String, Object> styleMap = this.style.toMap();
      map.putAll(styleMap);
    }
    return map;
  }
}
