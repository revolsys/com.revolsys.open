package com.revolsys.gis.gpx.io;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataTypes;

public final class GpxConstants {

  public static final String GPX_NS = "gpx";

  public static final String GPX_NS_URI = "http://www.topografix.com/GPX/1/1";

  public static final QName COMMENT_ELEMENT = new QName(GPX_NS_URI, "cmt");

  public static final QName CREATOR_ATTRIBUTE = new QName(null, "creator");

  public static final QName DESCRIPTION_ELEMENT = new QName(GPX_NS_URI, "desc");

  public static final QName ELEVATION_ELEMENT = new QName(GPX_NS_URI, "ele");

  public static final QName EXTENSION_ELEMENT = new QName(GPX_NS_URI,
    "extensions");

  public static final QName GPX_ELEMENT = new QName(GPX_NS_URI, "gpx");

  public static final DataObjectMetaDataImpl GPX_TYPE = new DataObjectMetaDataImpl(
    new QName(GPX_NS_URI, "gpx"));

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

    GPX_TYPE.addAttribute("location", DataTypes.GEOMETRY, true);
    GPX_TYPE.setGeometryAttributeIndex(0);
    GPX_TYPE.addAttribute("feature_type", DataTypes.STRING, false);
    GPX_TYPE.addAttribute("time", DataTypes.DATE_TIME, false);
    GPX_TYPE.addAttribute("magvar", DataTypes.DECIMAL, false);
    GPX_TYPE.addAttribute("geoidheight", DataTypes.DECIMAL, false);
    GPX_TYPE.addAttribute("name", DataTypes.STRING, false);
    GPX_TYPE.addAttribute("cmt", DataTypes.STRING, false);
    GPX_TYPE.addAttribute("desc", DataTypes.STRING, false);
    GPX_TYPE.addAttribute("src", DataTypes.STRING, false);
    GPX_TYPE.addAttribute("number", DataTypes.INTEGER, false);
    GPX_TYPE.addAttribute("link", DataTypes.STRING, false);
    GPX_TYPE.addAttribute("sym", DataTypes.STRING, false);
    GPX_TYPE.addAttribute("type", DataTypes.STRING, false);
    GPX_TYPE.addAttribute("fix", DataTypes.STRING, false);
    GPX_TYPE.addAttribute("sat", DataTypes.INTEGER, false);
    GPX_TYPE.addAttribute("hdop", DataTypes.DECIMAL, false);
    GPX_TYPE.addAttribute("vdop", DataTypes.DECIMAL, false);
    GPX_TYPE.addAttribute("pdop", DataTypes.DECIMAL, false);
    GPX_TYPE.addAttribute("ageofdgpsdata", DataTypes.DECIMAL, false);
    GPX_TYPE.addAttribute("dgpsid", DataTypes.STRING, false);

  }

  private GpxConstants() {
  }
}
