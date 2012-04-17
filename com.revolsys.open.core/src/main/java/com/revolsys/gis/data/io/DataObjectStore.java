package com.revolsys.gis.data.io;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.collection.ResultPager;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataFactory;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.io.StatisticsMap;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.vividsolutions.jts.geom.Geometry;

public interface DataObjectStore extends DataObjectMetaDataFactory {
  void addCodeTable(CodeTable codeTable);

  void addStatistic(String name, DataObject object);

  void addStatistic(String name, QName typeName, int count);

  void close();

  DataObject create(DataObjectMetaData metaData);

  DataObject create(QName typeName);

  Object createPrimaryIdValue(QName typeName);

  Query createQuery(
    final QName typeName,
    String whereClause,
    final BoundingBox boundingBox);

  StatisticsMap getStatistics();

  DataObjectReader createReader(
    QName typeName,
    String query,
    List<Object> parameters);

  Writer<DataObject> createWriter();

  void delete(DataObject object);

  void deleteAll(Collection<DataObject> objects);

  CodeTable getCodeTable(QName typeName);

  CodeTable getCodeTableByColumn(String columnName);

  Map<String, CodeTable> getCodeTableByColumnMap();

  DataObjectFactory getDataObjectFactory();

  String getLabel();

  void setDataObjectFactory(DataObjectFactory featureDataObjectFactory);

  /**
   * Get the meta data for the specified type.
   * 
   * @param typeName The type name.
   * @return The meta data.
   */
  DataObjectMetaData getMetaData(QName typeName);

  DataObjectStoreSchema getSchema(final String schemaName);

  /**
   * Get the list of name space names provided by the data store.
   * 
   * @return The name space names.
   */
  List<DataObjectStoreSchema> getSchemas();

  /**
   * Get the list of type names (including name space) in the name space.
   * 
   * @param namespace The name space.
   * @return The type names.
   */
  List<QName> getTypeNames(String namespace);

  List<DataObjectMetaData> getTypes(String namespace);

  void insert(DataObject object);

  void insertAll(Collection<DataObject> objects);

  boolean isEditable(QName typeName);

  DataObject load(QName typeName, Object id);

  DataObject lock(QName typeName, Object id);

  ResultPager<DataObject> page(Query query);

  Reader<DataObject> query(List<Query> queries);

  Reader<DataObject> query(QName typeName);

  Reader<DataObject> query(QName typeName, BoundingBox boundingBox);

  Reader<DataObject> query(QName typeName, Geometry geometry);

  Reader<DataObject> query(Query... queries);

  DataObject queryFirst(Query query);

  void setLabel(String label);

  void update(DataObject object);

  void updateAll(Collection<DataObject> objects);

  void initialize();

  DataObjectMetaData getMetaData(DataObjectMetaData metaData);
}
