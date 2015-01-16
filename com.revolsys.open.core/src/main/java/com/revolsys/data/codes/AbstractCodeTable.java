package com.revolsys.data.codes;

import java.beans.PropertyChangeSupport;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PreDestroy;
import javax.swing.JComponent;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.identifier.SingleIdentifier;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.MathUtil;

public abstract class AbstractCodeTable implements Closeable,
PropertyChangeSupportProxy, CodeTable, Cloneable {

  private boolean capitalizeWords = false;

  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  private Map<Identifier, List<Object>> idValueCache = new LinkedHashMap<>();

  private Map<Identifier, Identifier> idIdCache = new LinkedHashMap<>();

  private Map<List<Object>, Identifier> valueIdCache = new LinkedHashMap<>();

  private final Map<String, Identifier> stringIdMap = new HashMap<>();

  private JComponent swingEditor;

  private long maxId;

  private String name;

  private boolean caseSensitive = false;

  public AbstractCodeTable() {
  }

  public AbstractCodeTable(final boolean capitalizeWords) {
    this.capitalizeWords = capitalizeWords;
  }

  protected synchronized void addValue(final Identifier id,
    final List<Object> values) {
    if (id instanceof Number) {
      final Number number = (Number)id;
      final long longValue = number.longValue();
      if (longValue > this.maxId) {
        this.maxId = longValue;
      }
    }
    this.idValueCache.put(id, values);
    this.idIdCache.put(id, id);
    this.valueIdCache.put(values, id);
    this.valueIdCache.put(getNormalizedValues(values), id);
    String lowerId = id.toString();
    if (!this.caseSensitive) {
      lowerId = lowerId.toLowerCase();
    }
    this.stringIdMap.put(lowerId, id);
  }

  protected void addValue(final Identifier id, final Object... values) {
    final List<Object> valueList = Arrays.asList(values);
    addValue(id, valueList);

  }

  protected synchronized void addValues(
    final Map<Identifier, List<Object>> valueMap) {

    for (final Entry<Identifier, List<Object>> entry : valueMap.entrySet()) {
      final Identifier id = entry.getKey();
      final List<Object> values = entry.getValue();
      addValue(id, values);
    }
  }

  @Override
  public AbstractCodeTable clone() {
    try {
      final AbstractCodeTable clone = (AbstractCodeTable)super.clone();
      clone.idValueCache = new LinkedHashMap<>(this.idValueCache);
      clone.idIdCache = new LinkedHashMap<>(this.idIdCache);
      clone.valueIdCache = new LinkedHashMap<>(this.valueIdCache);
      return clone;
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  @PreDestroy
  public void close() {
    this.propertyChangeSupport = null;
    this.idValueCache.clear();
    this.idIdCache.clear();
    this.stringIdMap.clear();
    this.valueIdCache.clear();
    this.swingEditor = null;
  }

  @Override
  public Map<Identifier, List<Object>> getCodes() {
    return Collections.unmodifiableMap(this.idValueCache);
  }

  @Override
  public List<String> getFieldAliases() {
    return Collections.emptyList();
  }

  public Identifier getId(final List<Object> values) {
    return getId(values, true);
  }

  public Identifier getId(final List<Object> values, final boolean loadValues) {
    if (values.size() == 1) {
      final Object id = values.get(0);
      if (id == null) {
        return null;
      } else {
        final Identifier cachedId = this.idIdCache.get(id);
        if (cachedId != null) {
          return cachedId;
        } else {
          String lowerId = id.toString();
          if (!this.caseSensitive) {
            lowerId = lowerId.toLowerCase();
          }
          if (this.stringIdMap.containsKey(lowerId)) {
            return this.stringIdMap.get(lowerId);
          }
        }
      }
    }

    processValues(values);
    Identifier id = getIdByValue(values);
    if (id == null && loadValues) {
      synchronized (this) {
        id = loadId(values, true);
        if (id != null && !this.idValueCache.containsKey(id)) {
          addValue(id, values);
        }
      }
    }
    return id;
  }

  @Override
  public Identifier getId(final Map<String, ? extends Object> valueMap) {
    final List<String> valueFieldNames = getValueFieldNames();
    final Object[] values = new Object[valueFieldNames.size()];
    for (int i = 0; i < values.length; i++) {
      final String name = valueFieldNames.get(i);
      final Object value = valueMap.get(name);
      values[i] = value;
    }
    return getId(values);
  }

  @Override
  public Identifier getId(final Object... values) {
    final List<Object> valueList = Arrays.asList(values);
    return getId(valueList);
  }

  protected Identifier getIdByValue(final List<Object> valueList) {
    processValues(valueList);
    Identifier id = this.valueIdCache.get(valueList);
    if (id == null) {
      final List<Object> normalizedValues = getNormalizedValues(valueList);
      id = this.valueIdCache.get(normalizedValues);
    }
    return id;
  }

  public Identifier getIdExact(final List<Object> values) {
    return getIdExact(values, true);
  }

  public Identifier getIdExact(final List<Object> values,
    final boolean loadValues) {
    Identifier id = this.valueIdCache.get(values);
    if (id == null && loadValues) {
      synchronized (this) {
        id = loadId(values, false);
        return this.valueIdCache.get(values);
      }
    }
    return id;
  }

  @Override
  public Identifier getIdExact(final Object... values) {
    final List<Object> valueList = Arrays.asList(values);
    return getIdExact(valueList);
  }

  @Override
  public Map<String, ? extends Object> getMap(final Identifier id) {
    final List<Object> values = getValues(id);
    if (values == null) {
      return Collections.emptyMap();
    } else {
      final Map<String, Object> map = new HashMap<String, Object>();
      for (int i = 0; i < values.size(); i++) {
        final String name = getValueFieldNames().get(i);
        final Object value = values.get(i);
        map.put(name, value);
      }
      return map;
    }
  }

  @Override
  public String getName() {
    return this.name;
  }

  protected synchronized long getNextId() {
    return ++this.maxId;
  }

  List<Object> getNormalizedValues(final List<Object> values) {
    final List<Object> normalizedValues = new ArrayList<Object>();
    for (final Object value : values) {
      if (value == null) {
        normalizedValues.add(null);
      } else if (value instanceof Number) {
        final Number number = (Number)value;
        normalizedValues.add(MathUtil.toString(number));
      } else {
        normalizedValues.add(value.toString().toLowerCase());
      }
    }
    return normalizedValues;
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  @Override
  public JComponent getSwingEditor() {
    return this.swingEditor;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getValue(final Identifier id) {
    final List<Object> values = getValues(id);
    if (values != null) {
      return (V)values.get(0);
    } else {
      return null;
    }
  }

  @Override
  public <V> V getValue(final Object id) {
    return getValue(SingleIdentifier.create(id));
  }

  protected List<Object> getValueById(Object id) {
    if (this.valueIdCache.containsKey(Collections.singletonList(id))) {
      if (id instanceof SingleIdentifier) {
        final SingleIdentifier identifier = (SingleIdentifier)id;
        return Collections.singletonList(identifier.getValue(0));
      } else {
        return Collections.singletonList(id);
      }
    } else {
      List<Object> values = this.idValueCache.get(id);
      if (values == null) {
        String lowerId = id.toString();
        if (!this.caseSensitive) {
          lowerId = lowerId.toLowerCase();
        }
        if (this.stringIdMap.containsKey(lowerId)) {
          id = this.stringIdMap.get(lowerId);
          values = this.idValueCache.get(id);
        }
      }
      return values;
    }
  }

  @Override
  public List<String> getValueFieldNames() {
    return Arrays.asList("VALUE");
  }

  @Override
  public List<Object> getValues(final Identifier id) {
    if (id != null) {
      List<Object> values = getValueById(id);
      if (values == null) {
        synchronized (this) {
          values = loadValues(id);
          if (values != null) {
            addValue(id, values);
          }
        }
      }
      if (values != null) {
        return Collections.unmodifiableList(values);
      }

    }
    return null;

  }

  public boolean isCapitalizeWords() {
    return this.capitalizeWords;
  }

  public boolean isCaseSensitive() {
    return this.caseSensitive;
  }

  public boolean isEmpty() {
    return this.idIdCache.isEmpty();
  }

  protected Identifier loadId(final List<Object> values, final boolean createId) {
    return null;
  }

  protected List<Object> loadValues(final Object id) {
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

  @Override
  public synchronized void refresh() {
    this.idValueCache.clear();
    this.stringIdMap.clear();
    this.valueIdCache.clear();
  }

  public void setCapitalizeWords(final boolean capitalizedWords) {
    this.capitalizeWords = capitalizedWords;
  }

  public void setCaseSensitive(final boolean caseSensitive) {
    this.caseSensitive = caseSensitive;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setSwingEditor(final JComponent swingEditor) {
    this.swingEditor = swingEditor;
  }
}
