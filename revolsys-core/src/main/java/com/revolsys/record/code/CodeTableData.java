package com.revolsys.record.code;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.number.Numbers;

import com.revolsys.io.BaseCloseable;

import reactor.core.Disposable;

public class CodeTableData implements BaseCloseable, Cloneable {

  private final Instant startTime = Instant.now();

  private final List<Identifier> identifiers = new ArrayList<>();

  private final Map<Object, CodeTableEntry> entryCache = new LinkedHashMap<>();

  private final AtomicLong maxId = new AtomicLong();

  private int valueFieldLength = -1;

  private AbstractCodeTable codeTable;

  private boolean allLoaded = false;

  private Disposable disposable;

  public CodeTableData(final AbstractCodeTable codeTable) {
    this.codeTable = codeTable;
  }

  public CodeTableData(final CodeTableData data) {
    this.identifiers.addAll(data.identifiers);
    for (final Object key : data.entryCache.values()) {
      final CodeTableEntry entry = data.entryCache.get(key);
      this.entryCache.put(key, entry);
    }
    this.maxId.set(data.maxId.get());
    this.valueFieldLength = data.valueFieldLength;
  }

  protected synchronized CodeTableEntry addEntry(final Identifier id, final Object value) {
    synchronized (this.identifiers) {
      if (id instanceof Number) {
        final long longValue = ((Number)id).longValue();
        this.maxId.updateAndGet(oldId -> Math.max(oldId, longValue));
      }
      this.identifiers.add(id);
      final CodeTableEntry entry = CodeTableEntry.create(id, value);
      this.entryCache.put(id, entry);
      final String idString = id.toString();
      this.entryCache.put(idString, entry);
      if (!isCaseSensitive()) {
        final String lowerId = idString.toLowerCase();
        this.entryCache.put(lowerId, entry);
      }
      if (this.codeTable.isFindByValue(id)) {
        this.entryCache.put(value, entry);
        final Object normalizedValue = getNormalizedValue(value);
        this.entryCache.put(normalizedValue, entry);
      }
      return entry;
    }
  }

  @Override
  public CodeTableData clone() {
    return new CodeTableData(this);
  }

  @Override
  public void close() {
    this.identifiers.clear();
    this.entryCache.clear();
  }

  public Disposable getDisposable() {
    return this.disposable;
  }

  public CodeTableEntry getEntry(final Object idOrValue) {
    if (idOrValue == null) {
      return null;
    }
    CodeTableEntry entry = this.entryCache.get(idOrValue);
    if (entry == null) {
      final Object normalizedValue = getNormalizedValue(idOrValue);
      entry = this.entryCache.get(normalizedValue);
    }
    if (entry == null) {
      final String idString = idOrValue.toString();
      entry = this.entryCache.get(idString);
      if (entry == null) {
        if (!isCaseSensitive()) {
          final String lowerId = idString.toLowerCase();
          entry = this.entryCache.get(lowerId);
        }
      }
    }
    return entry;
  }

  public Identifier getIdentidier(final int index) {
    return this.identifiers.get(index);
  }

  public List<Identifier> getIdentifiers() {
    return Collections.unmodifiableList(this.identifiers);
  }

  protected long getNextId() {
    return this.maxId.incrementAndGet();
  }

  private Object getNormalizedValue(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return Numbers.toString(number);
    } else if (isCaseSensitive()) {
      return value;
    } else if (value instanceof Collection) {
      final List<Object> normalizedValues = new ArrayList<>();
      for (final Object childValue : (Collection<?>)value) {
        final Object normalizedValue = getNormalizedValue(childValue);
        normalizedValues.add(normalizedValue);
      }
      return normalizedValues;
    } else {
      return value.toString().toLowerCase();
    }
  }

  public Instant getStartTime() {
    return this.startTime;
  }

  public int getValueFieldLength() {
    if (this.valueFieldLength == -1) {
      this.valueFieldLength = CodeTableEntry.maxLength(this.entryCache.values());
    }
    return this.valueFieldLength;
  }

  public boolean hasIdentifier(final Identifier id) {
    return this.entryCache.containsKey(id);
  }

  public boolean isAfter(final CodeTableData data) {
    return this.startTime.isAfter(data.startTime);
  }

  public boolean isAllLoaded() {
    return this.allLoaded;
  }

  protected boolean isCaseSensitive() {
    return this.codeTable.isCaseSensitive();
  }

  public boolean isEmpty() {
    return this.entryCache.isEmpty();
  }

  public void setAllLoaded(final boolean allLoaded) {
    this.allLoaded = allLoaded;
  }

  public void setDisposable(final Disposable disposable) {
    this.disposable = disposable;
  }

  public int size() {
    return this.identifiers.size();
  }

}
