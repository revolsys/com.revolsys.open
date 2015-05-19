package com.revolsys.gis.io;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.format.tsv.TsvWriter;
import com.revolsys.util.Counter;
import com.revolsys.util.LongCounter;

public class Statistics {
  private final Map<String, Counter> counts = new TreeMap<>();

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

  public void add(final Record object) {
    if (object != null) {
      final RecordDefinition type = object.getRecordDefinition();
      add(type);
    }
  }

  public void add(final Record object, final long count) {
    final RecordDefinition type = object.getRecordDefinition();
    add(type, count);

  }

  public void add(final RecordDefinition type) {
    final String path = type.getPath();
    add(path);
  }

  public void add(final RecordDefinition type, final long count) {
    final String path = type.getPath();
    add(path, count);
  }

  public void add(final String name) {
    add(name, 1);
  }

  public synchronized boolean add(final String name, final long count) {
    Counter counter = counts.get(name);
    if (counter == null) {
      counter = new LongCounter(name, count);
      counts.put(name, counter);
      return true;
    } else {
      counter.add(count);
      return false;
    }
  }

  public void addAll(final Statistics statistics) {
    synchronized (statistics) {
      for (final String name : statistics.getNames()) {
        final long count = statistics.get(name);
        add(name, count);
      }
    }
  }

  public synchronized void addCountsText(final StringBuilder sb) {
    int totalCount = 0;
    if (message != null) {
      sb.append(message);
    }
    sb.append("\n");
    for (final Entry<String, Counter> entry : counts.entrySet()) {
      sb.append(entry.getKey());
      sb.append("\t");
      final Counter counter = entry.getValue();
      final long count = counter.get();
      totalCount += count;
      sb.append(count);
      sb.append("\n");
    }
    sb.append("Total");
    sb.append("\t");
    sb.append(totalCount);
    sb.append("\n");
  }

  public synchronized void clearCounts() {
    counts.clear();
  }

  public synchronized void clearCounts(final String typeName) {
    counts.remove(typeName);
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
      final Counter counter = counts.get(name);
      if (counter != null) {
        return counter.get();
      }
    }
    return null;
  }

  public synchronized Counter getCounter(final String name) {
    Counter counter = counts.get(name);
    if (counter == null) {
      counter = new LongCounter(name);
      counts.put(name, counter);
    }
    return counter;
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

  public synchronized String logCounts() {
    final StringBuilder sb = new StringBuilder();
    addCountsText(sb);
    final String string = sb.toString();
    if (isLogCounts() && !counts.isEmpty()) {
      log.info(string);
    }
    return string;
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

  public String toTsv() {
    return toTsv("NAME", "COUNT");
  }

  public String toTsv(final String... fieldNames) {
    final StringWriter out = new StringWriter();
    toTsv(out, fieldNames);
    return out.toString();
  }

  public void toTsv(final Writer out, final String... fieldNames) {
    try (
      TsvWriter tsv = new TsvWriter(out);) {
      long total = 0;
      tsv.write(Arrays.asList(fieldNames));
      for (final String name : getNames()) {
        final long count = get(name);
        total += count;
        tsv.write(name, count);
      }
      tsv.write("Total", total);
    }
  }
}
