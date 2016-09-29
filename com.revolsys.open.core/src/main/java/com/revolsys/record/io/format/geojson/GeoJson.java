package com.revolsys.record.io.format.geojson;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.io.GeometryReaderFactory;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.io.FileUtil;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.io.format.cogojson.CogoJson;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;

public class GeoJson extends AbstractIoFactoryWithCoordinateSystem
  implements RecordWriterFactory, GeometryReaderFactory {

  public static final String COORDINATES = "coordinates";

  public static final String CRS = "crs";

  public static final String EPSG = "EPSG:";

  public static final String FEATURE = "Feature";

  public static final String FEATURE_COLLECTION = "FeatureCollection";

  public static final String FEATURES = "features";

  public static final String GEOMETRIES = "geometries";

  public static final String GEOMETRY = "geometry";

  public static final String GEOMETRY_COLLECTION = "GeometryCollection";

  public static final String LINE_STRING = "LineString";

  public static final String MULTI_LINE_STRING = "MultiLineString";

  public static final String MULTI_POINT = "MultiPoint";

  public static final String MULTI_POLYGON = "MultiPolygon";

  public static final String NAME = "name";

  public static final String POINT = "Point";

  public static final String POLYGON = "Polygon";

  public static final String PROPERTIES = "properties";

  public static final String TYPE = "type";

  public static final String URN_OGC_DEF_CRS_EPSG = "urn:ogc:def:crs:EPSG::";

  public static final Set<String> GEOMETRY_TYPE_NAMES = new LinkedHashSet<>(
    Arrays.asList(POINT, LINE_STRING, POLYGON, MULTI_POINT, MULTI_LINE_STRING, MULTI_POLYGON,
      GEOMETRY_COLLECTION, CogoJson.COGO_LINE_STRING, CogoJson.COGO_MULTI_LINE_STRING,
      CogoJson.COGO_POLYGON, CogoJson.COGO_MULTI_POLYGON));

  public static final Set<String> OBJECT_TYPE_NAMES = new TreeSet<>(
    Arrays.asList(FEATURE, FEATURE_COLLECTION, POINT, LINE_STRING, POLYGON, MULTI_POINT,
      MULTI_LINE_STRING, MULTI_POLYGON, GEOMETRY_COLLECTION));

  public GeoJson() {
    super("GeoJSON");
    addMediaTypeAndFileExtension("application/vnd.geo+json", "geojson");
    addMediaType("application/x-geo+json");
  }

  @Override
  public boolean isReadFromZipFileSupported() {
    return true;
  }

  @Override
  public GeometryReader newGeometryReader(final Resource resource) {
    final GeoJsonGeometryIterator iterator = new GeoJsonGeometryIterator(resource);
    return iterator;
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = FileUtil.newUtf8Writer(outputStream);
    return new GeoJsonRecordWriter(writer);
  }
}
