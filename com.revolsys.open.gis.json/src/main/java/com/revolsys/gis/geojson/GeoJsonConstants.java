package com.revolsys.gis.geojson;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public interface GeoJsonConstants {

  String COORDINATES = "coordinates";

  String CRS = "crs";

  String DESCRIPTION = "GeoJSON";

  String FEATURE = "Feature";

  String FEATURE_COLLECTION = "FeatureCollection";

  String FEATURES = "features";

  String FILE_EXTENSION = "geojson";

  String GEOMETRY = "geometry";

  String GEOMETRY_COLLECTION = "GeometryCollection";

  String LINE_STRING = "LineString";

  String MEDIA_TYPE = "application/x-geo+json";

  String MULTI_LINE_STRING = "MultiLineString";

  String MULTI_POINT = "MultiPoint";

  String MULTI_POLYGON = "MultiPolygon";

  String NAME = "name";

  String POINT = "Point";

  String POLYGON = "Polygon";

  String PROPERTIES = "properties";

  String TYPE = "type";

  Set<String> OBJECT_TYPE_NAMES = new TreeSet<String>(Arrays.asList(FEATURE,
    FEATURE_COLLECTION, POINT, LINE_STRING, POLYGON, MULTI_POINT,
    MULTI_LINE_STRING, MULTI_POLYGON, GEOMETRY_COLLECTION));

  Set<String> GEOMETRY_TYPE_NAMES = new LinkedHashSet<String>(Arrays.asList(POINT,
    LINE_STRING, POLYGON, MULTI_POINT, MULTI_LINE_STRING, MULTI_POLYGON,
    GEOMETRY_COLLECTION));

  public static final String URN_OGC_DEF_CRS_EPSG = "urn:ogc:def:crs:EPSG::";
  public static final String EPSG = "EPSG:";
}
