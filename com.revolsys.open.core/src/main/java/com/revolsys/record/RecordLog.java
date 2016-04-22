package com.revolsys.record;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.collection.map.ThreadSharedProperties;
import com.revolsys.datatype.DataTypes;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.PathName;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Writer;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;

public class RecordLog implements BaseCloseable {

  private static final String KEY = RecordLog.class.getName();

  public static void error(final Class<?> logCategory, final String message, final Record record) {
    final RecordLog recordLog = getForThread();
    if (record == null) {
      final Logger log = LoggerFactory.getLogger(logCategory);
      log.error(message + "\tnull");
    } else if (recordLog == null) {
      final RecordDefinition recordDefinition = record.getRecordDefinition();
      final Logger log = LoggerFactory.getLogger(logCategory);
      log.error(message + "\t" + recordDefinition.getPath() + record.getIdentifier());
    } else {
      recordLog.error(message, record);
    }
  }

  public static RecordLog getForThread() {
    final RecordLog recordLog = ThreadSharedProperties.getProperty(KEY);
    return recordLog;
  }

  public static void info(final Class<?> logCategory, final String message, final Record record) {
    final RecordLog recordLog = getForThread();
    if (record == null) {
      final Logger log = LoggerFactory.getLogger(logCategory);
      log.info(message + "\tnull");
    } else if (recordLog == null) {
      final RecordDefinition recordDefinition = record.getRecordDefinition();
      final Logger log = LoggerFactory.getLogger(logCategory);
      log.info(message + "\t" + recordDefinition.getPath() + record.getIdentifier());
    } else {
      recordLog.info(message, record);
    }
  }

  public static RecordLog recordLog() {
    RecordLog recordLog = getForThread();
    if (recordLog == null) {
      recordLog = new RecordLog();
      ThreadSharedProperties.setProperty(KEY, recordLog);
    }
    return recordLog;
  }

  public static void warn(final Class<?> logCategory, final String message, final Record record) {
    final RecordLog recordLog = getForThread();
    if (record == null) {
      final Logger log = LoggerFactory.getLogger(logCategory);
      log.warn(message + "\tnull");
    } else if (recordLog == null) {
      final RecordDefinition recordDefinition = record.getRecordDefinition();
      final Logger log = LoggerFactory.getLogger(logCategory);
      log.warn(message + "\t" + recordDefinition.getPath() + record.getIdentifier());
    } else {
      recordLog.warn(message, record);
    }
  }

  private final Map<RecordDefinition, RecordDefinitionImpl> logRecordDefinitionMap = new HashMap<RecordDefinition, RecordDefinitionImpl>();

  private Writer<Record> writer;

  public RecordLog() {
  }

  public RecordLog(final Writer<Record> writer) {
    this.writer = writer;
  }

  @Override
  public void close() {
    if (this.writer != null) {
      this.writer.flush();
    }
    this.writer = null;
    this.logRecordDefinitionMap.clear();
  }

  public synchronized void error(final Object message, final Record record) {
    log("ERROR", message, record);
  }

  private RecordDefinition getLogRecordDefinition(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final RecordDefinition logRecordDefinition = getLogRecordDefinition(recordDefinition);
    return logRecordDefinition;
  }

  private RecordDefinition getLogRecordDefinition(final RecordDefinition recordDefinition) {
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
      logRecordDefinition.addField("LOGMESSAGE", DataTypes.STRING, 255, true);
      logRecordDefinition.addField("LOGLEVEL", DataTypes.STRING, 10, true);
      for (final FieldDefinition attribute : recordDefinition.getFields()) {
        final FieldDefinition logAttribute = new FieldDefinition(attribute);
        logRecordDefinition.addField(logAttribute);

      }
      this.logRecordDefinitionMap.put(recordDefinition, logRecordDefinition);
    }
    return logRecordDefinition;
  }

  public Writer<Record> getWriter() {
    return this.writer;
  }

  public synchronized void info(final Object message, final Record record) {
    log("INFO", message, record);
  }

  private void log(final String logLevel, final Object message, final Record record) {
    final Writer<Record> writer = this.writer;
    if (writer != null) {
      final RecordDefinition logRecordDefinition = getLogRecordDefinition(record);
      final Record logObject = new ArrayRecord(logRecordDefinition, record);
      logObject.setValue("LOGMESSAGE", message);
      logObject.setValue("LOGLEVEL", logLevel);
      synchronized (writer) {
        writer.write(logObject);
      }
    }
  }

  public void setWriter(final Writer<Record> writer) {
    this.writer = writer;
  }

  public synchronized void warn(final Object message, final Record record) {
    log("WARNING", message, record);
  }
}
