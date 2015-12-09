package com.revolsys.util.number;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.revolsys.collection.map.Maps;

public class DoubleStatistics {
  public static <K> void addValue(final Map<K, DoubleStatistics> statisticsByKey, final K key,
    final double value) {
    final DoubleStatistics statistics = Maps.get(statisticsByKey, key, DoubleStatistics.factory());
    statistics.addValue(value);
  }

  public static final Supplier<DoubleStatistics> factory() {
    return () -> new DoubleStatistics();
  }

  private final List<Double> values = new ArrayList<>();

  private boolean updated = false;

  private double mean;

  private double median;

  private double min = Double.MAX_VALUE;

  private double max = -Double.MAX_VALUE;

  private double sum = 0;

  public synchronized void addValue(final double value) {
    this.values.add(value);
    this.sum += value;
    if (value < this.min) {
      this.min = value;
    }
    if (value > this.max) {
      this.max = value;
    }
    this.updated = true;
  }

  public int getCount() {
    return this.values.size();
  }

  public double getMax() {
    return this.max;
  }

  public double getMean() {
    if (this.updated) {
      this.mean = this.sum / this.values.size();
    }
    return this.mean;
  }

  public double getMedian() {
    synchronized (this) {
      if (sort()) {
        final int count = getCount();
        if (count % 2 == 0) {
          final int index = count / 2;
          final double value1 = this.values.get(index);
          final double value2 = this.values.get(index + 1);
          this.median = (value1 + value2) / 2;
        } else {
          this.median = this.values.get((count + 1) / 2);
        }
      }
    }
    return this.median;
  }

  public synchronized double getMin() {
    return this.min;
  }

  public synchronized double getRange() {
    return getMax() - getMin();
  }

  public double getSum() {
    return this.sum;
  }

  private synchronized boolean sort() {
    if (this.updated) {
      this.updated = false;
      Collections.sort(this.values);
      return true;
    } else {
      return false;
    }
  }
}
