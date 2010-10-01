package com.revolsys.gis.gml;

import javax.xml.namespace.QName;

public interface GmlConstants {
  String _NS_PREFIX = "gml";

  String _NS_URI = "http://www.opengis.net/gml";

  QName BOUNDED_BY = new QName(_NS_URI, "boundedBy", _NS_PREFIX);

  QName DIMENSION = new QName("dimension");

  QName ENVELOPE = new QName(_NS_URI, "Envelope", _NS_PREFIX);

  QName FEATURE_COLLECTION = new QName(_NS_URI, "FeatureCollection", _NS_PREFIX);

  QName FEATURE_MEMBERS = new QName(_NS_URI, "featureMembers", _NS_PREFIX);

  String FILE_EXTENSION = "gml";

  String FORMAT_DESCRIPTION = "Geography Markup Language";

  QName INNER_BOUNDARY_IS = new QName(_NS_URI, "innerBoundaryIs", _NS_PREFIX);

  QName LINE_STRING = new QName(_NS_URI, "LineString", _NS_PREFIX);

  QName LINEAR_RING = new QName(_NS_URI, "LinearRing", _NS_PREFIX);

  QName LOWER_CORNER = new QName(_NS_URI, "lowerCorner", _NS_PREFIX);

  String MEDIA_TYPE = "application/gml+xml";

  QName MULTI_CURVE = new QName(_NS_URI, "MultiCurve", _NS_PREFIX);

  QName CURVE_MEMBERS = new QName(_NS_URI, "curveMembers", _NS_PREFIX);

  QName POINT_MEMBERS = new QName(_NS_URI, "pointMembers", _NS_PREFIX);

  QName GEOMETRY_MEMBERS = new QName(_NS_URI, "geometryMembers", _NS_PREFIX);

  QName SURFACE_MEMBERS = new QName(_NS_URI, "surfaceMembers", _NS_PREFIX);

  QName MULTI_POINT = new QName(_NS_URI, "MultiPoint", _NS_PREFIX);

  QName MULTI_SURFACE = new QName(_NS_URI, "MultiSurface", _NS_PREFIX);

  QName MULTI_GEOMETRY = new QName(_NS_URI, "MultiGeometry", _NS_PREFIX);

  QName OUTER_BOUNDARY_IS = new QName(_NS_URI, "outerBoundaryIs", _NS_PREFIX);

  QName POINT = new QName(_NS_URI, "Point", _NS_PREFIX);

  QName POLYGON = new QName(_NS_URI, "Polygon", _NS_PREFIX);

  QName POS = new QName(_NS_URI, "pos", _NS_PREFIX);

  QName POS_LIST = new QName(_NS_URI, "posList", _NS_PREFIX);

  QName SRS_NAME = new QName(_NS_URI, "srsName", _NS_PREFIX);

  QName UPPER_CORNER = new QName(_NS_URI, "upperCorner", _NS_PREFIX);

}
