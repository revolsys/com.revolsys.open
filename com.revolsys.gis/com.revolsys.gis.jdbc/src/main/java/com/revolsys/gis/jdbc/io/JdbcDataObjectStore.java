package com.revolsys.gis.jdbc.io;

import java.sql.Connection;
import java.sql.ResultSetMetaData;

import javax.sql.DataSource;
import javax.xml.namespace.QName;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.jdbc.data.model.property.JdbcCodeTableProperty;
import com.vividsolutions.jts.geom.Geometry;

public interface JdbcDataObjectStore extends DataObjectStore {

  long getNextPrimaryKey(String typeName);

  long getNextPrimaryKey(DataObjectMetaData metaData);

  void addCodeTable(JdbcCodeTableProperty jdbcCodeTableProperty);

  DataSource getDataSource();

  String getLabel();

  void setLabel(String label);

  void setDataSource(DataSource dataSource);

  void initialize();

  JdbcWriter createWriter();

  Connection getConnection();

  DataObjectMetaData getMetaData(QName tableName,
    ResultSetMetaData resultSetMetaData);

  String getGeneratePrimaryKeySql(DataObjectMetaData metaData);

  void setDataObjectFactory(DataObjectFactory featureDataObjectFactory);

  JdbcQueryReader createReader();

  Reader<DataObject> query(QName typeName, Geometry geometry, String condition);
}
