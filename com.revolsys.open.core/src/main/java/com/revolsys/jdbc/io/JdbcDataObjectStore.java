package com.revolsys.jdbc.io;

import java.sql.Connection;
import java.sql.ResultSetMetaData;

import javax.sql.DataSource;
import javax.xml.namespace.QName;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreQueryReader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.Reader;
import com.vividsolutions.jts.geom.Geometry;

public interface JdbcDataObjectStore extends DataObjectStore {

  DataObjectStoreQueryReader createReader();

  JdbcWriter createWriter();

  Connection getConnection();

  DataSource getDataSource();

  String getGeneratePrimaryKeySql(DataObjectMetaData metaData);

  String getLabel();

  DataObjectMetaData getMetaData(
    QName tableName,
    ResultSetMetaData resultSetMetaData);

  Object getNextPrimaryKey(DataObjectMetaData metaData);

  Object getNextPrimaryKey(String typeName);

  void initialize();

  Reader<DataObject> query(QName typeName, Geometry geometry, String condition);

  void setDataSource(DataSource dataSource);

  void setLabel(String label);
}
