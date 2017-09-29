package com.revolsys.util.count;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.revolsys.datatype.DataTypes;
import com.revolsys.io.PathNameProxy;
import com.revolsys.logging.Logs;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.format.tsv.Tsv;
import com.revolsys.record.io.format.tsv.TsvWriter;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;
import com.revolsys.util.Counter;
import com.revolsys.util.LongCounter;

public class LabelCountMap {
  private final Map<String, Counter> counterByLabel = new TreeMap<>();

  private boolean logCounts = true;

  private String message;

  private int providerCount = 0;

  public LabelCountMap() {
    this(null);
  }

  public LabelCountMap(final String message) {
    this.message = message;
  }

  public void addCount(final CharSequence label) {
    addCount(label, 1);
  }

  public synchronized boolean addCount(final CharSequence label, final long count) {
    if (label == null) {
      return false;
    } else {
      final String labelString = label.toString();
      Counter counter = this.counterByLabel.get(labelString);
      if (counter == null) {
        counter = new LongCounter(labelString, count);
        this.counterByLabel.put(labelString, counter);
        return true;
      } else {
        counter.add(count);
        return false;
      }
    }
  }

  public void addCount(final Enum<?> label) {
    addCount(label.name(), 1);
  }

  public void addCount(final PathNameProxy pathNameProxy) {
    if (pathNameProxy != null) {
      final CharSequence label = pathNameProxy.getPathName();
      addCount(label);
    }
  }

  public void addCount(final PathNameProxy pathNameProxy, final long count) {
    final CharSequence label = pathNameProxy.getPathName();
    addCount(label, count);
  }

  public void addCounts(final LabelCountMap labelCountMap) {
    synchronized (labelCountMap) {
      for (final String label : labelCountMap.getLabels()) {
        final long count = labelCountMap.getCount(label);
        addCount(label, count);
      }
    }
  }

  public synchronized void addCountsText(final StringBuilder sb) {
    int totalCount = 0;
    if (this.message != null) {
      sb.append(this.message);
    }
    sb.append("\n");
    for (final Entry<String, Counter> entry : this.counterByLabel.entrySet()) {
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
    this.counterByLabel.clear();
  }

  public synchronized void clearCounts(final String label) {
    if (label != null) {
      final String labelString = label.toString();
      this.counterByLabel.remove(labelString);
    }
  }

  public synchronized void connect() {
    this.providerCount++;
  }

  public synchronized void disconnect() {
    this.providerCount--;
    if (this.providerCount <= 0) {
      logCounts();
    }
  }

  public synchronized Long getCount(final CharSequence label) {
    if (label != null) {
      final String labelString = label.toString();
      final Counter counter = this.counterByLabel.get(labelString);
      if (counter != null) {
        return counter.get();
      }
    }
    return null;
  }

  public synchronized Counter getCounter(final CharSequence label) {
    if (label == null) {
      return null;
    } else {
      final String labelString = label.toString();
      Counter counter = this.counterByLabel.get(labelString);
      if (counter == null) {
        counter = new LongCounter(labelString);
        this.counterByLabel.put(labelString, counter);
      }
      return counter;
    }
  }

  public synchronized Set<String> getLabels() {
    return this.counterByLabel.keySet();
  }

  public String getMessage() {
    return this.message;
  }

  public boolean isLogCounts() {
    return this.logCounts;
  }

  public synchronized String logCounts() {
    final StringBuilder sb = new StringBuilder();
    addCountsText(sb);
    final String string = sb.toString();
    if (isLogCounts() && !this.counterByLabel.isEmpty()) {
      Logs.info(this, string);
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
    return this.message;
  }

  public String toTsv() {
    return toTsv("LABEL", "COUNT");
  }

  public String toTsv(final String... titles) {
    final StringWriter out = new StringWriter();
    toTsv(out, titles);
    return out.toString();
  }

  public void toTsv(final Writer out, final String... titles) {
    try (
      TsvWriter tsv = Tsv.plainWriter(out)) {
      long total = 0;
      tsv.write(Arrays.asList(titles));
      for (final String label : getLabels()) {
        final long count = getCount(label);
        total += count;
        tsv.write(label, count);
      }
      tsv.write("Total", total);
    }
  }

  public void writeCounts(final Object target, final String labelTitle) {
    final RecordDefinitionBuilder recordDefinitionBuilder = new RecordDefinitionBuilder("Counts");
    recordDefinitionBuilder.addField(labelTitle, DataTypes.STRING, 50);
    recordDefinitionBuilder.addField("Count", DataTypes.LONG, 10);
    final RecordDefinition recordDefinition = recordDefinitionBuilder.getRecordDefinition();
    try (
      RecordWriter recordWriter = RecordWriter.newRecordWriter(recordDefinition, target)) {
      for (final String label : getLabels()) {
        final Long count = getCount(label);
        recordWriter.write(label, count);
      }
    }
  }
}
