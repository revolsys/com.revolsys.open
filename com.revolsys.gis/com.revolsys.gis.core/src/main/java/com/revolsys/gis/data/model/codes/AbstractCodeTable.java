package com.revolsys.gis.data.model.codes;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.util.CaseConverter;

public abstract class AbstractCodeTable implements CodeTable {
  private boolean capitalizeWords = false;

  private Map<Number, List<Object>> idValueCache = new LinkedHashMap<Number, List<Object>>();

  private Map<List<Object>, Number> valueIdCache = new LinkedHashMap<List<Object>, Number>();

  public AbstractCodeTable() {
  }

  public AbstractCodeTable(
    final boolean capitalizeWords) {
    this.capitalizeWords = capitalizeWords;
  }

  protected synchronized void addValue(
    final Number id,
    final List<Object> values) {
    final Map<Number, List<Object>> idValueCache = new LinkedHashMap<Number, List<Object>>(
      this.idValueCache);
    idValueCache.put(id.longValue(), values);
    this.idValueCache = idValueCache;

    final Map<List<Object>, Number> valueIdCache = new LinkedHashMap<List<Object>, Number>(
      this.valueIdCache);
    valueIdCache.put(values, id.longValue());
    this.valueIdCache = valueIdCache;

  }

  protected void addValue(
    final Number id,
    final Object... values) {
    final List<Object> valueList = Arrays.asList(values);
    addValue(id, valueList);

  }

  protected synchronized void addValues(
    final Map<Number, List<Object>> valueMap) {
    final Map<Number, List<Object>> idValueCache = new LinkedHashMap<Number, List<Object>>(
      this.idValueCache);
    final Map<List<Object>, Number> valueIdCache = new LinkedHashMap<List<Object>, Number>(
      this.valueIdCache);

    for (final Entry<Number, List<Object>> entry : valueMap.entrySet()) {
      final Long id = entry.getKey().longValue();
      final List<Object> values = entry.getValue();
      idValueCache.put(id, values);
      valueIdCache.put(values, id);
    }
    this.idValueCache = idValueCache;
    this.valueIdCache = valueIdCache;

  }

  @Override
  public abstract CodeTable clone();

  public Map<Number, List<Object>> getCodes() {
    return Collections.unmodifiableMap(idValueCache);
  }

  public Number getId(
    final Map<String, ? extends Object> values) {
    throw new UnsupportedOperationException();

  }

  public Number getId(
    final Object... values) {
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

  protected Number getIdByValue(
    final List<Object> valueList) {
    processValues(valueList);
    return valueIdCache.get(valueList);
  }

  public Map<String, ? extends Object> getMap(
    final Number id) {
    throw new UnsupportedOperationException();

  }

  public <V> V getValue(
    final Number id) {
    final List<Object> values = getValues(id);
    if (values != null) {
      return (V)values.get(0);
    } else {
      return null;
    }
  }

  protected List<Object> getValueById(
    final Number id) {
    return idValueCache.get(id.longValue());
  }

  public List<Object> getValues(
    final Number id) {
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

  protected Number loadId(
    final List<Object> values,
    final boolean createId) {
    return null;
  }

  protected List<Object> loadValues(
    final Number id) {
    return null;
  }

  private void processValues(
    final List<Object> valueList) {
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

  public void setCapitalizeWords(
    final boolean capitalizedWords) {
    this.capitalizeWords = capitalizedWords;
  }
}
