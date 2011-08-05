package com.revolsys.gis.esri.gdb.file.arcobjects;

import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IQueryFilter;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geodatabase.ITable;
import com.esri.arcgis.geodatabase.QueryFilter;
import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.esri.gdb.file.arcobjects.type.AbstractFileGdbAttribute;

public class FileGdbQueryIterator extends AbstractIterator<DataObject> {

  private final DataObjectFactory dataObjectFactory;

  private ITable table;

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
    this.table = dataStore.getITable(typeName);
    this.fields = fields;
    this.whereClause = whereClause;
    this.dataObjectFactory = dataStore.getDataObjectFactory();
  }

  @Override
  protected void doClose() {
    rows = null;
    table = null;
    metaData = null;
    fields = null;
    whereClause = null;
  }

  @Override
  protected void doInit() {
    try {
      final IQueryFilter query = new QueryFilter();
      query.setSubFields(fields);
      query.setWhereClause(whereClause);
      rows = table.ITable_search(query, true);
    } catch (final Exception e) {
      throw new RuntimeException("Unable to perform search", e);
    }
  }

  protected DataObjectMetaData getMetaData() {
    if (metaData == null) {
      hasNext();
    }
    return metaData;
  }

  @Override
  protected DataObject getNext() throws NoSuchElementException {
    try {
      final IRow row = rows.nextRow();
      if (row == null) {
        throw new NoSuchElementException();
      } else {
        final DataObject object = dataObjectFactory.createDataObject(metaData);
        for (final Attribute attribute : metaData.getAttributes()) {
          final String name = attribute.getName();
          final AbstractFileGdbAttribute esriAttribute = (AbstractFileGdbAttribute)attribute;
          final Object value = esriAttribute.getValue(row);
          object.setValue(name, value);
        }
        object.setState(DataObjectState.Persisted);
        return object;
      }
    } catch (NoSuchElementException e) {
      throw e;
    } catch (final Exception e) {
      throw new RuntimeException("Unable to get next row", e);
    }
  }

  public void setWhereClause(final String whereClause) {
    this.whereClause = whereClause;
  }

  @Override
  public String toString() {
    return typeName.toString();
  }

}
