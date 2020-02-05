package com.revolsys.swing.map.layer.elevation.gridded.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.jeometry.common.awt.WebColors;
import org.jeometry.common.data.type.DataType;

import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.MultipleLayerRenderer;
import com.revolsys.swing.map.layer.elevation.ElevationModelLayer;
import com.revolsys.swing.map.layer.record.renderer.GeometryStyleLayerRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.panel.GeometryStylePanel;
import com.revolsys.swing.map.view.ViewRenderer;

public class BoundingBoxGriddedElevationModelLayerRenderer
  extends AbstractGriddedElevationModelLayerRenderer
  implements GeometryStyleLayerRenderer<ElevationModelLayer> {
  private static final GeometryStyle DEFAULT_STYLE = GeometryStyle.polygon(WebColors.FireBrick, 1,
    WebColors.newAlpha(WebColors.FireBrick, 0));

  private static final Icon ICON = Icons.getIcon("style_geometry");

  private GeometryStyle style = new GeometryStyle();

  private BoundingBoxGriddedElevationModelLayerRenderer() {
    super("boundingBoxGriddedElevationModelLayerRenderer", "Outline", ICON);

  }

  public BoundingBoxGriddedElevationModelLayerRenderer(final ElevationModelLayer layer,
    final MultipleLayerRenderer<ElevationModelLayer, AbstractGriddedElevationModelLayerRenderer> parent) {
    this();
    setParent((LayerRenderer<?>)parent);
    setLayer(layer);
    setStyle(DEFAULT_STYLE.clone());
  }

  public BoundingBoxGriddedElevationModelLayerRenderer(
    final Map<String, ? extends Object> properties) {
    this();
    setProperties(properties);
  }

  @Override
  public BoundingBoxGriddedElevationModelLayerRenderer clone() {
    final BoundingBoxGriddedElevationModelLayerRenderer clone = (BoundingBoxGriddedElevationModelLayerRenderer)super.clone();
    if (this.style != null) {
      clone.setStyle(this.style.clone());
    }
    return clone;
  }

  @SuppressWarnings("unchecked")
  public void delete() {
    final LayerRenderer<?> parent = getParent();
    if (parent instanceof MultipleLayerRenderer) {
      final MultipleLayerRenderer<ElevationModelLayer, AbstractGriddedElevationModelLayerRenderer> multiple = (MultipleLayerRenderer<ElevationModelLayer, AbstractGriddedElevationModelLayerRenderer>)parent;
      multiple.removeRenderer(this);
    }
  }

  @Override
  public DataType getGeometryType() {
    return GeometryDataTypes.POLYGON;
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
    final GeometryStyle geometryStyle = getStyle();

    final BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D graphics = image.createGraphics();
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    graphics.setPaint(geometryStyle.getPolygonFill());
    graphics.fillRect(0, 0, 15, 15);
    final Color color = geometryStyle.getLineColor();

    graphics.setColor(color);
    graphics.drawRect(0, 0, 15, 15);

    graphics.dispose();
    return new ImageIcon(image);
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
  public void render(final ViewRenderer view, final ElevationModelLayer layer,
    final BufferedGeoreferencedImage image) {
    if (!view.isCancelled()) {
      final Polygon boundary = getLayer().getBoundingBox().toPolygon(10);
      view.drawGeometry(boundary, this.style);
    }
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
      final Map<String, Object> styleMap = this.style.toMap();
      map.putAll(styleMap);
    }
    return map;
  }
}
