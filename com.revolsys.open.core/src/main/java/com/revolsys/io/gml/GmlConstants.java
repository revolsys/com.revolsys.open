package com.revolsys.io.gml;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.namespace.QName;

public interface GmlConstants {
  String _NS_PREFIX = "gml";

  String _NS_URI = "http://www.opengis.net/gml";

  QName BOUNDED_BY = new QName(_NS_URI, "boundedBy", _NS_PREFIX);

  QName COORDINATES = new QName(_NS_URI, "coordinates", _NS_PREFIX);

  QName DIMENSION = new QName("dimension");

  QName ENVELOPE = new QName(_NS_URI, "BoundingBoxDoubleGf", _NS_PREFIX);

  QName BOX = new QName(_NS_URI, "Box", _NS_PREFIX);

  QName FEATURE_COLLECTION = new QName(_NS_URI, "FeatureCollection", _NS_PREFIX);

  QName FEATURE_MEMBER = new QName(_NS_URI, "featureMember", _NS_PREFIX);

  String FILE_EXTENSION = "gml";

  String FORMAT_DESCRIPTION = "Geography Markup Language";

  QName GEOMETRY_MEMBER = new QName(_NS_URI, "geometryMember", _NS_PREFIX);

  QName INNER_BOUNDARY_IS = new QName(_NS_URI, "innerBoundaryIs", _NS_PREFIX);

  QName LINE_STRING = new QName(_NS_URI, "LineString", _NS_PREFIX);

  QName LINE_STRING_MEMBER = new QName(_NS_URI, "lineStringMember", _NS_PREFIX);

  QName LINEAR_RING = new QName(_NS_URI, "LinearRing", _NS_PREFIX);

  QName LOWER_CORNER = new QName(_NS_URI, "lowerCorner", _NS_PREFIX);

  String MEDIA_TYPE = "application/gml+xml";

  QName MULTI_GEOMETRY = new QName(_NS_URI, "MultiGeometry", _NS_PREFIX);

  QName MULTI_LINE_STRING = new QName(_NS_URI, "MultiLineString", _NS_PREFIX);

  QName MULTI_POINT = new QName(_NS_URI, "MultiPoint", _NS_PREFIX);

  QName MULTI_POLYGON = new QName(_NS_URI, "MultiPolygon", _NS_PREFIX);

  QName OUTER_BOUNDARY_IS = new QName(_NS_URI, "outerBoundaryIs", _NS_PREFIX);

  QName POINT = new QName(_NS_URI, "Point", _NS_PREFIX);

  QName POINT_MEMBER = new QName(_NS_URI, "pointMember", _NS_PREFIX);

  QName POLYGON = new QName(_NS_URI, "Polygon", _NS_PREFIX);

  QName POLYGON_MEMBER = new QName(_NS_URI, "polygonMember", _NS_PREFIX);

  QName POS = new QName(_NS_URI, "pos", _NS_PREFIX);

  QName POS_LIST = new QName(_NS_URI, "posList", _NS_PREFIX);

  QName SRS_NAME = new QName("srsName");

  QName UPPER_CORNER = new QName(_NS_URI, "upperCorner", _NS_PREFIX);;

  String VERSION_PROPERTY = "java:" + GmlConstants.class.getName() + ".version";

  Set<QName> GEOMETRY_TYPE_NAMES = new LinkedHashSet<QName>(Arrays.asList(
    POINT, LINE_STRING, POLYGON, MULTI_POINT, MULTI_LINE_STRING, MULTI_POLYGON,
    MULTI_GEOMETRY));

  Set<QName> ENVELOPE_AND_GEOMETRY_TYPE_NAMES = new LinkedHashSet<QName>(
      Arrays.asList(ENVELOPE, POINT, LINE_STRING, POLYGON, MULTI_POINT,
        MULTI_LINE_STRING, MULTI_POLYGON, MULTI_GEOMETRY));
}
