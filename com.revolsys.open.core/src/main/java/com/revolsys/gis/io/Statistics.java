package com.revolsys.gis.io;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;

public class Statistics {
  private final Map<String, Long> counts = new TreeMap<String, Long>();

  private final Logger log;

  private String message;

  private int providerCount = 0;

  private boolean logCounts = true;

  public Statistics() {
    this(null);
  }

  public Statistics(final String message) {
    this(Statistics.class.getName(), message);
  }

  public Statistics(final String category, final String message) {
    log = Logger.getLogger(category);
    this.message = message;
  }

  public void add(final DataObject object) {
    if (object != null) {
      final DataObjectMetaData type = object.getMetaData();
      add(type);
    }
  }

  public void add(final DataObject object, final long count) {
    final DataObjectMetaData type = object.getMetaData();
    add(type, count);

  }

  public void add(final DataObjectMetaData type) {
    final String path = type.getPath();
    add(path);
  }

  public void add(final DataObjectMetaData type, final long count) {
    final String path = type.getPath();
    add(path, count);
  }

  public void add(final String name) {
    add(name, 1);
  }

  public synchronized boolean add(final String name, final long count) {
    final Long oldCount = counts.get(name);
    if (oldCount == null) {
      counts.put(name, count);
      return true;
    } else {
      counts.put(name, oldCount + count);
      return false;
    }
  }

  public synchronized void addCountsText(final StringBuffer sb) {
    int totalCount = 0;
    if (message != null) {
      sb.append(message);
    }
    sb.append("\n");
    for (final Entry<String, Long> entry : counts.entrySet()) {
      sb.append(entry.getKey());
      sb.append("\t");
      final Long count = entry.getValue();
      totalCount += count;
      sb.append(count);
      sb.append("\n");
    }
    sb.append("Total");
    sb.append("\t");
    sb.append(totalCount);
    sb.append("\n");
  }

  public synchronized void connect() {
    providerCount++;
  }

  public synchronized void disconnect() {
    providerCount--;
    if (providerCount <= 0) {
      logCounts();
    }
  }

  public synchronized Long get(final String name) {
    if (name != null) {
      final Long count = counts.get(name);
      return count;
    } else {
      return null;
    }
  }

  public String getMessage() {
    return message;
  }

  public synchronized Set<String> getNames() {
    return counts.keySet();
  }

  public boolean isLogCounts() {
    return logCounts;
  }

  public synchronized void logCounts() {
    if (isLogCounts() && !counts.isEmpty()) {
      final StringBuffer sb = new StringBuffer();
      addCountsText(sb);
      log.info(sb.toString());
    }
  }

  public void setLogCounts(final boolean logCounts) {
    this.logCounts = logCounts;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return message;
  }
}
