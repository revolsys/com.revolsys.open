package com.revolsys.gis.data.io;

import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataFactory;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.vividsolutions.jts.geom.Geometry;

public interface DataObjectStore extends DataObjectMetaDataFactory {
  void addCodeTable(CodeTable<?> codeTable);

  void close();

  DataObject create(QName typeName);

  DataObject create(DataObjectMetaData metaData);

  Number createPrimaryId(QName typeName);

  Writer<DataObject> createWriter();

  void delete(DataObject object);

  void deleteAll(Collection<DataObject> objects);

  <T> CodeTable<T> getCodeTable(QName typeName);

  <T> CodeTable<T> getCodeTableByColumn(String columnName);

  DataObjectFactory getDataObjectFactory();

  String getLabel();

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

  Reader<DataObject> query(QName typeName);

  Reader<DataObject> query(QName typeName, BoundingBox boundingBox);

  Reader<DataObject> query(QName typeName, Geometry geometry);

  Reader<DataObject> query(QName typeName, String where, Object... arguments);

  DataObject queryFirst(QName typeName, String where, Object... arguments);

  void setLabel(String label);

  void update(DataObject object);

  void updateAll(Collection<DataObject> objects);
}
