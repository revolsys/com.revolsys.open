package com.revolsys.gis.esri.gdb.file.arcobjects;

import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import com.esri.arcgis.geodatabase.ICursor;
import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;

public class FileGdbQueryIterator extends AbstractIterator<DataObject> {

  private final DataObjectFactory dataObjectFactory;

  private String fields;

  private String whereClause;

  private DataObjectMetaData metaData;

  private ICursor rows;

  private final QName typeName;

  FileGdbQueryIterator(final ArcObjectsFileGdbDataObjectStore dataStore,
    final QName typeName) {
    this(dataStore, typeName, "*", "");
  }

  FileGdbQueryIterator(final ArcObjectsFileGdbDataObjectStore dataStore,
    final QName typeName, final String whereClause) {
    this(dataStore, typeName, "*", whereClause);
  }

  FileGdbQueryIterator(final ArcObjectsFileGdbDataObjectStore dataStore,
    final QName typeName, final String fields, final String whereClause) {
    this.typeName = typeName;
    this.metaData = dataStore.getMetaData(typeName);
    if (metaData == null) {
      throw new IllegalArgumentException("Unknown type " + typeName);
    }
    this.fields = fields;
    this.whereClause = whereClause;
    this.dataObjectFactory = dataStore.getDataObjectFactory();
  }

  @Override
  protected void doClose() {
    rows = null;
    metaData = null;
    fields = null;
    whereClause = null;
  }

  @Override
  protected void doInit() {
    rows = ArcObjectsFileGdbDataObjectStore.invoke(ArcObjectsUtil.class,
      "search", metaData, fields, whereClause);
  }

  protected DataObjectMetaData getMetaData() {
    if (metaData == null) {
      hasNext();
    }
    return metaData;
  }

  @Override
  protected DataObject getNext() throws NoSuchElementException {
    return ArcObjectsFileGdbDataObjectStore.invoke(ArcObjectsUtil.class,
      "getNext", rows, metaData, dataObjectFactory);
  }

  public void setWhereClause(final String whereClause) {
    this.whereClause = whereClause;
  }

  @Override
  public String toString() {
    return typeName.toString();
  }

}
