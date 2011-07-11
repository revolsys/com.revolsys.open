package com.revolsys.gis.esri.gdb.file;

import java.util.NoSuchElementException;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.esri.gdb.file.convert.GeometryConverter;
import com.revolsys.gis.esri.gdb.file.swig.EnumRows;
import com.revolsys.gis.esri.gdb.file.swig.EsriFileGdb;
import com.revolsys.gis.esri.gdb.file.swig.Geodatabase;
import com.revolsys.gis.esri.gdb.file.swig.Row;
import com.revolsys.gis.esri.gdb.file.swig.Table;
import com.revolsys.gis.esri.gdb.file.type.AbstractEsriFileGeodatabaseAttribute;
import com.vividsolutions.jts.geom.Envelope;

public class EsriFileGeodatabaseQueryIterator extends
  AbstractIterator<DataObject> {

  private DataObjectFactory dataObjectFactory = new ArrayDataObjectFactory();

  private Geodatabase geodatabase;

  private Table table;

  private String fields;

  private String whereClause;

  private Envelope envelope;

  private DataObjectMetaData metaData;

  private EnumRows rows = new EnumRows();

  public EsriFileGeodatabaseQueryIterator(DataObjectMetaData metaData,
    Geodatabase geodatabase, Table table) {
    this(metaData, geodatabase, table, "*", "", null);
  }

  public EsriFileGeodatabaseQueryIterator(DataObjectMetaData metaData,
    Geodatabase geodatabase, Table table, String fields, String whereClause) {
    this(metaData, geodatabase, table, fields, whereClause, null);
  }

  public EsriFileGeodatabaseQueryIterator(DataObjectMetaData metaData,
    Geodatabase geodatabase, Table table, String whereClause) {
    this(metaData, geodatabase, table, "*", whereClause, null);
  }

  public EsriFileGeodatabaseQueryIterator(DataObjectMetaData metaData,
    Geodatabase geodatabase, Table table, String whereClause, Envelope envelope) {
    this(metaData, geodatabase, table, "*", whereClause, envelope);
  }

  public EsriFileGeodatabaseQueryIterator(DataObjectMetaData metaData,
    Geodatabase geodatabase, Table table, String fields, String whereClause,
    Envelope envelope) {
    this.metaData = metaData;
    this.geodatabase = geodatabase;
    this.table = table;
    this.fields = fields;
    this.whereClause = whereClause;
    this.envelope = envelope;
  }

  public EsriFileGeodatabaseQueryIterator(DataObjectMetaData metaData,
    Geodatabase geodatabase, Table table, Envelope envelope) {
    this(metaData, geodatabase, table, "*", "", envelope);
  }

  protected void doClose() {
    try {
      if (rows != null) {
        try {
          rows.Close();
        } catch (NullPointerException e) {
        }
        rows = null;
      }
      if (table != null) {
        try {
          geodatabase.CloseTable(table);
        } catch (NullPointerException e) {
        }
        table = null;
      }
      geodatabase = null;
      metaData = null;
      fields = null;
      whereClause = null;
      envelope = null;
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  @Override
  protected void doInit() {
    if (envelope == null) {
      int result = table.Search(fields, whereClause, true, rows);
      if (result != 0) {
        throw new RuntimeException("Unable to search " + fields + " WHERE "
          + whereClause + ":" + EsriFileGdb.getErrorDescription(result));
      }
    } else {
      final com.revolsys.gis.esri.gdb.file.swig.Envelope envelope = GeometryConverter.toEsri(this.envelope);
      int result = table.Search(fields, whereClause, envelope, true, rows);
      if (result != 0) {
        throw new RuntimeException("Unable to search " + fields + " WHERE "
          + whereClause + " " + envelope + ":"
          + EsriFileGdb.getErrorDescription(result));
      }
    }
  }

  @Override
  protected DataObject getNext() throws NoSuchElementException {
    Row row = rows.next();
    if (row == null) {
      throw new NoSuchElementException();
    } else {
      try {
        DataObject object = dataObjectFactory.createDataObject(metaData);
        for (Attribute attribute : metaData.getAttributes()) {
          String name = attribute.getName();
          AbstractEsriFileGeodatabaseAttribute esriAttribute = (AbstractEsriFileGeodatabaseAttribute)attribute;
          Object value = esriAttribute.getValue(row);
          object.setValue(name, value);
        }
        return object;
      } finally {
        row.delete();
      }
    }
  }

  protected DataObjectMetaData getMetaData() {
    if (metaData == null) {
      hasNext();
    }
    return metaData;
  }

}
