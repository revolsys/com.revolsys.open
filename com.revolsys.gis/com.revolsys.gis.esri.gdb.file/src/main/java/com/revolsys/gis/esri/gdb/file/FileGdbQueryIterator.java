package com.revolsys.gis.esri.gdb.file;

import java.util.NoSuchElementException;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.esri.gdb.file.convert.GeometryConverter;
import com.revolsys.gis.esri.gdb.file.swig.EnumRows;
import com.revolsys.gis.esri.gdb.file.swig.Row;
import com.revolsys.gis.esri.gdb.file.swig.Table;
import com.revolsys.gis.esri.gdb.file.type.AbstractFileGdbAttribute;
import com.vividsolutions.jts.geom.Envelope;

public class FileGdbQueryIterator extends
  AbstractIterator<DataObject> {

  private final DataObjectFactory dataObjectFactory;

  private Table table;

  private String fields;

  private String whereClause;

  private Envelope envelope;

  private DataObjectMetaData metaData;

  private FileGdbDataObjectStore dataStore;

  private EnumRows rows;

   FileGdbQueryIterator(final DataObjectMetaData metaData,
    final FileGdbDataObjectStore dataStore, final Table table) {
    this(metaData, dataStore, table, "*", "", null);
  }

   FileGdbQueryIterator(final DataObjectMetaData metaData,
    final FileGdbDataObjectStore dataStore, final Table table,
    final Envelope envelope) {
    this(metaData, dataStore, table, "*", "", envelope);
  }

   FileGdbQueryIterator(final DataObjectMetaData metaData,
    final FileGdbDataObjectStore dataStore, final Table table,
    final String whereClause) {
    this(metaData, dataStore, table, "*", whereClause, null);
  }

   FileGdbQueryIterator(final DataObjectMetaData metaData,
    final FileGdbDataObjectStore dataStore, final Table table,
    final String whereClause, final Envelope envelope) {
    this(metaData, dataStore, table, "*", whereClause, envelope);
  }

   FileGdbQueryIterator(final DataObjectMetaData metaData,
    final FileGdbDataObjectStore dataStore, final Table table,
    final String fields, final String whereClause) {
    this(metaData, dataStore, table, fields, whereClause, null);
  }

  FileGdbQueryIterator(final DataObjectMetaData metaData,
    final FileGdbDataObjectStore dataStore, final Table table,
    final String fields, final String whereClause, final Envelope envelope) {
    this.metaData = metaData;
    this.dataStore = dataStore;
    this.table = table;
    this.fields = fields;
    this.whereClause = whereClause;
    this.envelope = envelope;
    this.dataObjectFactory = dataStore.getDataObjectFactory();
  }

  @Override
  protected void doClose() {
    try {
      if (rows != null) {
        try {
          rows.Close();
        } catch (final NullPointerException e) {
        }
        rows = null;
      }
      if (table != null) {
        dataStore.closeTable(table);
        table = null;
      }
      dataStore = null;
      metaData = null;
      fields = null;
      whereClause = null;
      envelope = null;
    } catch (final Throwable t) {
      t.printStackTrace();
    }
  }

  @Override
  protected void doInit() {
    if (envelope == null) {
      rows = table.search(fields, whereClause, true);
    } else {
      final com.revolsys.gis.esri.gdb.file.swig.Envelope envelope = GeometryConverter.toEsri(this.envelope);
      rows = table.search(fields, whereClause, envelope, true);
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
    final Row row = rows.next();
    if (row == null) {
      throw new NoSuchElementException();
    } else {
      try {
        final DataObject object = dataObjectFactory.createDataObject(metaData);
        for (final Attribute attribute : metaData.getAttributes()) {
          final String name = attribute.getName();
          final AbstractFileGdbAttribute esriAttribute = (AbstractFileGdbAttribute)attribute;
          final Object value = esriAttribute.getValue(row);
          object.setValue(name, value);
        }
        object.setState(DataObjectState.Persisted);
        return object;
      } finally {
        row.delete();
      }
    }
  }

}
