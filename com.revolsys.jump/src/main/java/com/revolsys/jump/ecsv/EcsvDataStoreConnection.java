package com.revolsys.jump.ecsv;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;

import com.revolsys.gis.ecsv.service.client.EcsvDataObjectStore;
import com.revolsys.jump.model.DataObjectFeature;
import com.revolsys.jump.model.FeatureDataObjectFactory;
import com.vividsolutions.jump.datastore.DataStoreConnection;
import com.vividsolutions.jump.datastore.DataStoreException;
import com.vividsolutions.jump.datastore.DataStoreMetadata;
import com.vividsolutions.jump.datastore.FilterQuery;
import com.vividsolutions.jump.datastore.Query;
import com.vividsolutions.jump.io.FeatureInputStream;
import com.vividsolutions.jump.parameter.ParameterList;

public class EcsvDataStoreConnection implements DataStoreConnection {

  private URI serverUri;

  private EcsvDataStoreMetaData metaData;

  private boolean closed;

  private String schema;

  private EcsvDataObjectStore client;

  public EcsvDataStoreConnection(final ParameterList params)
    throws DataStoreException {
    try {
      String url = (String)params.getParameter(EcsvDataStoreDriver.URL);

      schema = (String)params.getParameter(EcsvDataStoreDriver.SCHEMA);

      String user = (String)params.getParameter(EcsvDataStoreDriver.USER);
      String password = (String)params.getParameter(EcsvDataStoreDriver.PASSWORD);
      if (schema == null) {
        schema = user;
      }

      serverUri = new URI(url);
      client = EcsvDataObjectStore.create(serverUri, user, password,
        new FeatureDataObjectFactory());
      metaData = new EcsvDataStoreMetaData(client, schema);
    } catch (URISyntaxException e) {
      throw new DataStoreException(e.getMessage(), e);
    }
  }

  public void close() throws DataStoreException {
    closed = true;
  }

  public FeatureInputStream execute(final Query query) {
    if (query instanceof FilterQuery) {
      return executeFilterQuery((FilterQuery)query);
    }
    throw new IllegalArgumentException("Unsupported Query type");
  }

  /**
   * Executes a filter query. The SRID is optional for queries - it will be
   * determined automatically from the table metadata if not supplied.
   * 
   * @param query the query to execute
   * @return the results of the query
   */
  public FeatureInputStream executeFilterQuery(final FilterQuery query) {
    String datasetName = query.getDatasetName();
    QName ypeName = new QName(schema, datasetName);
    return new EcsvFeatureInputStream(client, ypeName, query);
  }

  public DataStoreMetadata getMetadata() {
    return metaData;
  }

  public boolean isClosed() throws DataStoreException {
    return closed;
  }

  public EcsvDataObjectStore getClient() {
    return client;
  }

}
