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

import com.revolsys.format.tsv.Tsv;
import com.revolsys.format.tsv.TsvWriter;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.Emptyable;

public class StatisticsMap implements Emptyable {
  private boolean logCounts;

  private String prefix;

  private int providerCount = 0;

  private final Map<String, Statistics> statisticsMap = new TreeMap<>();

  public StatisticsMap() {
  }

  public StatisticsMap(final Map<String, Statistics> statisticsMap) {
    if (statisticsMap != null) {
      this.statisticsMap.putAll(statisticsMap);
    }
  }

  public StatisticsMap(final String prefix) {
    this.prefix = prefix;
  }

  public void add(final CharSequence statisticName, final Record record) {
    final Statistics statistics = getStatistics(statisticName);
    statistics.add(record);

  }

  public void add(final CharSequence statisticName, final Record record, final long count) {
    final Statistics statistics = getStatistics(statisticName);
    statistics.add(record, count);
  }

  public void add(final CharSequence statisticName, final RecordDefinition type) {
    final Statistics statistics = getStatistics(statisticName);
    statistics.add(type);
  }

  public void add(final CharSequence statisticName, final RecordDefinition type, final long count) {
    final Statistics statistics = getStatistics(statisticName);
    statistics.add(type, count);
  }

  public void add(final CharSequence statisticName, final String name) {
    final Statistics statistics = getStatistics(statisticName);
    statistics.add(name);
  }

  public void add(final CharSequence statisticName, final String path, final long count) {
    final Statistics statistics = getStatistics(statisticName);
    statistics.add(path, count);
  }

  public void addAll(final StatisticsMap statisticsMap) {
    synchronized (statisticsMap) {
      for (final Entry<String, Statistics> entry : statisticsMap.statisticsMap.entrySet()) {
        final String category = entry.getKey();
        final Statistics statistics = entry.getValue();
        addAll(category, statistics);
      }
    }
  }

  public void addAll(final String name, final Statistics statistics) {
    final Statistics thisStatistics = getStatistics(name);
    thisStatistics.addAll(statistics);
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

  public long getStatistic(final String typeName, final String name) {
    final Statistics statistics = getStatistics(typeName);
    if (statistics == null) {
      return 0;
    } else {
      return statistics.get(name);
    }
  }

  public synchronized Statistics getStatistics(final CharSequence statisticName) {
    if (statisticName == null) {
      return null;
    } else {
      final String name = CollectionUtil.toString(" ", this.prefix, statisticName);
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

  @Override
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
    toTsv(out, fieldNames);
    return out.toString();
  }

  public void toTsv(final Writer out, final String... fieldNames) {
    try (
      TsvWriter tsv = Tsv.plainWriter(out)) {
      tsv.write(Arrays.asList(fieldNames));
      long total = 0;
      for (final Entry<String, Statistics> entry : this.statisticsMap.entrySet()) {
        final String category = entry.getKey();
        final Statistics statistics = entry.getValue();
        for (final String name : statistics.getNames()) {
          final long count = statistics.get(name);
          total += count;
          tsv.write(category, name, count);
        }
      }
      tsv.write(null, "Total", total);
    }
  }
}
