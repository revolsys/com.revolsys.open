package com.revolsys.gis.data.model.codes;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.util.CaseConverter;

public abstract class AbstractCodeTable implements CodeTable<Number>, Cloneable {
  private boolean capitalizeWords = false;

  private Map<Number, List<Object>> idValueCache = new LinkedHashMap<Number, List<Object>>();

  private Map<List<Object>, Number> valueIdCache = new LinkedHashMap<List<Object>, Number>();

  private long maxId;

  public AbstractCodeTable() {
  }

  public AbstractCodeTable(final boolean capitalizeWords) {
    this.capitalizeWords = capitalizeWords;
  }

  protected synchronized Number getNextId() {
    return ++maxId;
  }

  protected synchronized void addValue(final Number id,
    final List<Object> values) {
    final long longValue = id.longValue();
    if (longValue > maxId) {
      maxId = longValue;
    }
    idValueCache.put(id.longValue(), values);
    valueIdCache.put(values, id.longValue());
  }

  protected void addValue(final Number id, final Object... values) {
    final List<Object> valueList = Arrays.asList(values);
    addValue(id, valueList);

  }

  protected synchronized void addValues(final Map<Number, List<Object>> valueMap) {

    for (final Entry<Number, List<Object>> entry : valueMap.entrySet()) {
      final Long id = entry.getKey().longValue();
      final List<Object> values = entry.getValue();
      addValue(id, values);
    }
  }

  @Override
  public AbstractCodeTable clone() {
    try {
      final AbstractCodeTable clone = (AbstractCodeTable)super.clone();
      clone.idValueCache = new LinkedHashMap<Number, List<Object>>(idValueCache);
      clone.valueIdCache = new LinkedHashMap<List<Object>, Number>(valueIdCache);
      return clone;
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  public List<String> getAttributeAliases() {
    return Collections.emptyList();
  }

  public Map<Number, List<Object>> getCodes() {
    return Collections.unmodifiableMap(idValueCache);
  }

  public Number getId(final Map<String, ? extends Object> valueMap) {
    final List<String> valueAttributeNames = getValueAttributeNames();
    final Object[] values = new Object[valueAttributeNames.size()];
    for (int i = 0; i < values.length; i++) {
      final String name = valueAttributeNames.get(i);
      final Object value = valueMap.get(name);
      values[i] = value;
    }
    return getId(values);
  }

  public Number getId(final Object... values) {
    if (values.length == 1) {
      Object firstValue = values[0];
      if (firstValue instanceof Number) {
        Number id = (Number)firstValue;
        List<Object> foundValues = getValues(id);
        if (foundValues != null) {
          return id;
        }
      }
    }

    final List<Object> valueList = Arrays.asList(values);
    processValues(valueList);
    Number id = getIdByValue(valueList);
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

  protected Number getIdByValue(final List<Object> valueList) {
    processValues(valueList);
    return valueIdCache.get(valueList);
  }

  public Map<String, ? extends Object> getMap(final Number id) {
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
  public <V> V getValue(final Number id) {
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

  protected List<Object> getValueById(final Number id) {
    return idValueCache.get(id.longValue());
  }

  public List<Object> getValues(final Number id) {
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

  protected Number loadId(final List<Object> values, final boolean createId) {
    return null;
  }

  protected List<Object> loadValues(final Number id) {
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
