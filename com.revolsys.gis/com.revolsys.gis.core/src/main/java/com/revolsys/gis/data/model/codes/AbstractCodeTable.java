package com.revolsys.gis.data.model.codes;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.util.CaseConverter;

public abstract class AbstractCodeTable<T> implements CodeTable<T>, Cloneable {
  private boolean capitalizeWords = false;

  private Map<T, List<Object>> idValueCache = new LinkedHashMap<T, List<Object>>();

  private Map<List<Object>, T> valueIdCache = new LinkedHashMap<List<Object>, T>();

  private Map<String, T> stringIdMap = new HashMap<String, T>();

  private long maxId;

  public AbstractCodeTable() {
  }

  public AbstractCodeTable(final boolean capitalizeWords) {
    this.capitalizeWords = capitalizeWords;
  }

  protected synchronized long getNextId() {
    return ++maxId;
  }

  protected synchronized void addValue(final T id, final List<Object> values) {
    if (id instanceof Number) {
      Number number = (Number)id;
      final long longValue = number.longValue();
      if (longValue > maxId) {
        maxId = longValue;
      }
      Object newId = longValue;
      idValueCache.put((T)newId, values);
    } else {
    }
    valueIdCache.put(values, id);
    stringIdMap.put(id.toString().toLowerCase(), id);
  }

  protected void addValue(final T id, final Object... values) {
    final List<Object> valueList = Arrays.asList(values);
    addValue(id, valueList);

  }

  protected synchronized void addValues(final Map<T, List<Object>> valueMap) {

    for (final Entry<T, List<Object>> entry : valueMap.entrySet()) {
      final T id = entry.getKey();
      final List<Object> values = entry.getValue();
      addValue(id, values);
    }
  }

  @Override
  public AbstractCodeTable<T> clone() {
    try {
      final AbstractCodeTable<T> clone = (AbstractCodeTable<T>)super.clone();
      clone.idValueCache = new LinkedHashMap<T, List<Object>>(idValueCache);
      clone.valueIdCache = new LinkedHashMap<List<Object>, T>(valueIdCache);
      return clone;
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  public List<String> getAttributeAliases() {
    return Collections.emptyList();
  }

  public Map<T, List<Object>> getCodes() {
    return Collections.unmodifiableMap(idValueCache);
  }

  public T getId(final Map<String, ? extends Object> valueMap) {
    final List<String> valueAttributeNames = getValueAttributeNames();
    final Object[] values = new Object[valueAttributeNames.size()];
    for (int i = 0; i < values.length; i++) {
      final String name = valueAttributeNames.get(i);
      final Object value = valueMap.get(name);
      values[i] = value;
    }
    return getId(values);
  }

  public T getId(final Object... values) {
    if (values.length == 1) {
      final Object id = values[0];
      if (id == null) {
        return null;
      } else if (idValueCache.containsKey(id)) {
        return (T)id;
      } else {
        String lowerId = id.toString().toLowerCase();
        if (stringIdMap.containsKey(lowerId)) {
          return stringIdMap.get(lowerId);
        }
      }
    }

    final List<Object> valueList = Arrays.asList(values);
    processValues(valueList);
    T id = getIdByValue(valueList);
    if (id == null) {
      synchronized (this) {
        id = loadId(valueList, true);
        if (id != null) {
          addValue(id, valueList);
        }
      }
    }
    return id;
  }

  protected T getIdByValue(final List<Object> valueList) {
    processValues(valueList);
    return valueIdCache.get(valueList);
  }

  public Map<String, ? extends Object> getMap(final T id) {
    final List<Object> values = getValues(id);
    if (values == null) {
      return Collections.emptyMap();
    } else {
      final Map<String, Object> map = new HashMap<String, Object>();
      for (int i = 0; i < values.size(); i++) {
        final String name = getValueAttributeNames().get(i);
        final Object value = values.get(i);
        map.put(name, value);
      }
      return map;
    }
  }

  @SuppressWarnings("unchecked")
  public <V> V getValue(final T id) {
    final List<Object> values = getValues(id);
    if (values != null) {
      return (V)values.get(0);
    } else {
      return null;
    }
  }

  public List<String> getValueAttributeNames() {
    return Arrays.asList("VALUE");
  }

  protected List<Object> getValueById( T id) {
    List<Object> values = idValueCache.get(id);
    if (values == null) {
      String lowerId = id.toString().toLowerCase();
      if (stringIdMap.containsKey(lowerId)) {
        id= stringIdMap.get(lowerId);
        values = idValueCache.get(id);
      }
    }
    return values;
  }

  public List<Object> getValues(final T id) {
    if (id != null) {
      List<Object> values = getValueById(id);
      if (values == null) {
        synchronized (this) {
          values = loadValues(id);
          addValue(id, values);
        }
      }
      if (values != null) {
        return Collections.unmodifiableList(values);
      }

    }
    return null;

  }

  public boolean isCapitalizeWords() {
    return capitalizeWords;
  }

  protected T loadId(final List<Object> values, final boolean createId) {
    return null;
  }

  protected List<Object> loadValues(final T id) {
    return null;
  }

  private void processValues(final List<Object> valueList) {
    if (isCapitalizeWords()) {
      for (int i = 0; i < valueList.size(); i++) {
        final Object value = valueList.get(i);
        if (value != null) {
          final String newValue = CaseConverter.toCapitalizedWords(value.toString());
          valueList.set(i, newValue);
        }
      }
    }
  }

  public void setCapitalizeWords(final boolean capitalizedWords) {
    this.capitalizeWords = capitalizedWords;
  }
}
