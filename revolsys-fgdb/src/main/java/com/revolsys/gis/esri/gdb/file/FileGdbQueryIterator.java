package com.revolsys.gis.esri.gdb.file;

import java.util.NoSuchElementException;

import org.jeometry.common.logging.Logs;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.esri.filegdb.jni.Row;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.esri.gdb.file.capi.type.AbstractFileGdbFieldDefinition;
import com.revolsys.gis.esri.gdb.file.convert.GeometryConverter;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.RecordState;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Strings;
import com.revolsys.util.count.LabelCounters;

public class FileGdbQueryIterator extends AbstractIterator<Record> implements RecordReader {

  private BoundingBox boundingBox;

  private int count;

  private String fields;

  private int limit = Integer.MAX_VALUE;

  private int offset;

  private RecordDefinition recordDefinition;

  private RecordFactory<Record> recordFactory;

  private FileGdbRecordStore recordStore;

  private FileGdbEnumRowsIterator rows;

  private String sql;

  private LabelCounters labelCountMap;

  private TableWrapper table;

  private boolean closed = false;

  FileGdbQueryIterator(final FileGdbRecordStore recordStore,
    final FileGdbRecordDefinition recordDefinition, final String whereClause) {
    this(recordStore, recordDefinition, "*", whereClause, null, 0, -1);
  }

  FileGdbQueryIterator(final FileGdbRecordStore recordStore,
    final FileGdbRecordDefinition recordDefinition, final String whereClause,
    final BoundingBox boundingBox, final Query query, final int offset, final int limit) {
    this(recordStore, recordDefinition, "*", whereClause, boundingBox, offset, limit);
    final RecordFactory<Record> recordFactory = query.getRecordFactory();
    if (recordFactory != null) {
      this.recordFactory = recordFactory;
    }
  }

  FileGdbQueryIterator(final FileGdbRecordStore recordStore,
    final FileGdbRecordDefinition recordDefinition, final String fields, final String sql,
    final BoundingBox boundingBox, final int offset, final int limit) {
    this.sql = sql;
    this.recordDefinition = recordDefinition;
    if (this.recordDefinition == null) {
      this.closed = true;
    } else {
      this.recordStore = recordStore;
      this.table = recordDefinition.getTableReference().connect();
      if ("*".equals(fields)) {
        this.fields = Strings.toString(this.recordDefinition.getFieldNames());
      } else {
        this.fields = fields;
      }
      setBoundingBox(boundingBox);
      this.recordFactory = recordStore.getRecordFactory();
      this.offset = offset;
      if (limit >= 0) {
        this.limit = limit;
      }
    }
  }

  @Override
  protected void closeDo() {
    boolean close = true;
    if (this.closed || this.recordStore == null) {
      close = false;
    } else {
      this.closed = true;
    }
    if (close) {
      synchronized (this) {
        if (this.recordDefinition != null) {
          this.recordDefinition = null;
          try {
            try {
              final FileGdbEnumRowsIterator rows = this.rows;
              if (rows != null) {
                this.rows = null;
                rows.close();
              }
            } finally {
              final TableWrapper table = this.table;
              if (table != null) {
                this.table = null;
                table.close();
              }
            }
          } catch (final Throwable e) {
            Logs.error(this, "Error closing query: " + this.recordDefinition.getPathName(), e);
          } finally {
            this.boundingBox = null;
            this.recordStore = null;
            this.fields = null;
            this.rows = null;
            this.sql = null;
            this.table = null;
          }
        }
      }
    }
  }

  @Override
  protected synchronized Record getNext() throws NoSuchElementException {
    final FileGdbRecordStore recordStore = this.recordStore;
    final FileGdbEnumRowsIterator rows = this.rows;
    if (rows == null || this.closed) {
      throw new NoSuchElementException();
    } else {
      Row row = null;
      while (this.offset > 0 && this.count < this.offset) {
        row = rows.next();
        this.count++;
        if (this.closed) {
          throw new NoSuchElementException();
        }
      }
      if (this.count - this.offset >= this.limit) {
        throw new NoSuchElementException();
      }
      row = rows.next();
      this.count++;
      try {
        final Record record = this.recordFactory.newRecord(this.recordDefinition);
        if (this.labelCountMap == null) {
          recordStore.addStatistic("query", record);
        } else {
          this.labelCountMap.addCount(record);
        }
        record.setState(RecordState.INITIALIZING);
        for (final FieldDefinition field : this.recordDefinition.getFields()) {
          final String name = field.getName();
          final AbstractFileGdbFieldDefinition esriFieldDefinition = (AbstractFileGdbFieldDefinition)field;
          final Object value = esriFieldDefinition.getValue(row);
          record.setValue(name, value);
          if (this.closed) {
            throw new NoSuchElementException();
          }
        }
        record.setState(RecordState.PERSISTED);
        if (this.closed) {
          throw new NoSuchElementException();
        }
        return record;
      } catch (final RuntimeException e) {
        if (this.closed) {
          throw new NoSuchElementException();
        } else {
          throw e;
        }
      } finally {
        row.delete();
      }
    }
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    if (this.recordDefinition == null) {
      hasNext();
    }
    return this.recordDefinition;
  }

  public LabelCounters getStatistics() {
    return this.labelCountMap;
  }

  @Override
  protected synchronized void initDo() {
    if (!this.closed) {
      if (this.boundingBox == null) {
        if (this.sql.startsWith("SELECT")) {
          this.rows = this.table.query(this.sql, true);
        } else {
          this.rows = this.table.search(this.fields, this.sql, true);
        }
      } else {
        BoundingBox boundingBox = this.boundingBox;
        if (boundingBox.getWidth() == 0) {
          boundingBox = boundingBox.bboxEdit(editor -> editor.expandDeltaX(1));
        }
        if (boundingBox.getHeight() == 0) {
          boundingBox = boundingBox.bboxEdit(editor -> editor.expandDeltaY(1));
        }
        final com.revolsys.esri.filegdb.jni.Envelope envelope = GeometryConverter
          .toEsri(boundingBox);
        String sql = this.sql;
        if ("1 = 1".equals(sql)) {
          sql = "";
        }
        this.rows = this.table.search(this.fields, sql, envelope, true);
      }
      if (this.rows == null) {
        close();
      }
    }
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    final RecordDefinition recordDefinition = this.recordDefinition;
    if (recordDefinition != null) {
      this.boundingBox = boundingBox;
      if (boundingBox != null) {
        final FieldDefinition geometryField = recordDefinition.getGeometryField();
        if (geometryField != null) {
          final GeometryFactory geometryFactory = geometryField.getGeometryFactory();
          if (geometryFactory != null) {
            this.boundingBox = boundingBox.bboxToCs(geometryFactory);
          }
        }
      }
    }
  }

  public void setStatistics(final LabelCounters labelCountMap) {
    this.labelCountMap = labelCountMap;
  }

  @Override
  public String toString() {
    return this.recordDefinition.getPathName() + " " + this.sql;
  }
}
