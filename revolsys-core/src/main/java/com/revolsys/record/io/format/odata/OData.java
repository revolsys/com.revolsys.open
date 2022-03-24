package com.revolsys.record.io.format.odata;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.io.AbstractIoFactory;
import com.revolsys.record.io.RecordStoreFactory;
import com.revolsys.record.schema.RecordStore;

/**
 * odata:[url]
 */
public class OData extends AbstractIoFactory implements RecordStoreFactory {

  private static Map<String, DataType> DATA_TYPE_BY_EDM_STRING = new HashMap<>();

  static {
    // addDataType("Edm.Binary, DataTypes.BOOLEAN);
    addDataType("Edm.Boolean", DataTypes.BOOLEAN);
    addDataType("Edm.Byte", DataTypes.UBYTE);
    addDataType("Edm.Date", DataTypes.SQL_DATE);
    addDataType("Edm.DateTimeOffset", DataTypes.INSTANT);
    addDataType("Edm.Decimal", DataTypes.DECIMAL);
    addDataType("Edm.Double", DataTypes.DOUBLE);
    // addDataType("Edm.Duration",DataTypes.DOUBLE);
    addDataType("Edm.Guid", DataTypes.UUID);
    addDataType("Edm.Int16", DataTypes.SHORT);
    addDataType("Edm.Int32", DataTypes.INT);
    addDataType("Edm.Int64", DataTypes.LONG);
    addDataType("Edm.SByte", DataTypes.BYTE);
    addDataType("Edm.Single", DataTypes.FLOAT);
    // addDataType("Edm.Stream",DataTypes.STRING);
    addDataType("Edm.String", DataTypes.STRING);
    addDataType("Edm.TimeOfDay", DataTypes.TIME);
    addDataType("Edm.Geometry", GeometryDataTypes.GEOMETRY);
    addDataType("Edm.Geography", GeometryDataTypes.GEOMETRY);
    addDataType("Edm.GeometryCollection", GeometryDataTypes.GEOMETRY_COLLECTION);
    addDataType("Edm.GeographyCollection", GeometryDataTypes.GEOMETRY_COLLECTION);
    addDataType("Edm.GeometryPoint", GeometryDataTypes.POINT);
    addDataType("Edm.GeographyPoint", GeometryDataTypes.POINT);
    addDataType("Edm.GeometryMultiPoint", GeometryDataTypes.MULTI_POINT);
    addDataType("Edm.GeographyMultiPoint", GeometryDataTypes.MULTI_POINT);
    addDataType("Edm.GeometryLineString", GeometryDataTypes.LINE_STRING);
    addDataType("Edm.GeographyLineString", GeometryDataTypes.LINE_STRING);
    addDataType("Edm.GeometryMultiLineString", GeometryDataTypes.MULTI_LINE_STRING);
    addDataType("Edm.GeographyMultiLineString", GeometryDataTypes.MULTI_LINE_STRING);
    addDataType("Edm.GeometryPolygon", GeometryDataTypes.POLYGON);
    addDataType("Edm.GeographyPolygon", GeometryDataTypes.POLYGON);
    addDataType("Edm.GeometryMultiPolygon", GeometryDataTypes.MULTI_POLYGON);
    addDataType("Edm.GeographyMultiPolygon", GeometryDataTypes.MULTI_POLYGON);
  }

  public static final String URL_PREFIX = "odata:";

  private static final List<Pattern> URL_PATTERNS = Arrays
    .asList(Pattern.compile(URL_PREFIX + ".+"));

  private static void addDataType(final String kind, final DataType dataType) {
    DATA_TYPE_BY_EDM_STRING.put(kind, dataType);
  }

  public static DataType getDataTypeFromEdm(final String type) {
    return DATA_TYPE_BY_EDM_STRING.getOrDefault(type, DataTypes.OBJECT);
  }

  public OData() {
    super("OData");
  }

  @Override
  public boolean canOpenUrl(final String url) {
    if (isAvailable()) {
      return url.startsWith(URL_PREFIX);
    }
    return false;
  }

  @Override
  public Class<? extends RecordStore> getRecordStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    return RecordStore.class;
  }

  @Override
  public List<Pattern> getUrlPatterns() {
    return URL_PATTERNS;
  }

  @Override
  public ODataRecordStore newRecordStore(final MapEx connectionProperties) {
    return new ODataRecordStore(this, connectionProperties);
  }

  @Override
  public Map<String, Object> parseUrl(final String url) {
    if (url != null && url.startsWith(URL_PREFIX)) {
      final Map<String, Object> parameters = new LinkedHashMap<>();
      final String fileName = url.substring(URL_PREFIX.length());
      parameters.put("recordStoreType", getName());
      parameters.put("file", fileName);
    }
    return Collections.emptyMap();
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public String toUrl(final Map<String, Object> urlParameters) {
    final StringBuilder url = new StringBuilder(URL_PREFIX);
    final String file = Maps.getString(urlParameters, "serviceUrl");
    url.append(file);
    return url.toString().toLowerCase();
  }

}
