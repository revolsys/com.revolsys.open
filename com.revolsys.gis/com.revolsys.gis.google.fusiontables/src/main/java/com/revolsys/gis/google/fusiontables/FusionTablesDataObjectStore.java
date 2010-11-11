package com.revolsys.gis.google.fusiontables;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.googleapis.auth.clientlogin.ClientLogin;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.revolsys.csv.CsvMapIoFactory;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.AbstractDataObjectStore;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.MapReader;
import com.revolsys.io.Writer;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class FusionTablesDataObjectStore extends AbstractDataObjectStore {
  private static final Map<String, DataType> DATA_TYPES;

  static {
    Map<String, DataType> dataTypes = new HashMap<String, DataType>();
    dataTypes.put("string", DataTypes.STRING);
    dataTypes.put("number", DataTypes.DECIMAL);
    dataTypes.put("location", DataTypes.GEOMETRY);
    dataTypes.put("datetime", DataTypes.DATE_TIME);

    DATA_TYPES = Collections.unmodifiableMap(dataTypes);
  }

  private static final String SERVICE_URL = "http://www.google.com/fusiontables/api/query";

  private String username = "api.user@revolsys.com";

  private String password = "Tdnmatm1";

  private Map<QName, String> typeLoadSql = new HashMap<QName, String>();

  public FusionTablesDataObjectStore() {
    super(new ArrayDataObjectFactory());
  }

  protected String getLoadSql(
    final QName typeName) {
    String sql = typeLoadSql.get(typeName);
    if (sql == null) {
      final DataObjectMetaData metaData = getMetaData(typeName);
      if (metaData == null) {
        return null;
      } else {
        if (metaData.getIdAttributeIndex() == -1) {
          throw new IllegalArgumentException(typeName
            + " does not have a primary key");
        }

        final String idAttributeName = metaData.getIdAttributeName();

        final StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("SELECT * FROM " + typeName.getLocalPart());
        // JdbcQuery.addColumnsAndTableName(sqlBuffer, metaData, "T");
        sqlBuffer.append(" WHERE ");

        sqlBuffer.append(idAttributeName);
        sqlBuffer.append(" = ");

        sql = sqlBuffer.toString();
        typeLoadSql.put(typeName, sql);
      }
    }
    return sql;
  }

  @Override
  public DataObject load(
    final QName typeName,
    final Object id) {
    final String sql = getLoadSql(typeName);
    if (sql == null) {
      return null;
    } else {
      final DataObjectMetaData metaData = getMetaData(typeName);
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

  @PostConstruct
  public void init()
    throws HttpResponseException,
    IOException {
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(
    String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(
    String password) {
    this.password = password;
  }

  public Reader<DataObject> query(
    QName typeName) {
    // TODO Auto-generated method stub
    return null;
  }

  public Reader<DataObject> query(
    QName typeName,
    Envelope envelope) {
    // TODO Auto-generated method stub
    return null;
  }

  public Reader<DataObject> query(
    QName typeName,
    Geometry geometry) {
    // TODO Auto-generated method stub
    return null;
  }

  public DataObject query(
    QName typeName,
    String queryString,
    Object... arguments) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void loadSchemaDataObjectMetaData(
    DataObjectStoreSchema schema,
    Map<QName, DataObjectMetaData> metaDataMap) {
    List<QName> typeNames = new ArrayList<QName>();
    final String namespace = schema.getName();
    MapReader reader = createMapReader("SHOW TABLES");
    for (Map<String, Object> map : reader) {
      final String tableId = (String)map.get("table id");
      QName typeName = new QName(namespace, tableId);
      typeNames.add(typeName);
    }
    for (QName typeName : typeNames) {
      List<Attribute> attributes = getAttributes(typeName.getLocalPart());
      DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(typeName,
        attributes);
      metaDataMap.put(typeName, metaData);
    }
  }

  private MapReader createMapReader(
    final String sql) {
    try {
      final HttpResponse response = executeQuery(sql);
      final InputStream in = response.getContent();
      MapReader reader = new CsvMapIoFactory().createMapReader(in);
      return reader;
    } catch (IOException e) {
      throw new RuntimeException("Unable to invoke query " + sql, e);
    }
  }

  private DataObjectReader createDataObjectReader(
    DataObjectMetaData metaData,
    final String sql) {
    try {
      final HttpResponse response = executeQuery(sql);
      final InputStream in = response.getContent();
      DataObjectReader reader = new FusionTablesDataObjectReader(metaData, in);
      return reader;
    } catch (IOException e) {
      throw new RuntimeException("Unable to invoke query " + sql, e);
    }
  }

  private HttpResponse executeQuery(
    final String sql) {
    try {
      HttpTransport transport = GoogleTransport.create();
      GoogleHeaders headers = (GoogleHeaders)transport.defaultHeaders;
      headers.setApplicationName("fusiontables");
      headers.gdataVersion = "2";

      ClientLogin authenticator = new ClientLogin();
      authenticator.authTokenType = "fusiontables";
      authenticator.username = username;
      authenticator.password = password;
      authenticator.authenticate().setAuthorizationHeader(transport);
      final HttpRequest request = transport.buildGetRequest();
      GenericUrl url = new GenericUrl(
        "http://www.google.com/fusiontables/api/query");
      url.set("sql", sql);
      request.url = url;
      final HttpResponse response = request.execute();
      return response;
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Unable to encode query " + sql, e);
    } catch (IOException e) {
      throw new RuntimeException("Unable to invoke query " + sql, e);
    }
  }

  public Writer<DataObject> createWriter() {
    // TODO Auto-generated method stub
    return null;
  }

  private List<Attribute> getAttributes(
    String tableId) {
    List<Attribute> attributes = new ArrayList<Attribute>();
    MapReader reader = createMapReader("DESCRIBE " + tableId);
    for (Map<String, Object> map : reader) {
      final String name = (String)map.get("name");
      final String type = (String)map.get("type");
      DataType dataType = DATA_TYPES.get(type);
      if (dataType == null) {
        dataType = DataTypes.STRING;
      }
      final Attribute attribute = new Attribute(name, dataType, false);
      if (Geometry.class.isAssignableFrom(dataType.getJavaClass())) {
        attribute.setProperty(AttributeProperties.GEOMETRY_FACTORY,
          GeometryFactory.getFactory(4326));
      }
      attributes.add(attribute);
    }
    return attributes;
  }

  @Override
  protected void loadSchemas(
    Map<String, DataObjectStoreSchema> schemaMap) {
    schemaMap.put("", new DataObjectStoreSchema(this, ""));
  }
}
