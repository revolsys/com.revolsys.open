package com.revolsys.gis.esri.gdb.file;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;

import org.springframework.asm.commons.TableSwitchGenerator;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.esri.gdb.file.convert.GeometryConverter;
import com.revolsys.gis.esri.gdb.file.swig.EnumRows;
import com.revolsys.gis.esri.gdb.file.swig.EsriFileGdb;
import com.revolsys.gis.esri.gdb.file.swig.Table;
import com.revolsys.util.CollectionUtil;
import com.vividsolutions.jts.geom.Envelope;

public class EsriFileGeodatabaseQueryIterator extends
  AbstractIterator<DataObject> {

  private DataObjectFactory dataObjectFactory;

  private Table table;

  private String fields;

  private String whereClause;

  private Envelope envelope;

  private DataObjectMetaData metaData;

  private EnumRows rows = new EnumRows();

  public EsriFileGeodatabaseQueryIterator(Table table) {
    this(table, "*", "", null);
  }

  public EsriFileGeodatabaseQueryIterator(Table table, String fields,
    String whereClause) {
    this(table, fields, whereClause, null);
  }

  public EsriFileGeodatabaseQueryIterator(Table table, String whereClause) {
    this(table, "*", whereClause, null);
  }

  public EsriFileGeodatabaseQueryIterator(Table table, String whereClause,
    Envelope envelope) {
    this(table, "*", whereClause, envelope);
  }

  public EsriFileGeodatabaseQueryIterator(Table table, String fields,
    String whereClause, Envelope envelope) {
    this.table = table;
    this.fields = fields;
    this.whereClause = whereClause;
    this.envelope = envelope;
  }

  public EsriFileGeodatabaseQueryIterator(Table table, Envelope envelope) {
    this(table, "*", "", envelope);
  }

  protected void doClose() {
    this.rows.Close();
    this.rows.delete();
    this.rows = null;
    this.table = null;
    this.fields = null;
    this.whereClause = null;
    this.envelope = null;
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
    // TODO Auto-generated method stub
    return null;
  }

  protected DataObjectMetaData getMetaData() {
    if (metaData == null) {
      hasNext();
    }
    return metaData;
  }

}
