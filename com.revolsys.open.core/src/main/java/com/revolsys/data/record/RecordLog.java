package com.revolsys.data.record;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.collection.ThreadSharedAttributes;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.types.DataTypes;
import com.revolsys.io.Path;
import com.revolsys.io.Writer;

public class RecordLog {

  public static RecordLog recordLog() {
    RecordLog recordLog = getForThread();
    if (recordLog == null) {
      recordLog = new RecordLog();
      ThreadSharedAttributes.setAttribute(KEY, recordLog);
    }
    return recordLog;
  }

  public static void error(final Class<?> logCategory, final String message,
    final Record object) {
    final RecordLog recordLog = getForThread();
    if (object == null) {
      final Logger log = LoggerFactory.getLogger(logCategory);
      log.error(message + "\tnull");
    } else if (recordLog == null) {
      final RecordDefinition recordDefinition = object.getRecordDefinition();
      final Logger log = LoggerFactory.getLogger(logCategory);
      log.error(message + "\t" + recordDefinition.getPath() + object.getIdentifier());
    } else {
      recordLog.error(message, object);
    }
  }

  public static RecordLog getForThread() {
    final RecordLog recordLog = ThreadSharedAttributes.getAttribute(KEY);
    return recordLog;
  }

  public static void info(final Class<?> logCategory, final String message,
    final Record object) {
    final RecordLog recordLog = getForThread();
    if (object == null) {
      final Logger log = LoggerFactory.getLogger(logCategory);
      log.info(message + "\tnull");
    } else if (recordLog == null) {
      final RecordDefinition recordDefinition = object.getRecordDefinition();
      final Logger log = LoggerFactory.getLogger(logCategory);
      log.info(message + "\t" + recordDefinition.getPath() + object.getIdentifier());
    } else {
      recordLog.info(message, object);
    }
  }

  public static void warn(final Class<?> logCategory, final String message,
    final Record object) {
    final RecordLog recordLog = getForThread();
    if (object == null) {
      final Logger log = LoggerFactory.getLogger(logCategory);
      log.warn(message + "\tnull");
    } else if (recordLog == null) {
      final RecordDefinition recordDefinition = object.getRecordDefinition();
      final Logger log = LoggerFactory.getLogger(logCategory);
      log.warn(message + "\t" + recordDefinition.getPath() + object.getIdentifier());
    } else {
      recordLog.warn(message, object);
    }
  }

  private static final String KEY = RecordLog.class.getName();

  private Writer<Record> writer;

  private final Map<RecordDefinition, RecordDefinitionImpl> logMetaDataMap = new HashMap<RecordDefinition, RecordDefinitionImpl>();

  public RecordLog() {
  }

  public RecordLog(final Writer<Record> out) {
    this.writer = out;
  }

  public synchronized void error(final Object message, final Record object) {
    log("ERROR", message, object);
  }

  private RecordDefinition getLogMetaData(final Record object) {
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    final RecordDefinition logMetaData = getLogMetaData(recordDefinition);
    return logMetaData;
  }

  private RecordDefinition getLogMetaData(final RecordDefinition recordDefinition) {
    RecordDefinitionImpl logMetaData = this.logMetaDataMap.get(recordDefinition);
    if (logMetaData == null) {
      final String path = recordDefinition.getPath();
      final String parentPath = Path.getPath(path);
      final String tableName = Path.getName(path);
      final String logTableName;
      if (tableName.toUpperCase().equals(tableName)) {
        logTableName = tableName + "_LOG";
      } else {
        logTableName = tableName + "_log";
      }
      final String logTypeName = Path.toPath(parentPath, logTableName);
      logMetaData = new RecordDefinitionImpl(logTypeName);
      logMetaData.addAttribute("LOGMESSAGE", DataTypes.STRING, 255, true);
      logMetaData.addAttribute("LOGLEVEL", DataTypes.STRING, 10, true);
      for (final Attribute attribute : recordDefinition.getAttributes()) {
        final Attribute logAttribute = new Attribute(attribute);
        logMetaData.addAttribute(logAttribute);

      }
      this.logMetaDataMap.put(recordDefinition, logMetaData);
    }
    return logMetaData;
  }

  public Writer<Record> getWriter() {
    return this.writer;
  }

  public synchronized void info(final Object message, final Record object) {
    log("INFO", message, object);
  }

  private void log(final String logLevel, final Object message,
    final Record object) {
    if (this.writer != null) {
      final RecordDefinition logMetaData = getLogMetaData(object);
      final Record logObject = new ArrayRecord(logMetaData, object);
      logObject.setValue("LOGMESSAGE", message);
      logObject.setValue("LOGLEVEL", logLevel);
      synchronized (this.writer) {
        this.writer.write(logObject);
      }
    }
  }

  public void setWriter(final Writer<Record> writer) {
    this.writer = writer;
  }

  public synchronized void warn(final Object message, final Record object) {
    log("WARNING", message, object);
  }
}
