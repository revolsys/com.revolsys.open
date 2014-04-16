package com.revolsys.swing;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSObject;
import org.springframework.util.StringUtils;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;

public class PdfUtil {

  public static COSArray getArray(final COSDictionary dictionary,
    final String key) {
    final COSBase item = dictionary.getDictionaryObject(key);
    if (item instanceof COSArray) {
      return (COSArray)item;
    } else {
      return null;
    }
  }

  public static Rectangle2D getBBox(final COSDictionary dictionary) {
    final COSArray bbox = PdfUtil.getArray(dictionary, "BBox");
    if (bbox == null) {
      return null;
    } else {
      final float x1 = getFloat(bbox, 0);
      final float y1 = getFloat(bbox, 1);
      final float x2 = getFloat(bbox, 2);
      final float y2 = getFloat(bbox, 3);
      final float x = Math.min(x1, x2);
      final float y = Math.min(y1, y2);
      return new Rectangle2D.Float(x, y, Math.abs(x1 - x2), Math.abs(y1 - y2));
    }
  }

  public static COSDictionary getDictionary(final COSDictionary dictionary,
    final String key) {
    final COSBase item = dictionary.getDictionaryObject(key);
    if (item instanceof COSDictionary) {
      return (COSDictionary)item;
    } else {
      return null;
    }
  }

  public static float getFloat(final COSArray array, final int index) {
    final COSBase object = array.getObject(index);
    if (object instanceof COSNumber) {
      final COSNumber number = (COSNumber)object;
      return number.floatValue();
    } else {
      return 0;
    }
  }

  public static BoundingBox getPageBoundingBox(final COSDictionary page) {
    final Rectangle2D mediaBox = getPageMediaBox(page);
    final COSArray viewports = PdfUtil.getArray(page, "VP");
    for (final COSBase item : viewports) {
      if (item instanceof COSDictionary) {
        final COSDictionary viewport = (COSDictionary)item;
        final BoundingBox boundingBox = PdfUtil.getViewportBoundingBox(
          mediaBox, viewport);
        if (!boundingBox.isEmpty()) {
          return boundingBox;
        }
      }
    }
    return new Envelope();
  }

  public static Rectangle2D getPageMediaBox(final COSDictionary page) {
    final COSArray mediaBox = PdfUtil.getArray(page, "MediaBox");
    if (mediaBox == null) {
      return null;
    } else {
      final float x = getFloat(mediaBox, 0);
      final float y = getFloat(mediaBox, 1);
      final float width = getFloat(mediaBox, 2);
      final float height = getFloat(mediaBox, 3);
      return new Rectangle2D.Float(x, y, width, height);
    }
  }

  public static COSDictionary getPageViewport(final COSDictionary page) {
    final COSArray viewports = PdfUtil.getArray(page, "VP");
    if (viewports != null) {
      for (COSBase item : viewports) {
        if (item instanceof COSObject) {
          final COSObject object = (COSObject)item;
          item = object.getObject();
        }
        if (item instanceof COSDictionary) {
          final COSDictionary viewport = (COSDictionary)item;
          if (hasNameValue(viewport, "Type", "Viewport")) {
            return viewport;
          }
        }
      }
    }
    return null;
  }

  public static List<Point2D> getPoints(final COSDictionary dictionary,
    final String key) {
    final COSArray array = PdfUtil.getArray(dictionary, key);
    final List<Point2D> points = new ArrayList<Point2D>();
    if (array != null) {
      for (int i = 0; i < array.size(); i++) {
        final float x = PdfUtil.getFloat(array, i++);
        final float y = PdfUtil.getFloat(array, i);
        final Point2D point = new Point2D.Double(x, y);
        points.add(point);
      }
    }
    return points;
  }

  public static BoundingBox getViewportBoundingBox(final Rectangle2D mediaBox,
    final COSDictionary viewport) {
    if (hasNameValue(viewport, "Type", "Viewport")) {
      final COSDictionary measure = PdfUtil.getDictionary(viewport, "Measure");
      if (PdfUtil.hasNameValue(measure, "Type", "Measure")) {
        if (PdfUtil.hasNameValue(measure, "Subtype", "GEO")) {
          final COSDictionary gcs = PdfUtil.getDictionary(measure, "GCS");
          if (gcs != null) {
            GeometryFactory geometryFactory = GeometryFactory.getFactory();
            final int srid = gcs.getInt("EPSG");
            if (srid == -1) {
              final String wkt = gcs.getString("WKT");
              if (StringUtils.hasText(wkt)) {
                geometryFactory = GeometryFactory.getFactory(wkt);
              }
            } else {
              geometryFactory = GeometryFactory.getFactory(srid);
            }
            final com.revolsys.jts.geom.GeometryFactory geoGeometryFactory = geometryFactory.getGeographicGeometryFactory();

            BoundingBox boundingBox = new Envelope(geometryFactory);
            final COSArray geoPoints = PdfUtil.getArray(measure, "GPTS");

            for (int i = 0; i < geoPoints.size(); i++) {
              final float lat = PdfUtil.getFloat(geoPoints, i++);
              final float lon = PdfUtil.getFloat(geoPoints, i);
              final Point geoPoint = geoGeometryFactory.point(lon, lat);
              boundingBox = boundingBox.expandToInclude(geoPoint);
            }
            return boundingBox;
          }
        }
      }
    }
    return new Envelope();
  }

  public static boolean hasNameValue(final COSDictionary dictionary,
    final String key, final String value) {
    final String name = dictionary.getNameAsString(key);
    return value.equals(name);
  }
}
