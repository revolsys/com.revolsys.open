package com.revolsys.swing.map.view.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentGroup;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.BaseCloseable;
import com.revolsys.raster.io.format.pdf.PdfUtil;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.Project;

public class PdfViewport extends Viewport2D implements BaseCloseable {

  public static PDOptionalContentGroup getPDOptionalContentGroup(final Layer layer) {
    return layer.getProperty(PDOptionalContentGroup.class.getName());
  }

  private final PDPageContentStream contentStream;

  private final PDDocument document;

  private final Map<String, PDFont> fonts = new HashMap<>();

  private final PDPage page;

  public PdfViewport(final PDDocument document, final PDPage page, final Project project,
    final int width, final int height, final BoundingBox boundingBox) throws IOException {
    super(project, width, height, boundingBox);
    this.document = document;
    this.page = page;
    this.contentStream = new PDPageContentStream(document, page);
    final COSDictionary pageDictionary = page.getCOSObject();
    final COSArray viewports = PdfUtil.getArray(pageDictionary, "VP");

    final COSDictionary viewport = new COSDictionary();
    viewports.add(viewport);
    viewport.setName(COSName.TYPE, "Viewport");

    final COSArray bbox = PdfUtil.intArray(0, 0, width, height);
    viewport.setItem(COSName.BBOX, bbox);

    viewport.setString(COSName.NAME, "Main Map");

    final COSDictionary measure = PdfUtil.getDictionary(viewport, "Measure");
    measure.setName(COSName.TYPE, "Measure");
    measure.setName(COSName.SUBTYPE, "GEO");

    final COSArray bounds = PdfUtil.intArray(0, 0, 0, 1, 1, 1, 1, 0);
    measure.setItem("Bounds", bounds);

    final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();

    final double minX = boundingBox.getMinX();
    final double maxX = boundingBox.getMaxX();
    final double minY = boundingBox.getMinY();
    final double maxY = boundingBox.getMaxY();
    final COSArray geoPoints = new COSArray();
    addPoint(geoPoints, geometryFactory, minX, minY);
    addPoint(geoPoints, geometryFactory, minX, maxY);
    addPoint(geoPoints, geometryFactory, maxX, maxY);
    addPoint(geoPoints, geometryFactory, maxX, minY);
    measure.setItem("GPTS", geoPoints);

    final COSArray lpts = PdfUtil.intArray(0, 0, 0, 1, 1, 1, 1, 0);
    measure.setItem("LPTS", lpts);

    final COSDictionary gcs = PdfUtil.getDictionary(measure, "GCS");
    if (geometryFactory.isProjected()) {
      gcs.setName(COSName.TYPE, "PROJCS");
    } else {
      gcs.setName(COSName.TYPE, "GEOGCS");
    }
    final int srid = geometryFactory.getHorizontalCoordinateSystemId();
    gcs.setInt("EPSG", srid);
    String wkt = geometryFactory.toWktCs();
    wkt = wkt.replaceAll("false_easting", "False_Easting");
    wkt = wkt.replaceAll("false_northing", "False_Northing");
    wkt = wkt.replaceAll("Popular_Visualisation_Pseudo_Mercator", "Mercator");
    gcs.setString("WKT", wkt);
  }

  private void addPoint(final COSArray geoPoints, final GeometryFactory geometryFactory,
    final double x, final double y) {
    final Point point = geometryFactory.point(x, y);
    final GeometryFactory geographicGeometryFactory = geometryFactory
      .getGeographicGeometryFactory();
    final Point geoPoint = point.convertGeometry(geographicGeometryFactory);
    final double lon = geoPoint.getX();
    final double lat = geoPoint.getY();
    PdfUtil.addFloat(geoPoints, lat);
    PdfUtil.addFloat(geoPoints, lon);
  }

  @Override
  public void close() {
    try {
      this.contentStream.close();
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  public PDDocument getDocument() {
    return this.document;
  }

  protected PDFont getFont(final String path) throws IOException {
    PDFont font = this.fonts.get(path);
    if (font == null) {
      final InputStream fontStream = PDDocument.class
        .getResourceAsStream("/org/apache/pdfbox/resources/ttf/ArialMT.ttf");
      font = PDTrueTypeFont.loadTTF(this.document, fontStream);
      this.fonts.put("/org/apache/pdfbox/resources/ttf/ArialMT.ttf", font);
    }
    return font;
  }

  public PDPage getPage() {
    return this.page;
  }

  @Override
  public PdfViewRenderer newViewRenderer() {
    return new PdfViewRenderer(this, this.contentStream);
  }

}
