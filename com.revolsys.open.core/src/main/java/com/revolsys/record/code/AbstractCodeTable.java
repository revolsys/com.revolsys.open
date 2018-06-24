package com.revolsys.record.code;

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

import com.revolsys.identifier.Identifier;
import com.revolsys.identifier.SingleIdentifier;
import com.revolsys.io.BaseCloseable;
import com.revolsys.properties.BaseObjectWithPropertiesAndChange;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.number.Numbers;

public abstract class AbstractCodeTable extends BaseObjectWithPropertiesAndChange
  implements BaseCloseable, CodeTable, Cloneable {

  private boolean capitalizeWords = false;

  private boolean caseSensitive = false;

  private List<Identifier> identifiers = new ArrayList<>();

  private Map<Identifier, Identifier> idIdCache = new LinkedHashMap<>();

  private Map<Identifier, List<Object>> idValueCache = new LinkedHashMap<>();

  private long maxId;

  private String name;

  private final Map<String, Identifier> stringIdMap = new HashMap<>();

  private JComponent swingEditor;

  private Map<List<Object>, Identifier> valueIdCache = new LinkedHashMap<>();

  public AbstractCodeTable() {
  }

  public AbstractCodeTable(final boolean capitalizeWords) {
    this.capitalizeWords = capitalizeWords;
  }

  protected synchronized void addValue(final Identifier id, final List<Object> values) {
    if (id instanceof Number) {
      final Number number = (Number)id;
      final long longValue = number.longValue();
      if (longValue > this.maxId) {
        this.maxId = longValue;
      }
    }

    this.identifiers.add(id);
    this.idValueCache.put(id, values);
    this.idIdCache.put(id, id);

    addValueId(id, values);
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

  protected void addValueId(final Identifier id, final List<Object> values) {
    this.valueIdCache.put(values, id);
    this.valueIdCache.put(getNormalizedValues(values), id);
  }

  protected synchronized void addValues(final Map<Identifier, List<Object>> valueMap) {
    for (final Entry<Identifier, List<Object>> entry : valueMap.entrySet()) {
      final Identifier id = entry.getKey();
      final List<Object> values = entry.getValue();
      addValue(id, values);
    }
  }

  @Override
  public AbstractCodeTable clone() {
    final AbstractCodeTable clone = (AbstractCodeTable)super.clone();
    clone.identifiers = new ArrayList<>(this.identifiers);
    clone.idValueCache = new LinkedHashMap<>(this.idValueCache);
    clone.idIdCache = new LinkedHashMap<>(this.idIdCache);
    clone.valueIdCache = new LinkedHashMap<>(this.valueIdCache);
    return clone;
  }

  @Override
  @PreDestroy
  public void close() {
    this.identifiers.clear();
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

  protected Identifier getIdByValue(final List<Object> valueList) {
    processValues(valueList);
    Identifier id = this.valueIdCache.get(valueList);
    if (id == null) {
      final List<Object> normalizedValues = getNormalizedValues(valueList);
      id = this.valueIdCache.get(normalizedValues);
    }
    return id;
  }

  @Override
  public Identifier getIdentifier(final List<Object> values, final boolean loadMissing) {
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
          } else {
            final Identifier identifier = Identifier.newIdentifier(id);
            final Object value = getValue(identifier);
            if (value != id) {
              return this.stringIdMap.get(lowerId);
            }
          }
        }
      }
    }

    processValues(values);
    Identifier id = getIdByValue(values);
    if (id == null && loadMissing) {
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
  public List<Identifier> getIdentifiers() {
    refreshIfNeeded();
    return Collections.unmodifiableList(this.identifiers);
  }

  @Override
  public Identifier getIdExact(final List<Object> values, final boolean loadValues) {
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
  public String getName() {
    return this.name;
  }

  protected synchronized long getNextId() {
    return ++this.maxId;
  }

  private List<Object> getNormalizedValues(final List<Object> values) {
    final List<Object> normalizedValues = new ArrayList<>();
    for (final Object value : values) {
      if (value == null) {
        normalizedValues.add(null);
      } else if (value instanceof Number) {
        final Number number = (Number)value;
        normalizedValues.add(Numbers.toString(number));
      } else {
        normalizedValues.add(value.toString().toLowerCase());
      }
    }
    return normalizedValues;
  }

  @Override
  public JComponent getSwingEditor() {
    return this.swingEditor;
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
  public List<Object> getValues(final Identifier id) {
    if (id != null) {
      List<Object> values = getValueById(id);
      if (values == null) {
        synchronized (this) {
          values = loadValues(id);
          if (values != null && !isLoadAll()) {
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

  @Override
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
    this.identifiers.clear();
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
