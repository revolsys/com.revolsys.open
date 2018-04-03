package com.revolsys.swing.map.layer.grid;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.cs.unit.CustomUnits;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.gis.grid.RectangularMapGrid;
import com.revolsys.gis.grid.RectangularMapTile;
import com.revolsys.io.BaseCloseable;
import com.revolsys.swing.Icons;
import com.revolsys.swing.component.Form;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.TextStyle;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.Menus;
import com.revolsys.util.Cancellable;

import tec.uom.se.quantity.Quantities;

public class GridLayerRenderer extends AbstractLayerRenderer<GridLayer> {

  static {
    final MenuFactory menu = MenuFactory.getMenu(GridLayerRenderer.class);

    Menus.addMenuItem(menu, "layer", "View/Edit Style", "palette",
      GridLayerRenderer::showProperties, false);
  }

  private static final Icon ICON = Icons.getIcon("style_geometry");

  private GeometryStyle geometryStyle = new GeometryStyle();

  private TextStyle textStyle;

  private GridLayerRenderer() {
    super("gridLayerRenderer", "Grid Style");
    this.geometryStyle = GeometryStyle.line(WebColors.LightGray);
    this.geometryStyle.setPolygonFillOpacity(0);

    this.textStyle = new TextStyle();
    this.textStyle.setTextName("[formattedName]");
    this.textStyle.setTextSize(Quantities.getQuantity(12, CustomUnits.PIXEL));
    this.textStyle.setTextPlacementType("auto");
    this.textStyle.setTextHorizontalAlignment("center");
    this.textStyle.setTextVerticalAlignment("middle");

    setIcon(ICON);
  }

  public GridLayerRenderer(final GridLayer layer) {
    this();
    setLayer(layer);
  }

  @SuppressWarnings("unchecked")
  public GridLayerRenderer(final Map<String, ? extends Object> properties) {
    this();
    setProperties(properties);
    final Map<String, ? extends Object> geometryStyleProperties = (Map<String, ? extends Object>)properties
      .get("geometryStyle");
    this.geometryStyle.setProperties(geometryStyleProperties);
    final Map<String, ? extends Object> textStyleProperties = (Map<String, ? extends Object>)properties
      .get("textStyle");
    this.geometryStyle.setProperties(textStyleProperties);
  }

  @Override
  public GridLayerRenderer clone() {
    final GridLayerRenderer clone = (GridLayerRenderer)super.clone();
    if (this.geometryStyle != null) {
      clone.setGeometryStyle(this.geometryStyle.clone());
    }
    if (this.textStyle != null) {
      clone.setTextStyle(this.textStyle.clone());
    }
    return clone;
  }

  public GeometryStyle getGeometryStyle() {
    return this.geometryStyle;
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

  public TextStyle getTextStyle() {
    return this.textStyle;
  }

  public Icon newIcon() {
    final GeometryStyle geometryStyle = getGeometryStyle();

    final BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D graphics = image.createGraphics();

    this.textStyle.drawTextIcon(graphics, 8);

    final Color color = geometryStyle.getLineColor();
    graphics.setColor(color);

    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    final Shape shape = new Rectangle2D.Double(0, 0, 15, 15);
    graphics.draw(shape);

    graphics.dispose();
    return new ImageIcon(image);
  }

  @Override
  public Form newStylePanel() {
    return new GridLayerStylePanel(this);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source == this.geometryStyle) {
      refreshIcon();
    } else if (source == this.textStyle) {
      refreshIcon();
    }
    super.propertyChange(event);
  }

  protected void refreshIcon() {
    final Icon icon = newIcon();
    setIcon(icon);
  }

  @Override
  public void render(final Viewport2D viewport, final Cancellable cancellable,
    final GridLayer layer) {
    try {
      final double scaleForVisible = viewport.getScaleForVisible();
      if (layer.isVisible(scaleForVisible)) {
        final BoundingBox boundingBox = viewport.getBoundingBox();
        final RectangularMapGrid grid = layer.getGrid();
        final List<RectangularMapTile> tiles = grid.getTiles(boundingBox);
        final Graphics2D graphics = viewport.getGraphics();
        if (graphics != null) {
          final Font font = graphics.getFont();
          for (final RectangularMapTile tile : cancellable.cancellable(tiles)) {
            final BoundingBox tileBoundingBox = tile.getBoundingBox();
            final BoundingBox intersectBoundingBox = boundingBox.intersection(tileBoundingBox);
            if (!intersectBoundingBox.isEmpty()) {

              final GeometryFactory geometryFactory = viewport.getGeometryFactory();
              final Polygon polygon = tile.getPolygon(geometryFactory, 50);
              try (
                BaseCloseable transformCloseable = viewport.setUseModelCoordinates(graphics,
                  true)) {
                viewport.drawGeometryOutline(polygon, this.geometryStyle);
              }
              try (
                BaseCloseable transformClosable = viewport.setUseModelCoordinates(false)) {
                viewport.drawText(tile, polygon, this.textStyle);
              }
            }
            graphics.setFont(font);
          }
        }
      }
    } catch (final IllegalArgumentException e) {
    }
  }

  public void setGeometryStyle(final GeometryStyle geometryStyle) {
    this.geometryStyle = geometryStyle;
    if (this.geometryStyle != null) {
      this.geometryStyle.removePropertyChangeListener(this);
    }
    this.geometryStyle = geometryStyle;
    if (this.geometryStyle != null) {
      this.geometryStyle.addPropertyChangeListener(this);
    }
    firePropertyChange("geometryStyle", null, geometryStyle);
    refreshIcon();
  }

  public void setTextStyle(final TextStyle textStyle) {
    this.textStyle = textStyle;
    if (this.textStyle != null) {
      this.textStyle.removePropertyChangeListener(this);
    }
    this.textStyle = textStyle;
    if (this.textStyle != null) {
      this.textStyle.addPropertyChangeListener(this);
    }
    firePropertyChange("textStyle", null, textStyle);
    refreshIcon();
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    addToMap(map, "geometryStyle", this.geometryStyle);
    addToMap(map, "textStyle", this.textStyle);
    return map;
  }
}
