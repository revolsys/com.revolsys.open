package com.revolsys.record.io.format.gpx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.namespace.QName;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.xml.XmlWriter;

public class GpxWriter extends AbstractRecordWriter {

  private String commentAttribute = "comment";

  private String descriptionAttribute = "description";

  private File file;

  private String nameAttribute = "name";

  private final XmlWriter out;

  public GpxWriter(final File file) throws IOException {
    this(new FileWriter(file));
    this.file = file;
  }

  public GpxWriter(final Writer writer) {
    this.out = new XmlWriter(new BufferedWriter(writer));
    this.out.setIndent(false);
    this.out.startDocument("UTF-8", "1.0");

    this.out.startTag(GpxConstants.GPX_ELEMENT);
    this.out.attribute(GpxConstants.VERSION_ATTRIBUTE, "1.1");
    this.out.attribute(GpxConstants.CREATOR_ATTRIBUTE, "Revolution Systems Inc. - GIS");
  }

  @Override
  public void close() {
    this.out.endTag();
    this.out.endDocument();
    this.out.close();
  }

  @Override
  public void flush() {
    this.out.flush();
  }

  public String getCommentAttribute() {
    return this.commentAttribute;
  }

  public String getDescriptionAttribute() {
    return this.descriptionAttribute;
  }

  public String getNameAttribute() {
    return this.nameAttribute;
  }

  public void setCommentAttribute(final String commentAttribute) {
    this.commentAttribute = commentAttribute;
  }

  public void setDescriptionAttribute(final String descriptionAttribute) {
    this.descriptionAttribute = descriptionAttribute;
  }

  public void setNameAttribute(final String nameAttribute) {
    this.nameAttribute = nameAttribute;
  }

  @Override
  public String toString() {
    return this.file.getAbsolutePath();
  }

  @Override
  public void write(final Record object) {
    try {
      final Geometry geometry = object.getGeometry();
      if (geometry instanceof Point) {
        writeWaypoint(object);
      } else if (geometry instanceof LineString) {
        writeTrack(object);
      }
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private void writeAttributes(final Record object) {
    final Object time = object.getValue("timestamp");
    if (time != null) {
      if (time instanceof Date) {
        final DateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        timestampFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.out.element(GpxConstants.TIME_ELEMENT, timestampFormat.format(time));
      } else {
        this.out.element(GpxConstants.TIME_ELEMENT, time.toString());
      }
    }
    writeElement(object, GpxConstants.NAME_ELEMENT, this.nameAttribute);
    writeElement(object, GpxConstants.COMMENT_ELEMENT, this.commentAttribute);
    writeElement(object, GpxConstants.DESCRIPTION_ELEMENT, this.descriptionAttribute);
  }

  private void writeElement(final Record object, final QName tag, final String fieldName) {
    final String name = object.getValue(fieldName);
    if (name != null && name.length() > 0) {
      this.out.element(tag, name);
    }
  }

  private void writeTrack(final Record object) throws IOException {
    this.out.startTag(GpxConstants.TRACK_ELEMENT);
    LineString line = object.getGeometry();
    line = line.convertGeometry(GpxConstants.GEOMETRY_FACTORY);
    final LineString coordinatesList = line;
    writeAttributes(object);
    this.out.startTag(GpxConstants.TRACK_SEGMENT_ELEMENT);

    for (final Point coordinates : line.vertices()) {
      this.out.startTag(GpxConstants.TRACK_POINT_ELEMENT);
      this.out.attribute(GpxConstants.LON_ATTRIBUTE, coordinates.getX());
      this.out.attribute(GpxConstants.LAT_ATTRIBUTE, coordinates.getY());
      if (coordinatesList.getAxisCount() > 2) {
        final double elevation = coordinates.getCoordinate(2);
        if (!Double.isNaN(elevation)) {
          this.out.element(GpxConstants.ELEVATION_ELEMENT, String.valueOf(elevation));
        }
      }
      this.out.endTag(GpxConstants.TRACK_POINT_ELEMENT);
    }
    this.out.endTag(GpxConstants.TRACK_SEGMENT_ELEMENT);
    this.out.endTag(GpxConstants.TRACK_ELEMENT);
  }

  private void writeWaypoint(final Record wayPoint) throws IOException {
    this.out.startTag(GpxConstants.WAYPOINT_ELEMENT);
    final Point point = wayPoint.getGeometry();
    final Point geoCoordinate = point.convertGeometry(GpxConstants.GEOMETRY_FACTORY);
    this.out.attribute(GpxConstants.LON_ATTRIBUTE, geoCoordinate.getX());
    this.out.attribute(GpxConstants.LAT_ATTRIBUTE, geoCoordinate.getY());
    if (point.getAxisCount() > 2) {
      final double elevation = geoCoordinate.getZ();
      if (!Double.isNaN(elevation)) {
        this.out.element(GpxConstants.ELEVATION_ELEMENT, String.valueOf(elevation));
      }
    }
    writeAttributes(wayPoint);
    this.out.endTag(GpxConstants.WAYPOINT_ELEMENT);
  }

}
