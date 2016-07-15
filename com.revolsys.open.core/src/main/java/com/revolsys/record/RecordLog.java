package com.revolsys.record;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.collection.map.ThreadSharedProperties;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.PathName;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Writer;
import com.revolsys.logging.Logs;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;

public class RecordLog implements BaseCloseable {
  private static final String LOG_MESSAGE = "LOG_MESSAGE";

  private static final String LOG_LOCALITY = "LOG_LOCALITY";

  private static final String KEY = RecordLog.class.getName();

  public static void error(final Class<?> logCategory, final String message, final Record record) {
    final RecordLog recordLog = getForThread();
    if (record == null) {
      Logs.error(logCategory, message + "\tnull");
    } else if (recordLog == null) {
      final RecordDefinition recordDefinition = record.getRecordDefinition();
      Logs.error(logCategory, message + "\t" + recordDefinition.getPath() + record.getIdentifier());
    } else {
      recordLog.error(message, record);
    }
  }

  public static RecordLog getForThread() {
    final RecordLog recordLog = ThreadSharedProperties.getProperty(KEY);
    return recordLog;
  }

  public static RecordLog recordLog() {
    RecordLog recordLog = getForThread();
    if (recordLog == null) {
      recordLog = new RecordLog();
      ThreadSharedProperties.setProperty(KEY, recordLog);
    }
    return recordLog;
  }

  private final Map<RecordDefinition, RecordDefinitionImpl> logRecordDefinitionMap = new HashMap<>();

  private Writer<Record> writer;

  private boolean usesLocality;

  public RecordLog() {
  }

  public RecordLog(final boolean usesLocality) {
    this.usesLocality = usesLocality;
  }

  public RecordLog(final Writer<Record> writer) {
    this.writer = writer;
  }

  @Override
  public void close() {
    final Writer<Record> writer = this.writer;
    if (writer != null) {
      writer.flush();
      writer.close();
    }
    this.logRecordDefinitionMap.clear();
  }

  public synchronized void error(final Object message, final Record record) {
    log(null, message, record, null);
  }

  public synchronized void error(final Object message, final Record record,
    final Geometry geometry) {
    log(null, message, record, geometry);
  }

  public synchronized void error(final String localityName, final Object message,
    final Record record) {
    log(localityName, message, record, null);
  }

  public synchronized void error(final String localityName, final Object message,
    final Record record, final Geometry geometry) {
    log(localityName, message, record, geometry);
  }

  public RecordDefinition getLogRecordDefinition(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final RecordDefinition logRecordDefinition = getLogRecordDefinition(recordDefinition);
    return logRecordDefinition;
  }

  public RecordDefinition getLogRecordDefinition(final RecordDefinition recordDefinition) {
    RecordDefinitionImpl logRecordDefinition = this.logRecordDefinitionMap.get(recordDefinition);
    if (logRecordDefinition == null) {
      final String path = recordDefinition.getPath();
      final String parentPath = PathUtil.getPath(path);
      final String tableName = PathUtil.getName(path);
      final String logTableName;
      if (tableName.toUpperCase().equals(tableName)) {
        logTableName = tableName + "_LOG";
      } else {
        logTableName = tableName + "_log";
      }
      final PathName logTypeName = PathName.newPathName(PathUtil.toPath(parentPath, logTableName));
      logRecordDefinition = new RecordDefinitionImpl(logTypeName);
      if (this.usesLocality) {
        logRecordDefinition.addField(LOG_LOCALITY, DataTypes.STRING, 255, false);
      }
      logRecordDefinition.addField(LOG_MESSAGE, DataTypes.STRING, 255, true);
      for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
        final FieldDefinition logFieldDefinition = new FieldDefinition(fieldDefinition);
        final DataType dataType = logFieldDefinition.getDataType();
        if (recordDefinition.getGeometryField() == fieldDefinition) {
          logRecordDefinition.addField("GEOMETRY", dataType);
        } else {
          logRecordDefinition.addField(new FieldDefinition(fieldDefinition));
        }
      }
      logRecordDefinition.setGeometryFactory(recordDefinition.getGeometryFactory());
      this.logRecordDefinitionMap.put(recordDefinition, logRecordDefinition);
    }
    return logRecordDefinition;
  }

  public Writer<Record> getWriter() {
    return this.writer;
  }

  private void log(final Object localityName, final Object message, final Record record,
    Geometry geometry) {
    final Writer<Record> writer = this.writer;
    if (writer != null) {
      final RecordDefinition logRecordDefinition = getLogRecordDefinition(record);
      final Record logRecord = new ArrayRecord(logRecordDefinition, record);
      if (geometry == null) {
        geometry = record.getGeometry();
      }
      logRecord.setGeometryValue(geometry);
      logRecord.setValue(LOG_LOCALITY, localityName);
      logRecord.setValue(LOG_MESSAGE, message);
      synchronized (writer) {
        writer.write(logRecord);
      }
    }
  }

  public void setWriter(final Writer<Record> writer) {
    this.writer = writer;
  }

  @Override
  public String toString() {
    if (this.writer == null) {
      return super.toString();
    } else {
      return this.writer.toString();
    }
  }
}
