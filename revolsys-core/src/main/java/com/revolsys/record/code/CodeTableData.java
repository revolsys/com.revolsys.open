package com.revolsys.record.code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.record.code.AbstractSingleValueCodeTable.IncompleteValue;

public class CodeTableData implements Cloneable {

  private Map<Identifier, Identifier> idIdCache = new LinkedHashMap<>();

  private Map<String, Identifier> stringIdMap = new HashMap<>();

  private final AbstractCodeTable codeTable;

  private final AtomicLong maxId = new AtomicLong();

  private List<Identifier> identifiers = new ArrayList<>();

  private Map<Identifier, Object> idValueCache = new LinkedHashMap<>();

  private Map<Object, Identifier> valueIdCache = new LinkedHashMap<>();

  public CodeTableData(AbstractCodeTable codeTable) {
    super();
    this.codeTable = codeTable;
  }

  public void addIdentifier(final Identifier id) {
    updateMaxId(id);
    this.identifiers.add(id);
    this.idIdCache.put(id, id);
    final String idString = id.toString();
    this.stringIdMap.put(idString, id);
    if (!this.codeTable.isCaseSensitive()) {
      final String lowerId = idString.toLowerCase();
      this.stringIdMap.put(lowerId, id);
    }
  }

  public Object addIdentifierAndValue(final Identifier id, final Object value) {
    addIdentifier(id);
    final Object oldValue = this.idValueCache.put(id, value);
    return oldValue;
  }

  public int calculateValueFieldLength() {
    int length = 0;
    for (final Object value : this.idValueCache.values()) {
      if (!(value instanceof IncompleteValue)) {
        final int valueLength = value.toString().length();
        if (valueLength > length) {
          length = valueLength;
        }
      }
    }
    return length;
  }

  @Override
  public CodeTableData clone() {
    try {
      final CodeTableData clone = (CodeTableData)super.clone();
      clone.idIdCache = new LinkedHashMap<>(this.idIdCache);
      clone.stringIdMap = new LinkedHashMap<>(this.stringIdMap);
      clone.identifiers = new ArrayList<>(this.identifiers);
      clone.idValueCache = new LinkedHashMap<>(this.idValueCache);
      for (final Entry<Identifier, Object> entry : this.idValueCache.entrySet()) {
        final Identifier id = entry.getKey();
        final Object value = entry.getValue();
        if (!(value instanceof IncompleteValue)) {
          clone.idValueCache.put(id, value);
        }
      }
      clone.valueIdCache = new LinkedHashMap<>(this.valueIdCache);

      return clone;
    } catch (final CloneNotSupportedException e) {
      throw Exceptions.wrap(e);
    }
  }

  public Identifier getIdByValue(final Object value) {
    return this.valueIdCache.get(value);
  }

  public Identifier getIdentifier(final Object id) {
    if (id != null) {
      final Identifier identifier = this.idIdCache.get(id);
      if (identifier != null) {
        return identifier;
      } else {
        return getIdFromString(id);
      }
    }
    return null;
  }

  public List<Identifier> getIdentifiers() {
    return this.identifiers;
  }

  public Identifier getIdFromString(Object id) {
    final String idString = id.toString();
    Identifier identifier = this.stringIdMap.get(idString);
    if (identifier == null) {
      if (!this.codeTable.isCaseSensitive()) {
        final String lowerId = idString.toLowerCase();
        identifier = this.stringIdMap.get(lowerId);
      }
    }
    return identifier;
  }

  protected long getNextId() {
    return this.maxId.incrementAndGet();
  }

  @SuppressWarnings("unchecked")
  public <V> V getValueById(final Object id) {
    return (V)this.idValueCache.get(id);
  }

  public boolean hasIdentifier(final Identifier id) {
    return this.idValueCache.containsKey(id);
  }

  public boolean hasValue(final Object value) {
    return this.valueIdCache.containsKey(value);
  }

  public boolean isEmpty() {
    return this.idIdCache.isEmpty();
  }

  public void setValueToId(final Identifier id, final Object value) {
    this.valueIdCache.put(value, id);
  }

  protected void updateMaxId(final Identifier id) {
    if (id instanceof Number) {
      final long longValue = ((Number)id).longValue();
      this.maxId.updateAndGet(oldId -> Math.max(oldId, longValue));
    }
  }
}
