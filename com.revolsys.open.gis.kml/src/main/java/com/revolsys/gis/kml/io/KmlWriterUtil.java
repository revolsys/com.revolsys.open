package com.revolsys.gis.kml.io;

import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.io.StringBufferWriter;
import com.revolsys.xml.io.XmlWriter;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class KmlWriterUtil {

  public static void append(StringBuffer buffer, Geometry geometry) {
    XmlWriter writer = new XmlWriter(new StringBufferWriter(buffer),false);
    
    writeGeometry(writer, geometry);
    writer.close();
  }
  public static void writeGeometry(final XmlWriter writer,
    final Geometry geometry) {
    if (geometry != null) {
      final int numGeometries = geometry.getNumGeometries();
      if (numGeometries > 1) {
        writer.startTag(Kml22Constants.MULTI_GEOMETRY);
        for (int i = 0; i < numGeometries; i++) {
          writeGeometry(writer, geometry.getGeometryN(i));
        }
        writer.endTag();
      } else {
        Geometry geoGraphicsGeom = GeometryProjectionUtil.perform(geometry,
          Kml22Constants.COORDINATE_SYSTEM);
        if (geoGraphicsGeom instanceof Point) {
          final Point point = (Point)geoGraphicsGeom;
          KmlWriterUtil.writePoint(writer, point);
        } else if (geoGraphicsGeom instanceof LinearRing) {
          final LinearRing line = (LinearRing)geoGraphicsGeom;
          KmlWriterUtil.writeLinearRing(writer, line);
        } else if (geoGraphicsGeom instanceof LineString) {
          final LineString line = (LineString)geoGraphicsGeom;
          KmlWriterUtil.writeLineString(writer, line);
        } else if (geoGraphicsGeom instanceof Polygon) {
          final Polygon polygon = (Polygon)geoGraphicsGeom;
          KmlWriterUtil.writePolygon(writer, polygon);
        } else if (geoGraphicsGeom instanceof GeometryCollection) {
          final GeometryCollection collection = (GeometryCollection)geoGraphicsGeom;
          writeMultiGeometry(writer, collection);
        }
      }
    }
  }

  public static void writeMultiGeometry(final XmlWriter writer,
    GeometryCollection collection) {
    writer.startTag(Kml22Constants.MULTI_GEOMETRY);
    for (int i = 0; i < collection.getNumGeometries(); i++) {
      Geometry geometry = collection.getGeometryN(i);
      writeGeometry(writer, geometry);
    }
    writer.endTag(Kml22Constants.MULTI_GEOMETRY);

  }

  public static void writePolygon(final XmlWriter writer, final Polygon polygon) {
    writer.startTag(Kml22Constants.POLYGON);
    writer.startTag(Kml22Constants.OUTER_BOUNDARY_IS);
    writeLinearRing(writer, polygon.getExteriorRing());
    writer.endTag();
    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      writer.startTag(Kml22Constants.INNER_BOUNDARY_IS);
      final LineString ring = polygon.getInteriorRingN(i);
      writeLinearRing(writer, ring);
      writer.endTag();
    }
    writer.endTag();
  }

  public static void writeLinearRing(final XmlWriter writer,
    final LineString ring) {
    writer.startTag(Kml22Constants.LINEAR_RING);
    final CoordinateSequence coordinateSequence = ring.getCoordinateSequence();
    write(writer, coordinateSequence);
    writer.endTag();

  }

  public static void writePoint(final XmlWriter writer, final Point point) {
    writer.startTag(Kml22Constants.POINT);
    write(writer, point.getCoordinateSequence());
    writer.endTag();
  }

  public static void writeLineString(final XmlWriter writer,
    final LineString line) {
    writer.startTag(Kml22Constants.LINE_STRING);
    final CoordinateSequence coordinateSequence = line.getCoordinateSequence();
    write(writer, coordinateSequence);
    writer.endTag();
  }

  public static void write(final XmlWriter writer,
    final CoordinateSequence coordinateSequence) {
    writer.startTag(Kml22Constants.COORDINATES);
    boolean hasZ = coordinateSequence.getDimension() > 2;
    for (int i = 0; i < coordinateSequence.size(); i++) {
      writer.write(String.valueOf(coordinateSequence.getX(i)));
      writer.write(',');
      writer.write(String.valueOf(coordinateSequence.getY(i)));
      if (hasZ) {
        final double z = coordinateSequence.getOrdinate(i, 2);
        if (!Double.isNaN(z)) {
          writer.write(',');
          writer.write(String.valueOf(z));
        }
      }
      writer.write(' ');
    }
    writer.endTag();
  }
}
