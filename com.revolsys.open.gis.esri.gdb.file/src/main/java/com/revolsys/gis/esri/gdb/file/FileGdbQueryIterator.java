package com.revolsys.gis.esri.gdb.file;

import java.util.NoSuchElementException;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.property.AttributeProperties;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.esri.gdb.file.capi.swig.EnumRows;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.gis.esri.gdb.file.capi.swig.Table;
import com.revolsys.gis.esri.gdb.file.capi.type.AbstractFileGdbAttribute;
import com.revolsys.gis.esri.gdb.file.convert.GeometryConverter;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;

public class FileGdbQueryIterator extends AbstractIterator<Record> {

  private RecordFactory recordFactory;

  private Table table;

  private String fields;

  private String sql;

  private BoundingBox boundingBox;

  private RecordDefinition recordDefinition;

  private CapiFileGdbRecordStore recordStore;

  private EnumRows rows;

  private final String typePath;

  private final int offset;

  private final int limit;

  private int count;

  FileGdbQueryIterator(final CapiFileGdbRecordStore recordStore,
    final String typePath) {
    this(recordStore, typePath, "*", "", null, 0, -1);
  }

  FileGdbQueryIterator(final CapiFileGdbRecordStore recordStore,
    final String typePath, final String whereClause) {
    this(recordStore, typePath, "*", whereClause, null, 0, -1);
  }

  FileGdbQueryIterator(final CapiFileGdbRecordStore recordStore,
    final String typePath, final String whereClause,
    final BoundingBox boundingBox, final Query query, final int offset,
    final int limit) {
    this(recordStore, typePath, "*", whereClause, boundingBox, offset, limit);
    final RecordFactory factory = query.getProperty("recordFactory");
    if (factory != null) {
      this.recordFactory = factory;
    }
  }

  FileGdbQueryIterator(final CapiFileGdbRecordStore recordStore,
    final String typePath, final String fields, final String sql,
    final BoundingBox boundingBox, final int offset, final int limit) {
    this.recordStore = recordStore;
    this.typePath = typePath;
    this.recordDefinition = recordStore.getRecordDefinition(typePath);
    this.table = recordStore.getTable(typePath);
    this.fields = fields;
    this.sql = sql;
    setBoundingBox(boundingBox);
    this.recordFactory = recordStore.getRecordFactory();
    this.offset = offset;
    this.limit = limit;
  }

  @Override
  protected void doClose() {
    if (recordStore != null) {
      try {
        recordStore.closeEnumRows(rows);
      } catch (final Throwable e) {
      } finally {
        boundingBox = null;
        recordStore = null;
        fields = null;
        recordDefinition = null;
        rows = null;
        sql = null;
        table = null;
      }
    }
  }

  @Override
  protected void doInit() {
    if (recordDefinition != null) {
      synchronized (recordStore) {
        if (boundingBox == null) {
          if (sql.startsWith("SELECT")) {
            rows = recordStore.query(sql, true);
          } else {
            rows = recordStore.search(table, fields, sql, true);
          }
        } else {
          BoundingBox boundingBox = this.boundingBox;
          if (boundingBox.getWidth() == 0) {
            boundingBox = boundingBox.expand(1, 0);
          }
          if (boundingBox.getHeight() == 0) {
            boundingBox = boundingBox.expand(0, 1);
          }
          final com.revolsys.gis.esri.gdb.file.capi.swig.Envelope envelope = GeometryConverter.toEsri(boundingBox);
          rows = recordStore.search(table, fields, sql, envelope, true);
        }
      }
    }
  }

  protected RecordDefinition getRecordDefinition() {
    if (recordDefinition == null) {
      hasNext();
    }
    return recordDefinition;
  }

  @Override
  protected Record getNext() throws NoSuchElementException {
    if (rows == null || recordDefinition == null) {
      throw new NoSuchElementException();
    } else {
      Row row = null;
      while (offset > 0 && count < offset) {
        recordStore.nextRow(rows);
        count++;
      }
      if (limit > -1 && count >= offset + limit) {
        throw new NoSuchElementException();
      }
      row = recordStore.nextRow(rows);
      count++;
      if (row == null) {
        throw new NoSuchElementException();
      } else {
        try {
          final Record object = recordFactory.createRecord(recordDefinition);
          recordStore.addStatistic("query", object);
          object.setState(RecordState.Initalizing);
          for (final Attribute attribute : recordDefinition.getAttributes()) {
            final String name = attribute.getName();
            final AbstractFileGdbAttribute esriAttribute = (AbstractFileGdbAttribute)attribute;
            final Object value;
            synchronized (recordStore) {
              value = esriAttribute.getValue(row);
            }
            object.setValue(name, value);
          }
          object.setState(RecordState.Persisted);
          return object;

        } finally {
          recordStore.closeRow(row);
        }
      }
    }
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    final RecordDefinition recordDefinition = this.recordDefinition;
    if (recordDefinition != null) {
      this.boundingBox = boundingBox;

      if (boundingBox != null) {
        final Attribute geometryAttribute = recordDefinition.getGeometryAttribute();
        if (geometryAttribute != null) {
          final GeometryFactory geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);
          if (geometryFactory != null) {
            this.boundingBox = boundingBox.convert(geometryFactory);
          }
        }
      }
    }
  }

  public void setSql(final String whereClause) {
    this.sql = whereClause;
  }

  @Override
  public String toString() {
    return typePath.toString();
  }

}
