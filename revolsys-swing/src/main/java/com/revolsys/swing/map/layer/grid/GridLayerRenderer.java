package com.revolsys.swing.map.layer.grid;

import java.awt.Color;
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

import org.jeometry.common.awt.WebColors;
import org.jeometry.coordinatesystem.model.unit.CustomUnits;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.gis.grid.RectangularMapGrid;
import com.revolsys.gis.grid.RectangularMapTile;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.Icons;
import com.revolsys.swing.component.Form;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.TextStyle;
import com.revolsys.swing.map.view.TextStyleViewRenderer;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.swing.menu.MenuFactory;

import tech.units.indriya.quantity.Quantities;

public class GridLayerRenderer extends AbstractLayerRenderer<GridLayer> {

  static {
    final MenuFactory menu = MenuFactory.getMenu(GridLayerRenderer.class);

    menu.addMenuItem("layer", "View/Edit Style", "palette", GridLayerRenderer::showProperties,
      false);
  }

  private static final Icon ICON = Icons.getIcon("style_geometry");

  private GeometryStyle geometryStyle = new GeometryStyle();

  private TextStyle textStyle;

  private GridLayerRenderer() {
    super("gridLayerRenderer", "Grid Style", ICON);
    this.geometryStyle = GeometryStyle.line(WebColors.LightGray);
    this.geometryStyle.setPolygonFillOpacity(0);

    this.textStyle = new TextStyle();
    this.textStyle.setTextName("[formattedName]");
    this.textStyle.setTextSize(Quantities.getQuantity(12, CustomUnits.PIXEL));
    this.textStyle.setTextPlacementType("auto");
    this.textStyle.setTextHorizontalAlignment("center");
    this.textStyle.setTextVerticalAlignment("middle");
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
  public void render(final ViewRenderer view, final GridLayer layer) {
    try {
      final double scaleForVisible = view.getScaleForVisible();
      if (layer.isVisible(scaleForVisible)) {
        final TextStyleViewRenderer textStyle = view.newTextStyleViewRenderer(this.textStyle);
        final BoundingBox boundingBox = view.getBoundingBox();
        final RectangularMapGrid grid = layer.getGrid();
        final List<RectangularMapTile> tiles = grid.getTiles(boundingBox);
        for (final RectangularMapTile tile : view.cancellable(tiles)) {
          final BoundingBox tileBoundingBox = tile.getBoundingBox();
          final BoundingBox intersectBoundingBox = boundingBox.bboxIntersection(tileBoundingBox);
          if (!intersectBoundingBox.isEmpty()) {
            view.drawBboxOutline(this.geometryStyle, tileBoundingBox);
            final String label = tile.getFormattedName();
            textStyle.drawText(label, tileBoundingBox.toRectangle());
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
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    addToMap(map, "geometryStyle", this.geometryStyle);
    addToMap(map, "textStyle", this.textStyle);
    return map;
  }
}
