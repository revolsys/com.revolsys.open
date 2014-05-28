package com.revolsys.io.geojson;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public interface GeoJsonConstants {

  String COORDINATES = "coordinates";

  String CRS = "crs";

  String DESCRIPTION = "GeoJSON";

  String COGO_DESCRIPTION = "CogoJSON";

  String FEATURE = "Feature";

  String FEATURE_COLLECTION = "FeatureCollection";

  String FEATURES = "features";

  String FILE_EXTENSION = "geojson";

  String COGO_FILE_EXTENSION = "cogojson";

  String GEOMETRY = "geometry";

  String GEOMETRIES = "geometries";

  String GEOMETRY_COLLECTION = "GeometryCollection";

  String LINE_STRING = "LineString";

  String MEDIA_TYPE = "application/vnd.geo+json";

  String MEDIA_TYPE_OLD = "application/x-geo+json";

  String COGO_MEDIA_TYPE = "application/x-cogo+json";

  String MULTI_LINE_STRING = "MultiLineString";

  String COGO_MULTI_LINE_STRING = "CogoMultiLineString";

  String COGO_LINE_STRING = "CogoLineString";

  String COGO_MULTI_POLYGON = "CogoMultiPolygon";

  String COGO_POLYGON = "CogoPolygon";

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

  Set<String> GEOMETRY_TYPE_NAMES = new LinkedHashSet<String>(Arrays.asList(
    POINT, LINE_STRING, POLYGON, MULTI_POINT, MULTI_LINE_STRING, MULTI_POLYGON,
    GEOMETRY_COLLECTION, COGO_LINE_STRING, COGO_MULTI_LINE_STRING,
    COGO_POLYGON, COGO_MULTI_POLYGON));

  public static final String URN_OGC_DEF_CRS_EPSG = "urn:ogc:def:crs:EPSG::";

  public static final String EPSG = "EPSG:";
}
