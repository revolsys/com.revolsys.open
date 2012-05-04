package com.revolsys.gis.google.fusiontables;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.googleapis.auth.clientlogin.ClientLogin;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.gis.data.io.AbstractDataObjectStore;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.google.fusiontables.attribute.DateTimeAttribute;
import com.revolsys.gis.google.fusiontables.attribute.GeometryAttribute;
import com.revolsys.gis.google.fusiontables.attribute.NumberAttribute;
import com.revolsys.gis.google.fusiontables.attribute.StringAttribute;
import com.revolsys.io.AbstractMapReaderFactory;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Reader;
import com.revolsys.spring.InputStreamResource;
import com.vividsolutions.jts.geom.Geometry;

public class FusionTablesDataObjectStore extends AbstractDataObjectStore {
  private static final Map<String, DataType> DATA_TYPES;

  static {
    final Map<String, DataType> dataTypes = new HashMap<String, DataType>();
    dataTypes.put("string", DataTypes.STRING);
    dataTypes.put("number", DataTypes.DECIMAL);
    dataTypes.put("location", DataTypes.GEOMETRY);
    dataTypes.put("datetime", DataTypes.DATE_TIME);

    DATA_TYPES = Collections.unmodifiableMap(dataTypes);
  }

  private static final String SERVICE_URL = "http://www.google.com/fusiontables/api/query";

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\?");

  public static void addColumnNames(
    final StringBuffer sql,
    final DataObjectMetaData metaData) {
    final List<String> attributeNames = metaData.getAttributeNames();
    for (int i = 0; i < attributeNames.size(); i++) {
      if (i > 0) {
        sql.append(", ");
      }
      final String attributeName = attributeNames.get(i);
      if (attributeName.equals("rowid")) {
        sql.append(attributeName);
      } else {
        sql.append("'");
        sql.append(attributeName);
        sql.append("'");
      }
      // final Attribute attribute = metaData.getAttribute(attributeName);
      // if (attribute instanceof JdbcAttribute) {
      // final JdbcAttribute jdbcAttribute = (JdbcAttribute)attribute;
      // jdbcAttribute.addColumnName(sql, tablePrefix);
      // }
    }
  }

  public static void appendString(final StringBuffer buffer, final Object value) {

    if (value == null) {
      buffer.append("''");
    } else {
      final Class<?> valueClass = value.getClass();
      if (Geometry.class.isAssignableFrom(valueClass)) {
        GeometryAttribute.appendString(buffer, value);
      } else if (Number.class.isAssignableFrom(valueClass)) {
        NumberAttribute.appendString(buffer, value);
      } else if (Date.class.isAssignableFrom(valueClass)) {
        DateTimeAttribute.appendString(buffer, value);
      } else {
        StringAttribute.appendString(buffer, value);
      }
    }

  }

  public static void appendString(final StringBuffer buffer, final String string) {
    if (string == null) {
    } else {

      buffer.append('\'');
      for (int i = 0; i < string.length(); i++) {
        final char c = string.charAt(i);
        if (c == '\'') {
          buffer.append("\\'");
        } else {
          buffer.append(c);
        }
      }
      buffer.append('\'');
    }
  }

  private String username;

  private String password;

  private final Map<String, String> typeLoadSql = new HashMap<String, String>();

  private final Map<String, String> tableIdTypeNameMap = new HashMap<String, String>();

  private final Map<String, String> typePathTableIdMap = new HashMap<String, String>();

  private String label;

  public FusionTablesDataObjectStore() {
    super(new ArrayDataObjectFactory());
  }

  private DataObjectReader createDataObjectReader(
    final DataObjectMetaData metaData,
    final String sql) {
    try {
      final HttpResponse response = executeQuery(sql);
      final InputStream in = response.getContent();
      final DataObjectReader reader = new FusionTablesDataObjectReader(
        metaData, in);
      return reader;
    } catch (final IOException e) {
      throw new RuntimeException("Unable to invoke query " + sql, e);
    }
  }

  @Override
  protected AbstractIterator<DataObject> createIterator(
    final Query query,
    final Map<String, Object> properties) {
    // TODO Auto-generated method stub
    return super.createIterator(query, properties);
  }

  private Reader<Map<String, Object>> createMapReader(final String sql) {
    try {
      final HttpResponse response = executeQuery(sql);
      final InputStream in = response.getContent();
      final InputStreamResource resource = new InputStreamResource("in.csv", in);
      final Reader<Map<String, Object>> reader = AbstractMapReaderFactory.mapReader(resource);
      return reader;
    } catch (final IOException e) {
      throw new RuntimeException("Unable to invoke query " + sql, e);
    }
  }

  public void createTable(final DataObjectMetaData metaData) {
    final StringBuffer sql = new StringBuffer("CREATE TABLE ");
    sql.append(metaData.getTypeName());
    sql.append(" (");
    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      if (i > 0) {
        sql.append(", ");
      }
      final String attributeName = metaData.getAttributeName(i);
      appendString(sql, attributeName);
      sql.append(": ");
      final DataType dataType = metaData.getAttributeType(i);
      final Class<?> attributeClass = dataType.getJavaClass();
      if (Geometry.class.isAssignableFrom(attributeClass)) {
        sql.append("LOCATION");
      } else if (Number.class.isAssignableFrom(attributeClass)) {
        sql.append("NUMBER");
      } else if (Date.class.isAssignableFrom(attributeClass)) {
        sql.append("DATETIME");
      } else {
        sql.append("STRING");
      }
    }
    sql.append(" )");
    executePostQuery(sql);
    refreshMetaData("");
  }

  public FusionTablesDataObjectWriter createWriter() {
    final FusionTablesDataObjectWriter writer = new FusionTablesDataObjectWriter(
      this);
    return writer;
  }

  @Override
  public void delete(final DataObject object) {
    if (object.getState() == DataObjectState.Persisted
      || object.getState() == DataObjectState.Modified) {
      object.setState(DataObjectState.Deleted);
      getWriter().write(object);
    }
  }

  protected HttpResponse executePostQuery(final CharSequence sql) {
    try {
      final HttpTransport transport = GoogleTransport.create();
      final GoogleHeaders headers = (GoogleHeaders)transport.defaultHeaders;
      headers.setApplicationName("fusiontables");
      headers.gdataVersion = "2";

      final ClientLogin authenticator = new ClientLogin();
      authenticator.authTokenType = "fusiontables";
      authenticator.username = username;
      authenticator.password = password;
      authenticator.authenticate().setAuthorizationHeader(transport);
      final HttpRequest request = transport.buildPostRequest();
      final GenericUrl url = new GenericUrl(SERVICE_URL);
      request.url = url;

      final Map<String, Object> data = new LinkedHashMap<String, Object>();
      data.put("sql", sql);

      final UrlEncodedContent content = new UrlEncodedContent();
      content.data = data;
      request.content = content;
      final HttpResponse response = request.execute();
      return response;
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException("Unable to encode query " + sql, e);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to invoke query " + sql, e);
    }
  }

  private HttpResponse executeQuery(final CharSequence sql) {
    try {
      final HttpTransport transport = GoogleTransport.create();
      final GoogleHeaders headers = (GoogleHeaders)transport.defaultHeaders;
      headers.setApplicationName("fusiontables");
      headers.gdataVersion = "2";

      final ClientLogin authenticator = new ClientLogin();
      authenticator.authTokenType = "fusiontables";
      authenticator.username = username;
      authenticator.password = password;
      authenticator.authenticate().setAuthorizationHeader(transport);

      final HttpRequest request = transport.buildGetRequest();
      final GenericUrl url = new GenericUrl(SERVICE_URL);
      url.set("sql", sql);
      request.url = url;
      final HttpResponse response = request.execute();
      return response;
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException("Unable to encode query " + sql, e);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to invoke query " + sql, e);
    }
  }

  private List<Attribute> getAttributes(final String tableId) {
    final List<Attribute> attributes = new ArrayList<Attribute>();
    final Reader<Map<String, Object>> reader = createMapReader("DESCRIBE "
      + tableId);
    attributes.add(new NumberAttribute("rowid"));
    for (final Map<String, Object> map : reader) {
      final String name = (String)map.get("name");
      final String type = (String)map.get("type");
      DataType dataType = DATA_TYPES.get(type);
      if (dataType == null) {
        dataType = DataTypes.STRING;
      }
      final Attribute attribute;
      if (dataType.equals(DataTypes.DECIMAL)) {
        attribute = new NumberAttribute(name);
      } else if (dataType.equals(DataTypes.DATE_TIME)) {
        attribute = new DateTimeAttribute(name);
      } else if (dataType.equals(DataTypes.GEOMETRY)) {
        attribute = new GeometryAttribute(name);
        attribute.setProperty(AttributeProperties.GEOMETRY_FACTORY,
          GeometryFactory.getFactory(4326));
      } else {
        attribute = new StringAttribute(name);
      }
      attributes.add(attribute);
    }
    return attributes;
  }

  protected String getLoadSql(final String typePath) {
    String sql = typeLoadSql.get(typePath);
    if (sql == null) {
      final DataObjectMetaData metaData = getMetaData(typePath);
      if (metaData == null) {
        return null;
      } else {
        if (metaData.getIdAttributeIndex() == -1) {
          throw new IllegalArgumentException(typePath
            + " does not have a primary key");
        }

        final String idAttributeName = metaData.getIdAttributeName();

        final StringBuffer sqlBuffer = new StringBuffer();
        final String tableId = getTableId(typePath);
        sqlBuffer.append("SELECT ");
        addColumnNames(sqlBuffer, metaData);
        sqlBuffer.append(" FROM " + tableId);
        sqlBuffer.append(" WHERE ");

        sqlBuffer.append(idAttributeName);
        sqlBuffer.append(" = ");

        sql = sqlBuffer.toString();
        typeLoadSql.put(typePath, sql);
      }
    }
    return sql;
  }

  public String getPassword() {
    return password;
  }

  public String getTableId(final String typePath) {
    return typePathTableIdMap.get(getTypeName(typePath));
  }

  protected String getTypeName(final String path) {
    final String localTypeName = PathUtil.getName(path);
    return localTypeName;
  }

  public String getUsername() {
    return username;
  }

  public synchronized FusionTablesDataObjectWriter getWriter() {
    FusionTablesDataObjectWriter writer = getSharedAttribute("writer");
    if (writer == null) {
      writer = createWriter();
      setSharedAttribute("writer", writer);
    }
    return writer;
  }

  @PostConstruct
  public void init() throws HttpResponseException, IOException {
  }

  @Override
  public void insert(final DataObject object) {
    getWriter().write(object);
  }

  @Override
  public DataObject load(final String typePath, final Object id) {
    final String sql = getLoadSql(typePath);
    if (sql == null) {
      return null;
    } else {
      final DataObjectMetaData metaData = getMetaData(typePath);
      final DataObjectReader reader = createDataObjectReader(metaData, sql
        + "'" + id.toString().replaceAll("'", "''") + "'");
      try {
        final Iterator<DataObject> iterator = reader.iterator();
        if (iterator.hasNext()) {
          final DataObject object = iterator.next();
          return object;
        } else {
          return null;
        }
      } finally {
        reader.close();
      }
    }
  }

  @Override
  protected void loadSchemaDataObjectMetaData(
    final DataObjectStoreSchema schema,
    final Map<String, DataObjectMetaData> metaDataMap) {
    final List<String> typePaths = new ArrayList<String>();
    final String namespace = schema.getPath();
    final Reader<Map<String, Object>> reader = createMapReader("SHOW TABLES");
    for (final Map<String, Object> map : reader) {
      final String tableId = (String)map.get("table id");
      final String tableName = (String)map.get("name");
      final String typePath = PathUtil.getPath(namespace, tableName);
      tableIdTypeNameMap.put(tableId, typePath);
      typePathTableIdMap.put(typePath, tableId);
      typePaths.add(typePath);
    }
    for (final String typePath : typePaths) {
      final String tableId = getTableId(typePath);
      final List<Attribute> attributes = getAttributes(tableId);
      final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(
        typePath, attributes);
      metaData.setIdAttributeName("rowid");
      metaDataMap.put(typePath, metaData);
    }
  }

  @Override
  protected void loadSchemas(final Map<String, DataObjectStoreSchema> schemaMap) {
    schemaMap.put("", new DataObjectStoreSchema(this, ""));
  }

  public Reader<DataObject> query(
    final String typePath,
    final BoundingBox boundingBox) {
    final BoundingBox envelope = boundingBox.convert(GeometryFactory.getFactory(4326));
    final DataObjectMetaData metaData = getMetaData(typePath);
    final String where = "ST_INTERSECTS(" + metaData.getGeometryAttributeName()
      + ", RECTANGLE(LATLNG(" + envelope.getMinY() + "," + envelope.getMinX()
      + "), LATLNG(" + envelope.getMaxY() + "," + envelope.getMaxX() + ")))";
    final Query query = new Query(typePath);
    query.setWhereClause(where);
    return query(query);
  }

  public Reader<DataObject> query(final String typePath, final Geometry geometry) {
    final Geometry projectedGeometry = GeometryProjectionUtil.perform(geometry,
      4326);
    return query(typePath, new BoundingBox(projectedGeometry));
  }

  // public Reader<DataObject> query(String path, String where,
  // Object... arguments) {
  // final DataObjectMetaData metaData = getMetaData(typePath);
  // final StringBuffer sql = new StringBuffer();
  // sql.append("SELECT ");
  // addColumnNames(sql, metaData);
  // sql.append(" FROM ");
  // final String tableId = getTableId(typePath);
  // sql.append(tableId);
  // if (where == null) {
  // if (arguments.length > 0) {
  // throw new IllegalArgumentException(
  // "Arguments cannot be specified if there is no where clause");
  // }
  // } else {
  // sql.append(" WHERE ");
  // if (arguments.length == 0) {
  // if (where.indexOf('?') > -1) {
  // throw new IllegalArgumentException(
  // "No arguments specified for a where clause with placeholders: "
  // + where);
  // } else {
  // sql.append(where);
  // }
  // } else {
  // Matcher matcher = PLACEHOLDER_PATTERN.matcher(where);
  // int i = 0;
  // while (matcher.find()) {
  // if (i >= arguments.length) {
  // throw new IllegalArgumentException(
  // "Not enough arguments for where clause with placeholders: "
  // + where);
  // }
  // final Object argument = arguments[i];
  // matcher.appendReplacement(sql, "");
  // appendString(sql, argument);
  // i++;
  // }
  // matcher.appendTail(sql);
  // }
  // }
  // final String sqlString = sql.toString();
  // return createDataObjectReader(metaData, sqlString);
  // }

  public void setPassword(final String password) {
    this.password = password;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  @Override
  public String toString() {
    if (label == null) {
      return super.toString();
    } else {
      return label;
    }
  }

  @Override
  public void update(final DataObject object) {
    final FusionTablesDataObjectWriter writer = getWriter();
    writer.write(object);
  }
}
