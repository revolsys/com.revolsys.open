package com.revolsys.geometry.algorithm.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.revolsys.data.record.Record;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.predicate.Predicates;

public class PointRecordMap {

  private Comparator<Record> comparator;

  private Map<PointDouble, List<Record>> objectMap = new HashMap<>();

  private boolean removeEmptyLists;

  private int size = 0;

  public PointRecordMap() {
  }

  public PointRecordMap(final Comparator<Record> comparator) {
    this.comparator = comparator;
  }

  /**
   * Add a {@link Point} {@link Record} to the list of objects at the given
   * coordinate.
   *
   * @param pointObjects The map of point objects.
   * @param record The object to add.
   */
  public void add(final Record record) {
    final PointDouble key = getKey(record);
    final List<Record> records = getOrCreateRecords(key);
    records.add(record);
    if (this.comparator != null) {
      Collections.sort(records, this.comparator);
    }
    this.size++;
  }

  public void clear() {
    this.size = 0;
    this.objectMap = new HashMap<>();
  }

  public boolean containsKey(final Point point) {
    final PointDouble key = getKey(point);
    return this.objectMap.containsKey(key);
  }

  public List<Record> getAll() {
    final List<Record> records = new ArrayList<Record>();
    for (final List<Record> recordsAtPoint : this.objectMap.values()) {
      records.addAll(recordsAtPoint);
    }
    return records;
  }

  @SuppressWarnings("unchecked")
  public <V extends Record> V getFirstMatch(final Point point) {
    final List<Record> records = getRecords(point);
    if (records.isEmpty()) {
      return null;
    } else {
      return (V)records.get(0);
    }

  }

  public Record getFirstMatch(final Record record, final Predicate<Record> filter) {
    final List<Record> records = getRecords(record);
    for (final Record matchRecord : records) {
      if (filter.test(matchRecord)) {
        return matchRecord;
      }
    }
    return null;
  }

  private PointDouble getKey(final Point point) {
    return new PointDouble(point, 2);
  }

  private PointDouble getKey(final Record object) {
    final Point point = object.getGeometry();
    return getKey(point);
  }

  public Set<Point> getKeys() {
    return Collections.<Point> unmodifiableSet(this.objectMap.keySet());
  }

  public List<Record> getMatches(final Record record, final Predicate<Record> predicate) {
    final List<Record> records = getRecords(record);
    final List<Record> filteredRecords = Predicates.filter(records, predicate);
    return filteredRecords;
  }

  protected List<Record> getOrCreateRecords(final PointDouble key) {
    List<Record> objects = this.objectMap.get(key);
    if (objects == null) {
      objects = new ArrayList<Record>(1);
      this.objectMap.put(key, objects);
    }
    return objects;
  }

  public List<Record> getRecords(final Point point) {
    final PointDouble key = getKey(point);
    final List<Record> records = this.objectMap.get(key);
    if (records == null) {
      return Collections.emptyList();
    } else {
      return new ArrayList<Record>(records);
    }
  }

  public List<Record> getRecords(final Record record) {
    final Point point = record.getGeometry();
    final List<Record> objects = getRecords(point);
    return objects;
  }

  public void initialize(final Point point) {
    if (!isRemoveEmptyLists()) {
      final PointDouble key = getKey(point);
      getOrCreateRecords(key);
    }
  }

  public boolean isRemoveEmptyLists() {
    return this.removeEmptyLists;
  }

  public void remove(final Record record) {
    final PointDouble key = getKey(record);
    final List<Record> objects = this.objectMap.get(key);
    if (objects != null) {
      objects.remove(record);
      if (objects.isEmpty()) {
        if (isRemoveEmptyLists()) {
          this.objectMap.remove(key);
        }
      } else if (this.comparator != null) {
        Collections.sort(objects, this.comparator);
      }
    }
    this.size--;
  }

  public void setRemoveEmptyLists(final boolean removeEmptyLists) {
    this.removeEmptyLists = removeEmptyLists;
  }

  public int size() {
    return this.size;
  }

  public void sort(final Record record) {
    if (this.comparator != null) {
      final List<Record> records = getRecords(record);
      if (records != null) {
        Collections.sort(records, this.comparator);
      }
    }
  }

  public void write(final Channel<Record> out) {
    if (out != null) {
      for (final Point point : getKeys()) {
        final List<Record> points = getRecords(point);
        for (final Record object : points) {
          out.write(object);
        }
      }
    }
  }
}
