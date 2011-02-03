package com.revolsys.jump.jdbc;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.openjump.core.model.OpenJumpTaskProperties;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.jdbc.io.DataSourceFactory;
import com.revolsys.gis.jdbc.io.JdbcDataObjectStore;
import com.revolsys.gis.jdbc.io.JdbcFactory;
import com.revolsys.jump.model.FeatureDataObjectFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.datastore.AdhocQuery;
import com.vividsolutions.jump.datastore.DataStoreConnection;
import com.vividsolutions.jump.datastore.DataStoreException;
import com.vividsolutions.jump.datastore.DataStoreMetadata;
import com.vividsolutions.jump.datastore.FilterQuery;
import com.vividsolutions.jump.datastore.Query;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.FeatureInputStream;
import com.vividsolutions.jump.parameter.ParameterList;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Task;

public class JdbcDataStoreConnection implements DataStoreConnection {

  private JdbcDataStoreMetaData metaData;

  private JdbcDataObjectStore dataStore;

  private DataSource dataSource;

  private String schema;

  private Task task;

  private DataSourceFactory dataSourceFactory;

  public JdbcDataStoreConnection(WorkbenchContext workbenchContext,
    final ParameterList params) throws DataStoreException {
    this.task = workbenchContext.getTask();
    String url = (String)params.getParameter(JdbcDataStoreDriver.URL);
    schema = params.getParameter(JdbcDataStoreDriver.SCHEMA)
      .toString();

    String username = (String)params.getParameter(JdbcDataStoreDriver.USER);
    String password = (String)params.getParameter(JdbcDataStoreDriver.PASSWORD);
    if (schema == null) {
      schema = username;
    }

    Map<String, Object> config = new HashMap<String, Object>();
    config.put("url", url);
    config.put("username", username);
    config.put("password", password);

    try {
      dataSourceFactory = JdbcFactory.getDataSourceFactory(url);
      dataSource = dataSourceFactory.createDataSource(config);
      dataStore = JdbcFactory.createDataObjectStore(dataSource);
      dataStore.setDataObjectFactory(new FeatureDataObjectFactory());

      dataStore.initialize();
      metaData = new JdbcDataStoreMetaData(dataStore, schema);
    } catch (SQLException e) {
      throw new DataStoreException(e);
    }
  }

  public JdbcDataObjectStore getDataStore() {
    return dataStore;
  }

  public void close() throws DataStoreException {
    try {
      dataSourceFactory.closeDataSource(dataSource);
    } finally {
      dataSource = null;
      dataStore = null;
      metaData = null;
    }
  }

  public FeatureInputStream execute(final Query query) {
    if (query instanceof FilterQuery) {
      return executeFilterQuery((FilterQuery)query);
    }
    if (query instanceof AdhocQuery) {
      return executeAdhocQuery((AdhocQuery)query);
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
    FeatureSchema featureSchema = metaData.getFeatureSchema(datasetName,
      query.getGeometryAttributeName());
    QName typeName = new QName(schema, datasetName);
    Geometry filterGeometry = query.getFilterGeometry();
    String condition = query.getCondition();
    GeometryFactory geometryFactory = task.getProperty(OpenJumpTaskProperties.GEOMETRY_FACTORY);
    JdbcDataObjectStoreInputStream ifs = new JdbcDataObjectStoreInputStream(
      dataStore, geometryFactory, typeName, featureSchema, filterGeometry,
      condition);
    return ifs;
  }

  public FeatureInputStream executeAdhocQuery(final AdhocQuery query) {
    query.getQuery();

    return null;
  }

  public DataStoreMetadata getMetadata() {
    return metaData;
  }

  public boolean isClosed() throws DataStoreException {
    return dataSource == null;
  }
}
