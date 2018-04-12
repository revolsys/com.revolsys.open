package com.revolsys.record.io.format.gpx;

import javax.xml.namespace.QName;

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.cs.epsg.EpsgId;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.PathName;
import com.revolsys.record.schema.RecordDefinitionImpl;

public final class GpxConstants {
  public static final String GPX_NS = "gpx";

  public static final String GPX_NS_URI = "http://www.topografix.com/GPX/1/1";

  public static final QName COMMENT_ELEMENT = new QName(GPX_NS_URI, "cmt");

  public static final QName CREATOR_ATTRIBUTE = new QName(null, "creator");

  public static final QName DESCRIPTION_ELEMENT = new QName(GPX_NS_URI, "desc");

  public static final QName ELEVATION_ELEMENT = new QName(GPX_NS_URI, "ele");

  public static final QName EXTENSION_ELEMENT = new QName(GPX_NS_URI, "extensions");

  public static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory
    .floating3d(EpsgId.WGS84);

  public static final QName GPX_ELEMENT = new QName(GPX_NS_URI, "gpx");

  public static final RecordDefinitionImpl GPX_ROUTE = new RecordDefinitionImpl(
    PathName.newPathName("/gpx/route"));

  public static final RecordDefinitionImpl GPX_TRACK = new RecordDefinitionImpl(
    PathName.newPathName("/gpx/track"));

  public static final RecordDefinitionImpl GPX_TYPE = new RecordDefinitionImpl(
    PathName.newPathName("/gpx"));

  public static final RecordDefinitionImpl GPX_WAYPOINT = new RecordDefinitionImpl(
    PathName.newPathName("/gpx/waypoint"));

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

  public static final QName TRACK_SEGMENT_ELEMENT = new QName(GPX_NS_URI, "trkseg");

  public static final QName TYPE_ELEMENT = new QName(GPX_NS_URI, "type");

  public static final QName VERSION_ATTRIBUTE = new QName(null, "version");

  public static final QName WAYPOINT_ELEMENT = new QName(GPX_NS_URI, "wpt");

  static {
    addField("dataset_name", DataTypes.STRING, false);
    addField("index", DataTypes.DOUBLE, false);
    addField("feature_type", DataTypes.STRING, false);
    addField("time", DataTypes.DATE_TIME, false);
    addField("magvar", DataTypes.DOUBLE, false);
    addField("geoidheight", DataTypes.DOUBLE, false);
    addField("name", DataTypes.STRING, false);
    addField("cmt", DataTypes.STRING, false);
    addField("desc", DataTypes.STRING, false);
    addField("src", DataTypes.STRING, false);
    addField("number", DataTypes.INT, false);
    addField("link", DataTypes.STRING, false);
    addField("sym", DataTypes.STRING, false);
    addField("type", DataTypes.STRING, false);
    addField("fix", DataTypes.STRING, false);
    addField("sat", DataTypes.INT, false);
    addField("hdop", DataTypes.DOUBLE, false);
    addField("vdop", DataTypes.DOUBLE, false);
    addField("pdop", DataTypes.DOUBLE, false);
    addField("ageofdgpsdata", DataTypes.DOUBLE, false);
    addField("dgpsid", DataTypes.STRING, false);
    GPX_TYPE.addField("location", DataTypes.GEOMETRY, true);
    GPX_TYPE.setGeometryFactory(GEOMETRY_FACTORY);
    GPX_WAYPOINT.addField("geometry", DataTypes.POINT, true);
    GPX_WAYPOINT.setGeometryFactory(GEOMETRY_FACTORY);
    GPX_TRACK.addField("geometry", DataTypes.GEOMETRY, true);
    GPX_TRACK.setGeometryFactory(GEOMETRY_FACTORY);
    GPX_ROUTE.addField("geometry", DataTypes.LINE_STRING, true);
    GPX_ROUTE.setGeometryFactory(GEOMETRY_FACTORY);
  }

  private static void addField(final String name, final DataType type, final boolean required) {
    GPX_TYPE.addField(name, type, required);
    GPX_WAYPOINT.addField(name, type, required);
    GPX_TRACK.addField(name, type, required);
    GPX_ROUTE.addField(name, type, required);
  }

  private GpxConstants() {
  }
}
