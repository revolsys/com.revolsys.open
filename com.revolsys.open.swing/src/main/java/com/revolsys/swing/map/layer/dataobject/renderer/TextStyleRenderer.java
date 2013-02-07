package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.Measure;
import javax.measure.quantity.Length;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.CoordinatesWithOrientation;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.style.TextStyle;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class TextStyleRenderer extends AbstractDataObjectLayerRenderer {

  private static final AffineTransform NOOP_TRANSFORM = AffineTransform.getTranslateInstance(
    0, 0);

  public static String getLabel(final DataObject object, final TextStyle style) {
    final StringBuffer label = new StringBuffer();
    final String labelPattern = style.getTextName();
    final Matcher matcher = Pattern.compile("\\[([\\w.]+)\\]").matcher(
      labelPattern);
    while (matcher.find()) {
      final String propertyName = matcher.group(1);
      final Object value = object.getValueByPath(propertyName);
      String text;
      if (value == null) {
        text = "";
      } else {
        text = StringConverterRegistry.toString(value);
      }
      matcher.appendReplacement(label, text);
    }
    matcher.appendTail(label);

    return label.toString().trim();
  }

  public static CoordinatesWithOrientation getTextLocation(Viewport2D viewport,
    final Geometry geometry, final TextStyle style) {
    GeometryFactory viewportGeometryFactory = viewport.getGeometryFactory();
    if (viewportGeometryFactory != null) {
      GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);

      Coordinates point = null;

      double orientation = style.getTextOrientation();
      final String placementType = style.getTextPlacementType();
      final Matcher matcher = Pattern.compile("point\\((.*)\\)").matcher(
        placementType);
      final CoordinatesList points = CoordinatesListUtil.get(geometry);
      final int numPoints = points.size();
      if (numPoints > 1) {
        final boolean matches = matcher.matches();
        if (matches) {
          final String argument = matcher.group(1);
          int index;
          if (argument.matches("n(?:\\s*-\\s*(\\d+)\\s*)?")) {
            final String indexString = argument.replaceAll("[^0-9]+", "");
            index = numPoints - 1;
            if (indexString.length() > 0) {
              index -= Integer.parseInt(indexString);
            }
            if (index == 0) {
              index++;
            }
            point = ProjectionFactory.convert(points.get(index),
              geometryFactory, viewportGeometryFactory);
            final Coordinates p2 = ProjectionFactory.convert(
              points.get(index - 1), geometryFactory, viewportGeometryFactory);
            double angle = Math.toDegrees(p2.angle2d(point));
            orientation += angle;
          } else {
            index = Integer.parseInt(argument);
            if (index + 1 == numPoints) {
              index--;
            }
            point = ProjectionFactory.convert(points.get(index),
              geometryFactory, viewportGeometryFactory);
            final Coordinates p2 = ProjectionFactory.convert(
              points.get(index + 1), geometryFactory, viewportGeometryFactory);
            double angle = Math.toDegrees(point.angle2d(p2));
            orientation += angle;
          }

        } else if ("center".equals(placementType)) {
          if (geometry instanceof LineString && geometry.getNumPoints() > 1) {
            final double totalLength = geometry.getLength();
            final double centreLength = totalLength / 2;
            double currentLength = 0;
            for (int i = 1; i < numPoints && currentLength < centreLength; i++) {
              Coordinates p1 = points.get(i - 1);
              Coordinates p2 = points.get(i);
              final double segmentLength = p1.distance(p2);
              if (segmentLength + currentLength >= centreLength) {
                p1 = ProjectionFactory.convert(p1, geometryFactory,
                  viewportGeometryFactory);
                p2 = ProjectionFactory.convert(p2, geometryFactory,
                  viewportGeometryFactory);
                point = LineSegmentUtil.midPoint(viewportGeometryFactory, p1,
                  p2);
                double angle = Math.toDegrees(p1.angle2d(p2));
                orientation += angle;
              }
              currentLength += segmentLength;
            }
          }
        } else {  
         if (geometry instanceof Point) {
          point = CoordinatesUtil.get((Point)geometry);
         } else if (geometry instanceof LineString) {
           point = CoordinatesUtil.get((LineString)geometry);
         } else if (geometry instanceof Polygon) {
           point = CoordinatesUtil.get(geometry.getCentroid());
        }
          point = ProjectionFactory.convert(point,
            geometryFactory, viewportGeometryFactory);
        }

        if (point != null && viewport.getBoundingBox().contains(point)) {
          return new CoordinatesWithOrientation(point, orientation);
        }
      }
    }
    return null;
  }

  public static final void renderText(final Viewport2D viewport,
    final Graphics2D graphics, final DataObject object,
    final Geometry geometry, final TextStyle style) {
    final String label = getLabel(object, style);
    if (StringUtils.hasText(label)) {
      final CoordinatesWithOrientation point = getTextLocation(viewport,
        geometry, style);
      if (point != null) {
        double orientation = point.getOrientation();
        final boolean savedUseModelUnits = viewport.isUseModelCoordinates();
        final Paint paint = graphics.getPaint();
        viewport.setUseModelCoordinates(true, graphics);
        try {
          graphics.setBackground(Color.BLACK);
          style.setTextStyle(viewport, graphics);
          graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

          // TODO prevent upside down text
          final double x = point.getX();
          final double y = point.getY();

          graphics.translate(x, y);
          final AffineTransform savedTransform = graphics.getTransform();

          style.setTextStyle(viewport, graphics);

          if (orientation != 0) {
            graphics.rotate(Math.toRadians(orientation), 0, 0);
          }
          double dx = 0;
          double dy = 0;
          
          final Measure<Length> textDx = style.getTextDeltaX();
          dx += viewport.toDisplayValue(textDx);
          
          final Measure<Length> textDy = style.getTextDeltaY();
          dy += viewport.toDisplayValue(textDy);

          final FontMetrics fontMetrics = graphics.getFontMetrics();
          Rectangle2D bounds = fontMetrics.getStringBounds(label, graphics);
          final double width = bounds.getWidth();
          final double height = bounds.getHeight();
          dy += fontMetrics.getDescent();

          final String verticalAlignment = style.getTextVerticalAlignment();
          if ("top".equals(verticalAlignment)) {
            dy -= height;
          } else if ("middle".equals(verticalAlignment)) {
            dy -= height;
          }
          final String horizontalAlignment = style.getTextAlign();
          if ("right".equals(horizontalAlignment)) {
            dx -= width;
          } else if ("center".equals(horizontalAlignment)) {
            dx -= width / 2;
          }

          graphics.translate(dx, dy);
          graphics.scale(1, -1);
          if (Math.abs(orientation) > 90) {
            graphics.rotate(Math.PI, width / 2, -height/4 );
          }
          graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
         final double textHaloRadius = viewport.toDisplayValue(style.getTextHaloRadiusMeasure());
          if (textHaloRadius > 0) {
            final Stroke savedStroke = graphics.getStroke();
            final Stroke outlineStroke = new BasicStroke((float)textHaloRadius,
              BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
            graphics.setColor(style.getTextHaloFill());
            graphics.setStroke(outlineStroke);
            final Font font = graphics.getFont();
            final FontRenderContext fontRenderContext = graphics.getFontRenderContext();
            final TextLayout textLayout = new TextLayout(label, font,
              fontRenderContext);
            final Shape outlineShape = textLayout.getOutline(NOOP_TRANSFORM);
            graphics.draw(outlineShape);
            graphics.setStroke(savedStroke);
          }

          double pixel = viewport.getModelUnitsPerViewUnit();
          graphics.setColor(new Color(223,223,233,127));
         graphics.fill(new Rectangle2D.Double(bounds.getX()-pixel, bounds.getY(), width+2*pixel, height));
          
          graphics.setColor(style.getTextFill());
          graphics.drawString(label, (float)0, (float)0);
          graphics.setTransform(savedTransform);

        } finally {
          viewport.setUseModelCoordinates(savedUseModelUnits, graphics);
          graphics.setPaint(paint);
        }
      }
    }
  }

  private TextStyle style;

  public TextStyleRenderer(DataObjectLayer layer, TextStyle style) {
    super("textStyle", layer);
    this.style = style;
  }

  public TextStyleRenderer(final DataObjectLayer layer,
    final LayerRenderer<?> parent, final Map<String, Object> textStyle) {
    super("textStyle", layer, parent, textStyle);
    final Map<String, Object> style = getAllDefaults();
    style.putAll(textStyle);
    this.style = new TextStyle(style);
  }

  public TextStyle getStyle() {
    return style;
  }

  @Override
  protected void renderObject(final Viewport2D viewport,
    final Graphics2D graphics, final BoundingBox visibleArea,
    final DataObjectLayer layer, final DataObject object) {
    final Geometry geometry = object.getGeometryValue();
    renderText(viewport, graphics, object, geometry, style);
  }

  public void setStyle(final TextStyle style) {
    this.style = style;
  }

}
