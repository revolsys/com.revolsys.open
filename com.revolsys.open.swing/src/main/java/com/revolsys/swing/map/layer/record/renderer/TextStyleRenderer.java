package com.revolsys.swing.map.layer.record.renderer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.Icons;
import com.revolsys.swing.component.Form;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.style.TextStyle;
import com.revolsys.swing.map.layer.record.style.panel.TextStylePanel;
import com.revolsys.swing.map.view.ViewRenderer;

public class TextStyleRenderer extends AbstractGeometryRecordLayerRenderer {

  private static final Icon ICON = Icons.getIcon("style_text");

  private TextStyle style = new TextStyle();

  public TextStyleRenderer() {
    super("textStyle", "Text Style", ICON);
  }

  public TextStyleRenderer(final AbstractRecordLayer layer, final TextStyle textStyle) {
    this();
    setStyle(textStyle);
  }

  public TextStyleRenderer(final LayerRenderer<?> parent) {
    this();
    setParent(parent);
    setIcon(newIcon());
  }

  public TextStyleRenderer(final Map<String, ? extends Object> properties) {
    this();
    setProperties(properties);
  }

  @Override
  public TextStyleRenderer clone() {
    final TextStyleRenderer clone = (TextStyleRenderer)super.clone();
    clone.setStyle(this.style.clone());
    return clone;
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

  public TextStyle getStyle() {
    return this.style;
  }

  @Override
  public Icon newIcon() {
    final BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D graphics = image.createGraphics();
    this.style.drawTextIcon(graphics, 12);
    graphics.dispose();
    final Icon icon = new ImageIcon(image);
    return icon;

  }

  @Override
  public Form newStylePanel() {
    return new TextStylePanel(this);
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
    view.drawText(record, geometry, this.style);
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
    }
  }

  public void setStyle(final TextStyle style) {
    if (this.style != null) {
      this.style.removePropertyChangeListener(this);
    }
    this.style = style;
    if (this.style != null) {
      this.style.addPropertyChangeListener(this);
    }
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
