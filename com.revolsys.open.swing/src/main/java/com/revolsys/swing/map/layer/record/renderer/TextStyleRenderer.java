package com.revolsys.swing.map.layer.record.renderer;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.collection.map.MapEx;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.impl.PointDoubleXYOrientation;
import com.revolsys.io.BaseCloseable;
import com.revolsys.record.Record;
import com.revolsys.swing.Icons;
import com.revolsys.swing.component.Form;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.style.TextStyle;
import com.revolsys.swing.map.layer.record.style.panel.TextStylePanel;
import com.revolsys.util.Property;

public class TextStyleRenderer extends AbstractRecordLayerRenderer {
  private static final Pattern FIELD_PATTERN = Pattern.compile("\\[([\\w.]+)\\]");

  private static final Icon ICON = Icons.getIcon("style_text");

  public static final AffineTransform NOOP_TRANSFORM = AffineTransform.getTranslateInstance(0, 0);

  public static String getLabel(final Record record, final TextStyle style) {
    if (record == null) {
      return "Text";
    } else {
      final StringBuffer label = new StringBuffer();
      final String labelPattern = style.getTextName();
      final Matcher matcher = FIELD_PATTERN.matcher(labelPattern);
      while (matcher.find()) {
        final String propertyName = matcher.group(1);
        String text = "";
        try {
          final Object value = record.getValueByPath(propertyName);
          if (value != null) {
            text = DataTypes.toString(value);
          }
        } catch (final Throwable e) {
        }
        matcher.appendReplacement(label, text);
      }
      matcher.appendTail(label);

      return label.toString().trim();
    }
  }

  public static final void renderText(final Viewport2D viewport, final Graphics2D graphics,
    final Record record, final Geometry geometry, final TextStyle style) {
    final String label = getLabel(record, style);
    if (geometry != null) {
      for (final Geometry part : geometry.geometries()) {
        renderText(viewport, graphics, label, part, style);
      }
    }
  }

  public static void renderText(final Viewport2D viewport, final Graphics2D graphics,
    final String label, final Geometry geometry, final TextStyle style) {
    if (Property.hasValue(label) && geometry != null || viewport == null) {
      final String textPlacementType = style.getTextPlacementType();
      final PointDoubleXYOrientation point = getPointWithOrientation(viewport, geometry,
        textPlacementType);
      if (point != null) {
        double orientation;
        final String orientationType = style.getTextOrientationType();
        if ("none".equals(orientationType)) {
          orientation = 0;
        } else {
          orientation = point.getOrientation();
          if (orientation > 270) {
            orientation -= 360;
          }
        }
        orientation += style.getTextOrientation();

        final Paint paint = graphics.getPaint();
        final Composite composite = graphics.getComposite();
        try {
          graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
          graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

          final double x = point.getX();
          final double y = point.getY();
          final double[] location;
          if (viewport == null) {
            location = new double[] {
              x, y
            };
          } else {
            location = viewport.toViewCoordinates(x, y);
          }

          final AffineTransform savedTransform = graphics.getTransform();

          style.setTextStyle(viewport, graphics);

          final Measure<Length> textDx = style.getTextDx();
          double dx = Viewport2D.toDisplayValue(viewport, textDx);

          final Measure<Length> textDy = style.getTextDy();
          double dy = -Viewport2D.toDisplayValue(viewport, textDy);

          final FontMetrics fontMetrics = graphics.getFontMetrics();

          double maxWidth = 0;
          final String[] lines = label.split("[\\r\\n]");
          for (final String line : lines) {
            final Rectangle2D bounds = fontMetrics.getStringBounds(line, graphics);
            final double width = bounds.getWidth();
            maxWidth = Math.max(width, maxWidth);
          }
          final int descent = fontMetrics.getDescent();
          final int ascent = fontMetrics.getAscent();
          final int leading = fontMetrics.getLeading();
          final double maxHeight = lines.length * (ascent + descent) + (lines.length - 1) * leading;
          final String verticalAlignment = style.getTextVerticalAlignment();
          if ("top".equals(verticalAlignment)) {
          } else if ("middle".equals(verticalAlignment)) {
            dy -= maxHeight / 2;
          } else {
            dy -= maxHeight;
          }

          String horizontalAlignment = style.getTextHorizontalAlignment();
          double screenX = location[0];
          double screenY = location[1];
          final String textPlacement = textPlacementType;
          if ("auto".equals(textPlacement) && viewport != null) {
            if (screenX < 0) {
              screenX = 1;
              dx = 0;
              horizontalAlignment = "left";
            }
            final int viewWidth = viewport.getViewWidthPixels();
            if (screenX + maxWidth > viewWidth) {
              screenX = (int)(viewWidth - maxWidth - 1);
              dx = 0;
              horizontalAlignment = "left";
            }
            if (screenY < maxHeight) {
              screenY = 1;
              dy = 0;
            }
            final int viewHeight = viewport.getViewHeightPixels();
            if (screenY > viewHeight) {
              screenY = viewHeight - 1 - maxHeight;
              dy = 0;
            }
          }
          graphics.translate(screenX, screenY);
          if (orientation != 0) {
            graphics.rotate(-Math.toRadians(orientation), 0, 0);
          }
          graphics.translate(dx, dy);
          for (final String line : lines) {
            graphics.translate(0, ascent);
            final AffineTransform lineTransform = graphics.getTransform();
            final Rectangle2D bounds = fontMetrics.getStringBounds(line, graphics);
            final double width = bounds.getWidth();
            final double height = bounds.getHeight();

            if ("right".equals(horizontalAlignment)) {
              graphics.translate(-width, 0);
            } else if ("center".equals(horizontalAlignment) || "auto".equals(horizontalAlignment)) {
              graphics.translate(-width / 2, 0);
            }
            graphics.translate(dx, 0);

            graphics.scale(1, 1);
            if (Math.abs(orientation) > 90) {
              graphics.rotate(Math.PI, maxWidth / 2, -height / 4);
            }

            final int textBoxOpacity = style.getTextBoxOpacity();
            final Color textBoxColor = style.getTextBoxColor();
            if (textBoxOpacity > 0 && textBoxColor != null) {
              graphics.setPaint(textBoxColor);
              final double cornerSize = Math.max(height / 2, 5);
              final RoundRectangle2D.Double box = new RoundRectangle2D.Double(bounds.getX() - 3,
                bounds.getY() - 1, width + 6, height + 2, cornerSize, cornerSize);
              graphics.fill(box);
            }

            final double radius = style.getTextHaloRadius();
            final Unit<Length> unit = style.getTextSizeUnit();
            final double textHaloRadius = Viewport2D.toDisplayValue(viewport,
              Measure.valueOf(radius, unit));
            if (textHaloRadius > 0) {
              final Stroke savedStroke = graphics.getStroke();
              final Stroke outlineStroke = new BasicStroke((float)(textHaloRadius + 1),
                BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
              graphics.setColor(style.getTextHaloFill());
              graphics.setStroke(outlineStroke);
              final Font font = graphics.getFont();
              final FontRenderContext fontRenderContext = graphics.getFontRenderContext();
              final TextLayout textLayout = new TextLayout(line, font, fontRenderContext);
              final Shape outlineShape = textLayout.getOutline(NOOP_TRANSFORM);
              graphics.draw(outlineShape);
              graphics.setStroke(savedStroke);
            }

            graphics.setColor(style.getTextFill());
            if (textBoxOpacity > 0 && textBoxOpacity < 255) {
              graphics.setComposite(AlphaComposite.SrcOut);
              graphics.drawString(line, (float)0, (float)0);
              graphics.setComposite(AlphaComposite.DstOver);
              graphics.drawString(line, (float)0, (float)0);

            } else {
              graphics.setComposite(AlphaComposite.SrcOver);
              graphics.drawString(line, (float)0, (float)0);
            }

            graphics.setTransform(lineTransform);
            graphics.translate(0, leading + descent);
          }
          graphics.setTransform(savedTransform);

        } finally {
          graphics.setPaint(paint);
          graphics.setComposite(composite);
        }
      }
    }
  }

  private TextStyle style = new TextStyle();

  public TextStyleRenderer(final AbstractRecordLayer layer, final LayerRenderer<?> parent) {
    super("textStyle", "Text Style", layer, parent);
    setIcon(newIcon());
  }

  public TextStyleRenderer(final AbstractRecordLayer layer, final TextStyle textStyle) {
    super("textStyle", "Text Style");
    setStyle(textStyle);
  }

  public TextStyleRenderer(final Map<String, ? extends Object> properties) {
    super("textStyle", "Text Style");
    setIcon(ICON);
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
    Icon icon;
    final BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D graphics = image.createGraphics();

    double orientation = this.style.getTextOrientation();

    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
      RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

    final String textFaceName = this.style.getTextFaceName();
    final Font font = new Font(textFaceName, 0, 12);
    graphics.setFont(font);
    final FontMetrics fontMetrics = graphics.getFontMetrics();

    int x = 0;
    int y = 15;
    final String text = "A";
    final Rectangle2D bounds = fontMetrics.getStringBounds(text, graphics);
    final double width = bounds.getWidth();
    final double height = fontMetrics.getAscent();
    final String horizontalAlignment = this.style.getTextHorizontalAlignment();
    if ("right".equals(horizontalAlignment)) {
      x = 15 - (int)width;
    } else if ("center".equals(horizontalAlignment) || "auto".equals(horizontalAlignment)) {
      x = 8 - (int)(width / 2);
    }
    final String verticalAlignment = this.style.getTextVerticalAlignment();
    if ("top".equals(verticalAlignment)) {
      y = (int)height;
    } else if ("middle".equals(verticalAlignment) || "auto".equals(verticalAlignment)) {
      y = 7 + (int)(height / 2);
    }
    if (orientation != 0) {
      if (orientation > 270) {
        orientation -= 360;
      }
      graphics.rotate(-Math.toRadians(orientation), 8, 8);
    }

    final int textBoxOpacity = this.style.getTextBoxOpacity();
    final Color textBoxColor = this.style.getTextBoxColor();
    if (textBoxOpacity > 0 && textBoxColor != null) {
      graphics.setPaint(textBoxColor);
      final RoundRectangle2D.Double box = new RoundRectangle2D.Double(0, 0, 16, 16, 5, 5);
      graphics.fill(box);
    }

    final double textHaloRadius = this.style.getTextHaloRadius();
    if (textHaloRadius > 0) {
      final Stroke savedStroke = graphics.getStroke();
      final Stroke outlineStroke = new BasicStroke((float)(textHaloRadius + 1),
        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
      graphics.setColor(this.style.getTextHaloFill());
      graphics.setStroke(outlineStroke);
      final FontRenderContext fontRenderContext = graphics.getFontRenderContext();
      final TextLayout textLayout = new TextLayout(text, font, fontRenderContext);
      final Shape outlineShape = textLayout.getOutline(NOOP_TRANSFORM);
      graphics.draw(outlineShape);
      graphics.setStroke(savedStroke);
    }

    graphics.setColor(this.style.getTextFill());
    if (textBoxOpacity > 0 && textBoxOpacity < 255) {
      graphics.setComposite(AlphaComposite.SrcOut);
      graphics.drawString(text, x, y);
      graphics.setComposite(AlphaComposite.DstOver);
      graphics.drawString(text, x, y);
    } else {
      graphics.setComposite(AlphaComposite.SrcOver);
      graphics.drawString(text, x, y);
    }
    graphics.dispose();
    icon = new ImageIcon(image);
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
  public void renderRecord(final Viewport2D viewport, final BoundingBox visibleArea,
    final AbstractLayer layer, final LayerRecord record) {
    final Geometry geometry = record.getGeometry();
    if (Property.hasValue(geometry)) {
      try (
        BaseCloseable transformClosable = viewport.setUseModelCoordinates(false)) {
        viewport.drawText(record, geometry, this.style);
      }
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
  public MapEx toMap() {
    final MapEx map = super.toMap();
    if (this.style != null) {
      final Map<String, Object> styleMap = this.style.toMap();
      map.putAll(styleMap);
    }
    return map;
  }
}
