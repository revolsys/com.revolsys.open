package com.revolsys.gis.io;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.tsv.TsvWriter;
import com.revolsys.util.CollectionUtil;

public class StatisticsMap {
  private final Map<String, Statistics> statisticsMap = new TreeMap<String, Statistics>();

  private int providerCount = 0;

  private String prefix;

  private boolean logCounts;

  public StatisticsMap() {
  }

  public StatisticsMap(final String prefix) {
    this.prefix = prefix;
  }

  public void add(final String statisticName, final Record object) {
    final Statistics statistics = getStatistics(statisticName);
    statistics.add(object);

  }

  public void add(final String statisticName, final Record object,
    final long count) {
    final Statistics statistics = getStatistics(statisticName);
    statistics.add(object, count);
  }

  public void add(final String statisticName, final RecordDefinition type) {
    final Statistics statistics = getStatistics(statisticName);
    statistics.add(type);
  }

  public void add(final String statisticName, final RecordDefinition type,
    final long count) {
    final Statistics statistics = getStatistics(statisticName);
    statistics.add(type, count);
  }

  public synchronized void add(final String name, final Statistics statistics) {
    this.statisticsMap.put(name, statistics);
    statistics.connect();
  }

  public void add(final String statisticName, final String name) {
    final Statistics statistics = getStatistics(statisticName);
    statistics.add(name);
  }

  public void add(final String statisticName, final String path,
    final long count) {
    final Statistics statistics = getStatistics(statisticName);
    statistics.add(path, count);
  }

  public synchronized void addCountsText(final StringBuilder sb) {
    for (final Statistics stats : this.statisticsMap.values()) {
      stats.addCountsText(sb);
    }
  }

  public void clear() {
    this.statisticsMap.clear();
  }

  @PostConstruct
  public synchronized void connect() {
    this.providerCount++;
  }

  @PreDestroy
  public synchronized void disconnect() {
    this.providerCount--;
    if (this.providerCount <= 0) {
      for (final Statistics statistics : this.statisticsMap.values()) {
        statistics.disconnect();
      }
    }
  }

  public synchronized String getCountsText() {
    final StringBuilder sb = new StringBuilder();
    addCountsText(sb);
    return sb.toString();
  }

  public String getPrefix() {
    return this.prefix;
  }

  public synchronized Statistics getStatistics(final String statisticName) {
    if (statisticName == null) {
      return null;
    } else {
      final String name = CollectionUtil.toString(" ", this.prefix,
        statisticName);
      Statistics statistics = this.statisticsMap.get(name);
      if (statistics == null) {
        statistics = new Statistics(name);
        statistics.setLogCounts(this.logCounts);
        this.statisticsMap.put(name, statistics);
      }
      return statistics;
    }
  }

  public synchronized Set<String> getStatisticsNames() {
    return this.statisticsMap.keySet();
  }

  public boolean isEmpty() {
    return this.statisticsMap.isEmpty();
  }

  public synchronized void setLogCounts(final boolean logCounts) {
    this.logCounts = logCounts;
    for (final Statistics statistics : this.statisticsMap.values()) {
      statistics.setLogCounts(logCounts);
    }
  }

  public void setPrefix(final String prefix) {
    this.prefix = prefix;
  }

  public String toTsv() {
    return toTsv("CATEGORY", "NAME", "COUNT");
  }

  public String toTsv(final String... fieldNames) {
    final StringWriter out = new StringWriter();
    return toTsv(out, fieldNames);
  }

  public String toTsv(final Writer out, final String... fieldNames) {
    try (
        TsvWriter tsv = new TsvWriter(out);) {
      tsv.write(Arrays.asList(fieldNames));
      for (final Entry<String, Statistics> entry : this.statisticsMap.entrySet()) {
        final String category = entry.getKey();
        final Statistics statistics = entry.getValue();
        for (final String name : statistics.getNames()) {
          final long count = statistics.get(name);
          tsv.write(category, name, count);
        }
      }
      return out.toString();
    }
  }
}
