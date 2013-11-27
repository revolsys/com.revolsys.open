package com.revolsys.io.gpx;

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

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.cs.projection.CoordinateProjectionUtil;
import com.revolsys.gis.cs.projection.CoordinatesOperation;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.InPlaceIterator;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.xml.XmlWriter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class GpxWriter extends AbstractWriter<DataObject> {

  private String commentAttribute = "comment";

  private String descriptionAttribute = "description";

  private File file;

  private String nameAttribute = "name";

  private final XmlWriter out;

  public GpxWriter(final File file) throws IOException {
    this(new FileWriter(file));
    this.file = file;
  }

  public GpxWriter(final Writer writer) throws IOException {
    out = new XmlWriter(new BufferedWriter(writer));
    out.setIndent(false);
    out.startDocument("UTF-8", "1.0");

    out.startTag(GpxConstants.GPX_ELEMENT);
    out.attribute(GpxConstants.VERSION_ATTRIBUTE, "1.1");
    out.attribute(GpxConstants.CREATOR_ATTRIBUTE,
      "Revolution Systems Inc. - GIS");
  }

  @Override
  public void close() {
    out.endTag();
    out.endDocument();
    out.close();
  }

  @Override
  public void flush() {
    out.flush();
  }

  public String getCommentAttribute() {
    return commentAttribute;
  }

  public String getDescriptionAttribute() {
    return descriptionAttribute;
  }

  public String getNameAttribute() {
    return nameAttribute;
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
    return file.getAbsolutePath();
  }

  @Override
  public void write(final DataObject object) {
    try {
      final Geometry geometry = object.getGeometryValue();
      if (geometry instanceof Point) {
        writeWaypoint(object);
      } else if (geometry instanceof LineString) {
        writeTrack(object);
      }
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private void writeAttributes(final DataObject object) {
    final Object time = object.getValue("timestamp");
    if (time != null) {
      if (time instanceof Date) {
        final DateFormat timestampFormat = new SimpleDateFormat(
          "yyyy-MM-dd'T'HH:mm:ss.");
        timestampFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        out.element(GpxConstants.TIME_ELEMENT, timestampFormat.format(time));
      } else {
        out.element(GpxConstants.TIME_ELEMENT, time.toString());
      }
    }
    writeElement(object, GpxConstants.NAME_ELEMENT, nameAttribute);
    writeElement(object, GpxConstants.COMMENT_ELEMENT, commentAttribute);
    writeElement(object, GpxConstants.DESCRIPTION_ELEMENT, descriptionAttribute);
  }

  private void writeElement(final DataObject object, final QName tag,
    final String attributeName) {
    final String name = object.getValue(attributeName);
    if (name != null && name.length() > 0) {
      out.element(tag, name);
    }
  }

  private void writeTrack(final DataObject object) throws IOException {
    out.startTag(GpxConstants.TRACK_ELEMENT);
    final LineString line = object.getGeometryValue();
    final int srid = line.getSRID();
    final CoordinateSystem coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(srid);
    final CoordinatesOperation inverseCoordinatesOperation = ProjectionFactory.getToGeographicsCoordinatesOperation(coordinateSystem);
    final CoordinatesList coordinatesList = CoordinatesListUtil.get(line);
    writeAttributes(object);
    out.startTag(GpxConstants.TRACK_SEGMENT_ELEMENT);
    final DoubleCoordinates geoCoordinates = new DoubleCoordinates(
      coordinatesList.getDimension());

    for (final Coordinates coordinates : new InPlaceIterator(coordinatesList)) {
      inverseCoordinatesOperation.perform(coordinates, geoCoordinates);
      out.startTag(GpxConstants.TRACK_POINT_ELEMENT);
      out.attribute(GpxConstants.LON_ATTRIBUTE, geoCoordinates.getX());
      out.attribute(GpxConstants.LAT_ATTRIBUTE, geoCoordinates.getY());
      if (coordinatesList.getDimension() > 2) {
        final double elevation = geoCoordinates.getValue(2);
        if (!Double.isNaN(elevation)) {
          out.element(GpxConstants.ELEVATION_ELEMENT, String.valueOf(elevation));
        }
      }
      out.endTag(GpxConstants.TRACK_POINT_ELEMENT);
    }
    out.endTag(GpxConstants.TRACK_SEGMENT_ELEMENT);
    out.endTag(GpxConstants.TRACK_ELEMENT);
  }

  private void writeWaypoint(final DataObject wayPoint) throws IOException {
    out.startTag(GpxConstants.WAYPOINT_ELEMENT);
    final Point point = wayPoint.getGeometryValue();
    final Coordinate coordinate = point.getCoordinate();
    final CoordinateSystem coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(point.getSRID());
    final CoordinatesOperation inverseCoordinatesOperation = ProjectionFactory.getToGeographicsCoordinatesOperation(coordinateSystem);
    final Coordinate geoCoordinate = CoordinateProjectionUtil.perform(
      inverseCoordinatesOperation, coordinate);
    out.attribute(GpxConstants.LON_ATTRIBUTE, geoCoordinate.x);
    out.attribute(GpxConstants.LAT_ATTRIBUTE, geoCoordinate.y);
    if (point.getCoordinateSequence().getDimension() > 2) {
      final double elevation = geoCoordinate.z;
      if (!Double.isNaN(elevation)) {
        out.element(GpxConstants.ELEVATION_ELEMENT, String.valueOf(elevation));
      }
    }
    writeAttributes(wayPoint);
    out.endTag(GpxConstants.WAYPOINT_ELEMENT);
  }

}
