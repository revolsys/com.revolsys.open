package com.revolsys.gis.kml.io;

import javax.xml.namespace.QName;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;

public interface Kml22Constants {
  String FILE_EXTENSION = "kml";

  String MEDIA_TYPE = "application/vnd.google-earth.kml+xml";

  String FORMAT_DESCRIPTION = "Google Earth";

  CoordinateSystem COORDINATE_SYSTEM = EpsgCoordinateSystems.getCoordinateSystem(4326);

  String KML_NS_URI = "http://www.opengis.net/kml/2.2";

  QName COORDINATES = new QName(KML_NS_URI, "coordinates");

  QName DESCRIPTION = new QName(KML_NS_URI, "description");

  QName DATA = new QName(KML_NS_URI, "Data");

  QName DOCUMENT = new QName(KML_NS_URI, "Document");

  QName EAST = new QName(KML_NS_URI, "east");

  QName EXTENDED_DATA = new QName(KML_NS_URI, "ExtendedData");

  QName GROUND_OVERLAY = new QName(KML_NS_URI, "GroundOverlay");

  QName HREF = new QName(KML_NS_URI, "href");

  QName ICON = new QName(KML_NS_URI, "Icon");

  QName INNER_BOUNDARY_IS = new QName(KML_NS_URI, "innerBoundaryIs");

  QName KML = new QName(KML_NS_URI, "kml");

  QName LAT_LON_ALT_BOX = new QName(KML_NS_URI, "LatLonAltBox");

  QName LAT_LON_BOX = new QName(KML_NS_URI, "LatLonBox");

  QName LINE_STRING = new QName(KML_NS_URI, "LineString");

  QName LINEAR_RING = new QName(KML_NS_URI, "LinearRing");

  QName LINK = new QName(KML_NS_URI, "Link");

  QName LOD = new QName(KML_NS_URI, "Lod");

  QName MAX_LOD_PIXELS = new QName(KML_NS_URI, "maxLodPixels");

  QName MIN_LOD_PIXELS = new QName(KML_NS_URI, "minLodPixels");

  QName MULTI_GEOMETRY = new QName(KML_NS_URI, "MultiGeomentry");

  QName NAME = new QName(KML_NS_URI, "name");

  QName NETWORK_LINK = new QName(KML_NS_URI, "NetworkLink");

  QName NORTH = new QName(KML_NS_URI, "north");

  QName OUTER_BOUNDARY_IS = new QName(KML_NS_URI, "outerBoundaryIs");

  QName PLACEMARK = new QName(KML_NS_URI, "Placemark");

  QName POINT = new QName(KML_NS_URI, "Point");

  QName POLYGON = new QName(KML_NS_URI, "Polygon");

  QName REGION = new QName(KML_NS_URI, "Region");

  QName SOUTH = new QName(KML_NS_URI, "south");

  QName STYLE_URL = new QName(KML_NS_URI, "styleUrl");

  QName VALUE = new QName(KML_NS_URI, "value");

  QName VIEW_REFRESH_MODE = new QName(KML_NS_URI, "viewRefreshMode");

  QName WEST = new QName(KML_NS_URI, "west");

  QName OPEN = new QName(KML_NS_URI, "open");
}
