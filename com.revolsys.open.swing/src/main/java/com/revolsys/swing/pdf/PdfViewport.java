package com.revolsys.swing.pdf;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.measure.Measure;
import javax.measure.quantity.Length;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.graphics.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.PDLineDashPattern;
import org.springframework.util.StringUtils;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.gis.cs.esri.EsriCsWktWriter;
import com.revolsys.gis.model.coordinates.PointWithOrientation;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.raster.PdfUtil;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.renderer.TextStyleRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.TextStyle;

public class PdfViewport extends Viewport2D implements AutoCloseable {

  private final PDDocument document;

  private final PDPage page;

  private final PDPageContentStream contentStream;

  private final Set<Float> alphaSet = new HashSet<>();

  private int styleId = 0;

  private final Map<GeometryStyle, String> styleNames = new HashMap<>();

  private final Canvas canvas = new Canvas();

  public PdfViewport(final PDDocument document, final PDPage page,
    final Project project, final int width, final int height,
    final BoundingBox boundingBox) throws IOException {
    super(project, width, height, boundingBox);
    this.document = document;
    this.page = page;
    this.contentStream = new PDPageContentStream(document, page);
    final COSArray viewports = PdfUtil.getArray(page.getCOSDictionary(), "VP");

    final COSDictionary viewport = new COSDictionary();
    viewports.add(viewport);
    viewport.setItem("Type", COSName.getPDFName("Viewport"));

    final COSArray bbox = new COSArray();
    PdfUtil.addInt(bbox, 0);
    PdfUtil.addInt(bbox, height);
    PdfUtil.addInt(bbox, width);
    PdfUtil.addInt(bbox, 0);

    viewport.setItem("BBox", bbox);
    viewport.setString("Name", "Main Map");

    final COSDictionary measure = PdfUtil.getDictionary(viewport, "Measure");
    measure.setName("Type", "Measure");
    measure.setName("Subtype", "GEO");

    final COSArray bounds = PdfUtil.intArray(0, 0, 0, 1, 1, 1, 1, 0);
    measure.setItem("Bounds", bounds);

    final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();

    final double minX = boundingBox.getMinX();
    final double maxX = boundingBox.getMaxX();
    final double minY = boundingBox.getMinY();
    final double maxY = boundingBox.getMaxY();
    final COSArray geoPoints = new COSArray();
    addPoint(geoPoints, geometryFactory, minX, maxY);
    addPoint(geoPoints, geometryFactory, minX, minY);
    addPoint(geoPoints, geometryFactory, maxX, minY);
    addPoint(geoPoints, geometryFactory, maxX, maxY);
    measure.setItem("GPTS", geoPoints);

    final COSArray lpts = PdfUtil.intArray(0, 0, 0, 1, 1, 1, 1, 0);
    measure.setItem("LPTS", lpts);

    final COSDictionary gcs = PdfUtil.getDictionary(measure, "GCS");
    if (geometryFactory.isProjected()) {
      gcs.setName("Type", "PROJCS");
    } else {
      gcs.setName("Type", "GEOGCS");
    }
    // final int srid = geometryFactory.getSrid();
    // gcs.setInt("EPSG", srid);
    final CoordinateSystem coordinateSystem = EsriCoordinateSystems.getCoordinateSystem(geometryFactory.getCoordinateSystem());
    final String wkt = EsriCsWktWriter.toWkt(coordinateSystem);
    gcs.setString("WKT", wkt);
  }

  private void addPoint(final COSArray geoPoints,
    final GeometryFactory geometryFactory, final double x, final double y) {
    final Point point = geometryFactory.point(x, y);
    final GeometryFactory geographicGeometryFactory = geometryFactory.getGeographicGeometryFactory();
    final Point geoPoint = point.convert(geographicGeometryFactory);
    final double lon = geoPoint.getX();
    final double lat = geoPoint.getY();
    PdfUtil.addFloat(geoPoints, lat);
    PdfUtil.addFloat(geoPoints, lon);
  }

  @Override
  public void close() throws IOException {
    this.contentStream.close();
  }

  @Override
  public void drawGeometry(final Geometry geometry, final GeometryStyle style) {
    try {
      this.contentStream.saveGraphicsState();
      setGeometryStyle(style);
      this.contentStream.setNonStrokingColor(style.getPolygonFill());
      this.contentStream.setStrokingColor(style.getLineColor());

      for (Geometry part : geometry.geometries()) {
        part = part.convert(getGeometryFactory());
        if (part instanceof LineString) {
          final LineString line = (LineString)part;

          drawLine(line);
          this.contentStream.stroke();
        } else if (part instanceof Polygon) {
          final Polygon polygon = (Polygon)part;

          int i = 0;
          for (final LinearRing ring : polygon.rings()) {
            if (i == 0) {
              if (ring.isClockwise()) {
                drawLineReverse(ring);
              } else {
                drawLine(ring);
              }
            } else {
              if (ring.isCounterClockwise()) {
                drawLineReverse(ring);
              } else {
                drawLine(ring);
              }
            }
            this.contentStream.closeSubPath();
            i++;
          }
          this.contentStream.fill(PathIterator.WIND_NON_ZERO);
          for (final LinearRing ring : polygon.rings()) {

            drawLine(ring);
            this.contentStream.stroke();
          }
        }
      }

    } catch (final IOException e) {
      e.printStackTrace();
    } finally {
      try {
        this.contentStream.restoreGraphicsState();
      } catch (final IOException e) {
      }
    }
  }

  private void drawLine(final LineString line) throws IOException {
    for (int i = 0; i < line.getVertexCount(); i++) {
      final double modelX = line.getX(i);
      final double modelY = line.getY(i);
      final double[] viewCoordinates = toViewCoordinates(modelX, modelY);
      final float viewX = (float)viewCoordinates[0];
      final float viewY = (float)(getViewHeightPixels() - viewCoordinates[1]);
      if (i == 0) {
        this.contentStream.moveTo(viewX, viewY);
      } else {
        this.contentStream.lineTo(viewX, viewY);
      }
    }
  }

  private void drawLineReverse(final LineString line) throws IOException {
    final int toVertexIndex = line.getVertexCount() - 1;
    for (int i = toVertexIndex; i >= 0; i--) {
      final double modelX = line.getX(i);
      final double modelY = line.getY(i);
      final double[] viewCoordinates = toViewCoordinates(modelX, modelY);
      final float viewX = (float)viewCoordinates[0];
      final float viewY = (float)(getViewHeightPixels() - viewCoordinates[1]);
      if (i == toVertexIndex) {
        this.contentStream.moveTo(viewX, viewY);
      } else {
        this.contentStream.lineTo(viewX, viewY);
      }
    }
  }

  @Override
  public void drawText(final LayerRecord object, final Geometry geometry,
    final TextStyle style) {
    try {
      final String label = TextStyleRenderer.getLabel(object, style);
      if (StringUtils.hasText(label) && geometry != null) {
        final PointWithOrientation point = TextStyleRenderer.getTextLocation(
          this, geometry, style);
        if (point != null) {
          final double orientation = point.getOrientation();

          this.contentStream.saveGraphicsState();
          try {
            // style.setTextStyle(viewport, graphics);

            final double x = point.getX();
            final double y = point.getY();
            final double[] location = toViewCoordinates(x, y);

            // style.setTextStyle(viewport, graphics);

            final Measure<Length> textDx = style.getTextDx();
            double dx = Viewport2D.toDisplayValue(this, textDx);

            final Measure<Length> textDy = style.getTextDy();
            double dy = -Viewport2D.toDisplayValue(this, textDy);
            final Font font = style.getFont(this);
            final FontMetrics fontMetrics = this.canvas.getFontMetrics(font);

            double maxWidth = 0;
            final String[] lines = label.split("[\\r\\n]");
            for (final String line : lines) {
              final Rectangle2D bounds = fontMetrics.getStringBounds(line,
                this.canvas.getGraphics());
              final double width = bounds.getWidth();
              maxWidth = Math.max(width, maxWidth);
            }
            final int descent = fontMetrics.getDescent();
            final int ascent = fontMetrics.getAscent();
            final int leading = fontMetrics.getLeading();
            final double maxHeight = lines.length * (ascent + descent)
              + (lines.length - 1) * leading;
            final String verticalAlignment = style.getTextVerticalAlignment();
            if ("top".equals(verticalAlignment)) {
            } else if ("middle".equals(verticalAlignment)) {
              dy -= maxHeight / 2;
            } else {
              dy -= maxHeight;
            }

            String horizontalAlignment = style.getTextHorizontalAlignment();
            double screenX = location[0];
            double screenY = getViewHeightPixels() - location[1];
            final String textPlacementType = style.getTextPlacementType();
            if ("auto".equals(textPlacementType)) {
              if (screenX < 0) {
                screenX = 1;
                dx = 0;
                horizontalAlignment = "left";
              }
              final int viewWidth = getViewWidthPixels();
              if (screenX + maxWidth > viewWidth) {
                screenX = (int)(viewWidth - maxWidth - 1);
                dx = 0;
                horizontalAlignment = "left";
              }
              if (screenY < maxHeight) {
                screenY = 1;
                dy = 0;
              }
              final int viewHeight = getViewHeightPixels();
              if (screenY > viewHeight) {
                screenY = viewHeight - 1 - maxHeight;
                dy = 0;
              }
            }
            AffineTransform transform = new AffineTransform();
            transform.translate(screenX, screenY);
            if (orientation != 0) {
              transform.rotate(-Math.toRadians(orientation), 0, 0);
            }
            transform.translate(dx, dy);

            for (int i = 0; i < lines.length; i++) {
              final String line = lines[i];
              transform.translate(0, ascent);
              final AffineTransform lineTransform = new AffineTransform(
                transform);
              final Rectangle2D bounds = fontMetrics.getStringBounds(line,
                this.canvas.getGraphics());
              final double width = bounds.getWidth();
              final double height = bounds.getHeight();

              if ("right".equals(horizontalAlignment)) {
                transform.translate(-width, 0);
              } else if ("center".equals(horizontalAlignment)) {
                transform.translate(-width / 2, 0);
              }
              transform.translate(dx, 0);

              transform.scale(1, 1);
              if (Math.abs(orientation) > 90) {
                transform.rotate(Math.PI, maxWidth / 2, -height / 4);
              }
              /*
               * final double textHaloRadius = Viewport2D.toDisplayValue(this,
               * style.getTextHaloRadius()); if (textHaloRadius > 0) {
               * graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
               * RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB); final Stroke
               * savedStroke = graphics.getStroke(); final Stroke outlineStroke
               * = new BasicStroke( (float)textHaloRadius, BasicStroke.CAP_BUTT,
               * BasicStroke.JOIN_BEVEL);
               * graphics.setColor(style.getTextHaloFill());
               * graphics.setStroke(outlineStroke); final Font font =
               * graphics.getFont(); final FontRenderContext fontRenderContext =
               * graphics.getFontRenderContext(); final TextLayout textLayout =
               * new TextLayout(line, font, fontRenderContext); final Shape
               * outlineShape =
               * textLayout.getOutline(TextStyleRenderer.NOOP_TRANSFORM);
               * graphics.draw(outlineShape); graphics.setStroke(savedStroke); }
               */
              final Color textBoxColor = style.getTextBoxColor();
              if (textBoxColor != null) {
                this.contentStream.setNonStrokingColor(textBoxColor);
                final double cornerSize = Math.max(height / 2, 5);
                // final RoundRectangle2D.Double box = new
                // RoundRectangle2D.Double(
                // bounds.getX() - 3, bounds.getY() - 1, width + 6, height + 2,
                // cornerSize, cornerSize);
                this.contentStream.fillRect((float)bounds.getX() - 3,
                  (float)bounds.getY() - 1, (float)width + 6, (float)height + 2);
              }
              this.contentStream.setNonStrokingColor(style.getTextFill());

              this.contentStream.beginText();
              final InputStream fontStream = PDDocument.class.getResourceAsStream("/org/apache/pdfbox/resources/ttf/ArialMT.ttf");
              final PDFont pdfFont = PDTrueTypeFont.loadTTF(this.document,
                fontStream);

              this.contentStream.setFont(pdfFont, font.getSize2D());
              this.contentStream.setTextMatrix(transform);
              this.contentStream.drawString(line);
              this.contentStream.endText();

              transform = lineTransform;
              transform.translate(0, leading + descent);
            }

          } finally {
            this.contentStream.restoreGraphicsState();
          }
        }
      }
    } catch (final IOException e) {
      throw new RuntimeException("Unable to write PDF", e);
    }
  }

  @Override
  public boolean isHidden(final AbstractRecordLayer layer,
    final LayerRecord record) {
    return false;
  }

  private void setGeometryStyle(final GeometryStyle style) throws IOException {
    String styleName = this.styleNames.get(style);
    if (styleName == null) {
      styleName = "rgStyle" + this.styleId++;

      final PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();

      final int lineOpacity = style.getLineOpacity();
      if (lineOpacity != 255) {
        graphicsState.setStrokingAlphaConstant(lineOpacity / 255f);
      }

      final Measure<Length> lineWidth = style.getLineWidth();
      graphicsState.setLineWidth((float)toDisplayValue(lineWidth));

      final List<Measure<Length>> lineDashArray = style.getLineDashArray();
      if (lineDashArray != null && !lineDashArray.isEmpty()) {
        int size = lineDashArray.size();
        if (size == 1) {
          size++;
        }
        final float[] dashArray = new float[size];

        for (int i = 0; i < dashArray.length; i++) {
          if (i < lineDashArray.size()) {
            final Measure<Length> dash = lineDashArray.get(i);
            dashArray[i] = (float)toDisplayValue(dash);
          } else {
            dashArray[i] = dashArray[i - 1];
          }
        }
        final int offset = (int)toDisplayValue(style.getLineDashOffset());
        final COSArray dashCosArray = new COSArray();
        dashCosArray.setFloatArray(dashArray);
        final PDLineDashPattern pattern = new PDLineDashPattern(dashCosArray,
          offset);
        graphicsState.setLineDashPattern(pattern);
      }
      switch (style.getLineCap()) {
        case BUTT:
          graphicsState.setLineCapStyle(0);
        break;
        case ROUND:
          graphicsState.setLineCapStyle(1);
        break;
        case SQUARE:
          graphicsState.setLineCapStyle(2);
        break;
      }

      switch (style.getLineJoin()) {
        case MITER:
          graphicsState.setLineJoinStyle(0);
        break;
        case ROUND:
          graphicsState.setLineJoinStyle(1);
        break;
        case BEVEL:
          graphicsState.setLineJoinStyle(2);
        break;
      }

      final int polygonFillOpacity = style.getPolygonFillOpacity();
      if (polygonFillOpacity != 255) {
        graphicsState.setNonStrokingAlphaConstant(polygonFillOpacity / 255f);
      }

      final PDResources resources = this.page.findResources();
      Map<String, PDExtendedGraphicsState> graphicsStateDictionary = resources.getGraphicsStates();
      if (graphicsStateDictionary == null) {
        graphicsStateDictionary = new TreeMap<>();
      }
      graphicsStateDictionary.put(styleName, graphicsState);
      resources.setGraphicsStates(graphicsStateDictionary);

      this.styleNames.put(style, styleName);
    }
    this.contentStream.appendRawCommands("/" + styleName + " gs\n");
  }
}
