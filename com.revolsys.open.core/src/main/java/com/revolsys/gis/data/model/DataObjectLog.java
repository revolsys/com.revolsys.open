package com.revolsys.gis.data.model;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.collection.ThreadSharedAttributes;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Writer;

public class DataObjectLog {

  private static final String KEY = DataObjectLog.class.getName();

  public static DataObjectLog dataObjectLog() {
    DataObjectLog dataObjectLog = getForThread();
    if (dataObjectLog == null) {
      dataObjectLog = new DataObjectLog();
      ThreadSharedAttributes.setAttribute(KEY, dataObjectLog);
    }
    return dataObjectLog;
  }

  public static void error(
    final Class<?> logCategory,
    final String message,
    final DataObject object) {
    final DataObjectLog dataObjectLog = getForThread();
    if (object == null) {
      final Logger log = LoggerFactory.getLogger(logCategory);
      log.error(message + "\tnull");
    } else if (dataObjectLog == null) {
      final DataObjectMetaData metaData = object.getMetaData();
      final Logger log = LoggerFactory.getLogger(logCategory);
      log.error(message + "\t" + metaData.getPath() + object.getIdValue());
    } else {
      dataObjectLog.error(message, object);
    }
  }

  public static DataObjectLog getForThread() {
    final DataObjectLog dataObjectLog = ThreadSharedAttributes.getAttribute(KEY);
    return dataObjectLog;
  }

  public static void info(
    final Class<?> logCategory,
    final String message,
    final DataObject object) {
    final DataObjectLog dataObjectLog = getForThread();
    if (object == null) {
      final Logger log = LoggerFactory.getLogger(logCategory);
      log.info(message + "\tnull");
    } else if (dataObjectLog == null) {
      final DataObjectMetaData metaData = object.getMetaData();
      final Logger log = LoggerFactory.getLogger(logCategory);
      log.info(message + "\t" + metaData.getPath() + object.getIdValue());
    } else {
      dataObjectLog.info(message, object);
    }
  }

  public static void warn(
    final Class<?> logCategory,
    final String message,
    final DataObject object) {
    final DataObjectLog dataObjectLog = getForThread();
    if (object == null) {
      final Logger log = LoggerFactory.getLogger(logCategory);
      log.warn(message + "\tnull");
    } else if (dataObjectLog == null) {
      final DataObjectMetaData metaData = object.getMetaData();
      final Logger log = LoggerFactory.getLogger(logCategory);
      log.warn(message + "\t" + metaData.getPath() + object.getIdValue());
    } else {
      dataObjectLog.warn(message, object);
    }
  }

  private Writer<DataObject> writer;

  private final Map<DataObjectMetaData, DataObjectMetaDataImpl> logMetaDataMap = new HashMap<DataObjectMetaData, DataObjectMetaDataImpl>();

  public DataObjectLog() {
  }

  public DataObjectLog(final Writer<DataObject> out) {
    this.writer = out;
  }

  public synchronized void error(final Object message, final DataObject object) {
    log("ERROR", message, object);
  }

  private DataObjectMetaData getLogMetaData(final DataObject object) {
    final DataObjectMetaData metaData = object.getMetaData();
    final DataObjectMetaData logMetaData = getLogMetaData(metaData);
    return logMetaData;
  }

  private DataObjectMetaData getLogMetaData(final DataObjectMetaData metaData) {
    DataObjectMetaDataImpl logMetaData = logMetaDataMap.get(metaData);
    if (logMetaData == null) {
      final String path = metaData.getPath();
      final String parentPath = PathUtil.getPath(path);
      final String tableName = PathUtil.getName(path);
      final String logTableName;
      if (tableName.toUpperCase().equals(tableName)) {
        logTableName = tableName + "_LOG";
      } else {
        logTableName = tableName + "_log";
      }
      final String logTypeName = PathUtil.toPath(parentPath , logTableName);
      logMetaData = new DataObjectMetaDataImpl(logTypeName);
      logMetaData.addAttribute("LOGMESSAGE", DataTypes.STRING, 255, true);
      logMetaData.addAttribute("LOGLEVEL", DataTypes.STRING, 10, true);
      for (final Attribute attribute : metaData.getAttributes()) {
        final Attribute logAttribute = new Attribute(attribute);
        logMetaData.addAttribute(logAttribute);

      }
      logMetaDataMap.put(metaData, logMetaData);
    }
    return logMetaData;
  }

  public Writer<DataObject> getWriter() {
    return writer;
  }

  public synchronized void info(final Object message, final DataObject object) {
    log("INFO", message, object);
  }

  private void log(
    final String logLevel,
    final Object message,
    final DataObject object) {
    if (writer != null) {
      final DataObjectMetaData logMetaData = getLogMetaData(object);
      final DataObject logObject = new ArrayDataObject(logMetaData, object);
      logObject.setValue("LOGMESSAGE", message);
      logObject.setValue("LOGLEVEL", logLevel);
      synchronized (writer) {
        writer.write(logObject);
      }
    }
  }

  public void setWriter(final Writer<DataObject> writer) {
    this.writer = writer;
  }

  public synchronized void warn(final Object message, final DataObject object) {
    log("WARNING", message, object);
  }
}
