package com.revolsys.io.gpx;

import javax.xml.namespace.QName;

import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.jts.geom.GeometryFactory;

public final class GpxConstants {

  public static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.floating3(4326);

  public static final String GPX_NS = "gpx";

  public static final String GPX_NS_URI = "http://www.topografix.com/GPX/1/1";

  public static final QName COMMENT_ELEMENT = new QName(GPX_NS_URI, "cmt");

  public static final QName CREATOR_ATTRIBUTE = new QName(null, "creator");

  public static final QName DESCRIPTION_ELEMENT = new QName(GPX_NS_URI, "desc");

  public static final QName ELEVATION_ELEMENT = new QName(GPX_NS_URI, "ele");

  public static final QName EXTENSION_ELEMENT = new QName(GPX_NS_URI,
    "extensions");

  public static final QName GPX_ELEMENT = new QName(GPX_NS_URI, "gpx");

  public static final RecordDefinitionImpl GPX_TYPE = new RecordDefinitionImpl(
    "gpx");

  public static final RecordDefinitionImpl GPX_WAYPOINT = new RecordDefinitionImpl(
    "/gpx/waypoint");

  public static final RecordDefinitionImpl GPX_TRACK = new RecordDefinitionImpl(
    "/gpx/track");

  public static final RecordDefinitionImpl GPX_ROUTE = new RecordDefinitionImpl(
    "/gpx/route");

  public static final QName LAT_ATTRIBUTE = new QName(null, "lat");

  public static final QName LON_ATTRIBUTE = new QName(null, "lon");

  public static final QName METADATA_ELEMENT = new QName(GPX_NS_URI, "metadata");

  public static final QName NAME_ELEMENT = new QName(GPX_NS_URI, "name");

  public static final QName ROUTE_ELEMENT = new QName(GPX_NS_URI, "rte");

  public static final QName ROUTE_POINT_ELEMENT = new QName(GPX_NS_URI, "rtept");

  public static final QName SYM_ELEMENT = new QName(GPX_NS_URI, "sym");

  public static final QName TIME_ELEMENT = new QName(GPX_NS_URI, "time");

  public static final QName TRACK_ELEMENT = new QName(GPX_NS_URI, "trk");

  public static final QName TRACK_POINT_ELEMENT = new QName(GPX_NS_URI, "trkpt");

  public static final QName TRACK_SEGMENT_ELEMENT = new QName(GPX_NS_URI,
    "trkseg");

  public static final QName TYPE_ELEMENT = new QName(GPX_NS_URI, "type");

  public static final QName VERSION_ATTRIBUTE = new QName(null, "version");

  public static final QName WAYPOINT_ELEMENT = new QName(GPX_NS_URI, "wpt");

  public static final String FILE_EXTENSION = "gpx";

  public static final String MEDIA_TYPE = "application/gpx+xml";

  static {
    addAttribute("dataset_name", DataTypes.STRING, false);
    addAttribute("index", DataTypes.DOUBLE, false);
    addAttribute("feature_type", DataTypes.STRING, false);
    addAttribute("time", DataTypes.DATE_TIME, false);
    addAttribute("magvar", DataTypes.DOUBLE, false);
    addAttribute("geoidheight", DataTypes.DOUBLE, false);
    addAttribute("name", DataTypes.STRING, false);
    addAttribute("cmt", DataTypes.STRING, false);
    addAttribute("desc", DataTypes.STRING, false);
    addAttribute("src", DataTypes.STRING, false);
    addAttribute("number", DataTypes.INT, false);
    addAttribute("link", DataTypes.STRING, false);
    addAttribute("sym", DataTypes.STRING, false);
    addAttribute("type", DataTypes.STRING, false);
    addAttribute("fix", DataTypes.STRING, false);
    addAttribute("sat", DataTypes.INT, false);
    addAttribute("hdop", DataTypes.DOUBLE, false);
    addAttribute("vdop", DataTypes.DOUBLE, false);
    addAttribute("pdop", DataTypes.DOUBLE, false);
    addAttribute("ageofdgpsdata", DataTypes.DOUBLE, false);
    addAttribute("dgpsid", DataTypes.STRING, false);
    GPX_TYPE.addAttribute("location", DataTypes.GEOMETRY, true);
    GPX_TYPE.setGeometryFactory(GEOMETRY_FACTORY);
    GPX_WAYPOINT.addAttribute("geometry", DataTypes.POINT, true);
    GPX_WAYPOINT.setGeometryFactory(GEOMETRY_FACTORY);
    GPX_TRACK.addAttribute("geometry", DataTypes.MULTI_LINE_STRING, true);
    GPX_TRACK.setGeometryFactory(GEOMETRY_FACTORY);
    GPX_ROUTE.addAttribute("geometry", DataTypes.LINE_STRING, true);
    GPX_ROUTE.setGeometryFactory(GEOMETRY_FACTORY);
  }

  private static void addAttribute(final String name, final DataType type,
    final boolean required) {
    GPX_TYPE.addAttribute(name, type, required);
    GPX_WAYPOINT.addAttribute(name, type, required);
    GPX_TRACK.addAttribute(name, type, required);
    GPX_ROUTE.addAttribute(name, type, required);
  }

  private GpxConstants() {
  }
}
