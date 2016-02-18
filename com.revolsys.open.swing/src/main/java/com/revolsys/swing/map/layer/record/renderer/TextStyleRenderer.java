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
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.swing.Icon;

import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.coordinates.PointWithOrientation;
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
  private static final Icon ICON = Icons.getIcon("style_text");

  public static final AffineTransform NOOP_TRANSFORM = AffineTransform.getTranslateInstance(0, 0);

  public static String getLabel(final Record record, final TextStyle style) {
    if (record == null) {
      return "Text";
    } else {
      final StringBuffer label = new StringBuffer();
      final String labelPattern = style.getTextName();
      final Matcher matcher = Pattern.compile("\\[([\\w.]+)\\]").matcher(labelPattern);
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
    final Record object, final Geometry geometry, final TextStyle style) {
    final String label = getLabel(object, style);
    renderText(viewport, graphics, label, geometry, style);
  }

  public static void renderText(final Viewport2D viewport, final Graphics2D graphics,
    final String label, final Geometry geometry, final TextStyle style) {
    if (Property.hasValue(label) && geometry != null || viewport == null) {
      final String textPlacementType = style.getTextPlacementType();
      final PointWithOrientation point = getPointWithOrientation(viewport, geometry,
        textPlacementType);
      if (point != null) {
        double orientation;
        final String orientationType = style.getTextOrientationType();
        if ("none".equals(orientationType)) {
          orientation = 0;
        } else {
          orientation = point.getOrientation();
        }
        orientation += style.getTextOrientation();

        final Paint paint = graphics.getPaint();
        final Composite composite = graphics.getComposite();
        try {
          graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
          graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
          style.setTextStyle(viewport, graphics);

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
            if (orientation > 270) {
              orientation -= 360;
            }
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

  private TextStyle style;

  public TextStyleRenderer(final AbstractRecordLayer layer, final LayerRenderer<?> parent) {
    this(layer, parent, Collections.<String, Object> emptyMap());
  }

  public TextStyleRenderer(final AbstractRecordLayer layer, final LayerRenderer<?> parent,
    final Map<String, Object> textStyle) {
    super("textStyle", "Text Style", layer, parent, textStyle);
    setStyle(new TextStyle(textStyle));
    setIcon(ICON);
  }

  @Override
  public TextStyleRenderer clone() {
    final TextStyleRenderer clone = (TextStyleRenderer)super.clone();
    clone.setStyle(this.style.clone());
    return clone;
  }

  public TextStyle getStyle() {
    return this.style;
  }

  @Override
  public Form newStylePanel() {
    return new TextStylePanel(this);
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

  public void setStyle(final TextStyle style) {
    if (this.style != null) {
      this.style.removePropertyChangeListener(this);
    }
    this.style = style;
    if (this.style != null) {
      this.style.addPropertyChangeListener(this);
    }
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
