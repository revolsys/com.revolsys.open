package com.revolsys.gis.ecsv.service.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.io.AbstractDataObjectStore;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.ecsv.service.EcsvServiceConstants;
import com.revolsys.io.FileUtil;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.io.ecsv.EcsvConstants;
import com.revolsys.io.ecsv.EcsvIoFactory;
import com.revolsys.spring.InputStreamResource;
import com.vividsolutions.jts.geom.Geometry;

public class EcsvDataObjectStore extends AbstractDataObjectStore {
  public static final EcsvDataObjectStore create(final URI uri) {
    return new EcsvDataObjectStore(uri, new ArrayDataObjectFactory());
  }

  public Writer<DataObject> createWriter() {
    return null;
  }

  public static final EcsvDataObjectStore create(
    final URI uri,
    final DataObjectFactory factory) {
    return new EcsvDataObjectStore(uri, factory);
  }

  public static final EcsvDataObjectStore create(
    final URI uri,
    final String username,
    final String password) {
    final ArrayDataObjectFactory factory = new ArrayDataObjectFactory();
    return new EcsvDataObjectStore(uri, username, password, factory);
  }

  public static final EcsvDataObjectStore create(
    final URI uri,
    final String username,
    final String password,
    final DataObjectFactory factory) {
    return new EcsvDataObjectStore(uri, username, password, factory);
  }

  @Override
  public int getRowCount(Query query) {
    throw new UnsupportedOperationException();
  }
  private final Map<String, String> namespacePaths = new HashMap<String, String>();

  private final String password;

  private final EcsvIoFactory ioFactory = new EcsvIoFactory();

  private final URI uri;

  private final String username;

  public EcsvDataObjectStore(final URI uri,
    final DataObjectFactory dataObjectFactory) {
    this(uri, null, null, dataObjectFactory);
  }

  public EcsvDataObjectStore(final URI uri, final String username,
    final String password, final DataObjectFactory dataObjectFactory) {
    super(dataObjectFactory);
    this.uri = uri;
    this.username = username;
    this.password = password;
  }

  protected DataObjectReader createReader(final String path) {
    final Map<String, String> params = Collections.emptyMap();
    return createReader(path, params);

  }

  protected DataObjectReader createReader(
    final String path,
    final Map<String, String> parameters) {

    final HttpClient client = new HttpClient();
    if (username != null) {
      final HttpClientParams params = client.getParams();
      params.setAuthenticationPreemptive(true);
      final Credentials credentials = new UsernamePasswordCredentials(username,
        password);
      final HttpState state = client.getState();
      final AuthScope scope = new AuthScope(uri.getHost(), uri.getPort(),
        AuthScope.ANY_REALM);
      state.setCredentials(scope, credentials);
    }
    String requestUri = uri.toASCIIString();
    if (!requestUri.endsWith("/")) {
      requestUri += "/";
    }
    if (path.length() != 0) {
      requestUri += path + "." + EcsvConstants.FILE_EXTENSION;
    }
    final GetMethod method = new GetMethod(requestUri);

    if (!parameters.isEmpty()) {
      final NameValuePair[] params = new NameValuePair[parameters.size()];
      int i = 0;
      for (final Entry<String, String> parameter : parameters.entrySet()) {
        params[i] = new NameValuePair(parameter.getKey(), parameter.getValue());
        i++;
      }
      method.setQueryString(params);
    }
    try {
      final int statusCode = client.executeMethod(method);
      if (statusCode == HttpStatus.SC_OK) {
        String fileName = FileUtil.getFileName(path);
        final InputStream in = method.getResponseBodyAsStream();
        return (DataObjectReader)ioFactory.createDataObjectReader(
          new InputStreamResource(fileName, in), getDataObjectFactory());
      } else {
        throw new IllegalArgumentException("Unnable to connect to server: "
          + statusCode);
      }
    } catch (final HttpException e) {
      throw new IllegalArgumentException("Unnable to connect to server: "
        + e.getMessage(), e);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unnable to read data from server: "
        + e.getMessage(), e);
    }
  }

  private String getPath(final String namespaceUri) {
    String path = namespacePaths.get(namespaceUri);
    if (path == null) {
      getSchemas();
      path = namespacePaths.get(namespaceUri);
      if (path == null) {
        return namespaceUri;
      }
    }
    return path;
  }

  // TODO move to load loadSchemaDataObjectMetaData
  protected DataObjectMetaData loadMetaData(final String typePath) {
    final String namespaceUri = PathUtil.getPath(typePath);
    final String namespacePath = getPath(namespaceUri);
    final String name = PathUtil.getName(typePath);
    final String path = namespacePath + "/" + name;
    final DataObjectReader reader = createReader(path,
      Collections.singletonMap("action", "metaData"));
    final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(this,
      getSchema(namespaceUri), reader.getMetaData());
    reader.close();
    return metaData;
  }

  @Override
  protected void loadSchemaDataObjectMetaData(
    final DataObjectStoreSchema schema,
    Map<String, DataObjectMetaData> metaDataMap) {
  }

  @Override
  protected void loadSchemas(Map<String, DataObjectStoreSchema> schemaMap) {
    final Reader<DataObject> reader = createReader("");
    if (reader != null) {
      for (final DataObject object : reader) {
        final String path = object.getValue(EcsvServiceConstants.PATH_ATTR);
        final String namespaceUri = object.getValue(EcsvServiceConstants.NAMESPACE_URI_ATTR);
        namespacePaths.put(namespaceUri, path);
        schemaMap.put(namespaceUri, new DataObjectStoreSchema(this,
          namespaceUri));
      }
    }
  }

  // TODO move to load loadSchemaDataObjectMetaData
  protected List<String> loadTypeNames(final String namespaceUri) {
    final List<String> typePaths = new ArrayList<String>();
    final String path = getPath(namespaceUri);
    final Reader<DataObject> reader = createReader(path);
    if (reader != null) {
      for (final DataObject object : reader) {
        final String typePath = object.getValue(EcsvServiceConstants.TYPE_NAME_ATTR);
        typePaths.add(typePath);
      }
    }
    return typePaths;
  }

  public Reader<DataObject> query(final String typePath) {
    final String path = getPath(PathUtil.getPath(typePath)) + "/"
      + PathUtil.getName(typePath);
    return createReader(path);
  }

  public Reader<DataObject> query(
    final String typePath,
    final BoundingBox envelope) {
    final String path = getPath(PathUtil.getPath(typePath)) + "/"
      + PathUtil.getName(typePath);
    final Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(
      "filter",
      "intersects(GEOMETRY,rectangle(" + envelope.getMinX() + ","
        + envelope.getMinY() + "," + envelope.getMaxX() + ","
        + envelope.getMaxY() + "))");
    return createReader(path, parameters);
  }

  public Reader<DataObject> query(final String typePath, final Geometry geometry) {
    final BoundingBox boundingBox = new BoundingBox(geometry);
    return query(typePath, boundingBox);
  }
}
