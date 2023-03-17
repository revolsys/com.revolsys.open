package com.revolsys.record.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JComponent;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.number.Numbers;

import com.revolsys.io.BaseCloseable;
import com.revolsys.properties.BaseObjectWithPropertiesAndChange;
import com.revolsys.record.schema.FieldDefinition;

public abstract class AbstractCodeTable extends BaseObjectWithPropertiesAndChange
  implements BaseCloseable, CodeTable, Cloneable {

  protected boolean caseSensitive = false;

  private List<Identifier> identifiers = new ArrayList<>();

  private Map<Identifier, Identifier> idIdCache = new LinkedHashMap<>();

  private Map<Identifier, Object> idValueCache = new LinkedHashMap<>();

  private final AtomicLong maxId = new AtomicLong();

  private String name;

  private Map<String, Identifier> stringIdMap = new HashMap<>();

  private JComponent swingEditor;

  private FieldDefinition valueFieldDefinition = new FieldDefinition("value", DataTypes.STRING,
    true);

  protected int valueFieldLength = -1;

  private Map<Object, Identifier> valueIdCache = new LinkedHashMap<>();

  public AbstractCodeTable() {
  }

  public void addIdentifier(final Identifier id) {
    updateMaxId(id);
    this.identifiers.add(id);
    this.idIdCache.put(id, id);
    final String idString = id.toString();
    this.stringIdMap.put(idString, id);
    if (!isCaseSensitive()) {
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
      final int valueLength = value.toString().length();
      if (valueLength > length) {
        length = valueLength;
      }
    }
    return length;
  }

  public void clear() {
    this.idIdCache.clear();
    this.stringIdMap.clear();
    this.identifiers.clear();
    this.idValueCache.clear();
    this.valueIdCache.clear();
  }

  @Override
  public AbstractCodeTable clone() {
    final AbstractCodeTable clone = (AbstractCodeTable)super.clone();
    clone.idIdCache = new LinkedHashMap<>(this.idIdCache);
    clone.stringIdMap = new LinkedHashMap<>(this.stringIdMap);
    clone.identifiers = new ArrayList<>(this.identifiers);
    clone.idValueCache = new LinkedHashMap<>(this.idValueCache);
    clone.valueIdCache = new LinkedHashMap<>(this.valueIdCache);
    return clone;
  }

  @Override
  public void close() {
    clear();
    this.swingEditor = null;
  }

  @Override
  public Identifier getIdentifier(final Object value) {
    Identifier identifier = null;
    if (value != null) {
      identifier = this.idIdCache.get(value);
      if (identifier == null) {
        identifier = getIdFromString(value);
        if (identifier == null) {
          identifier = this.valueIdCache.get(value);
        }
      }
    }
    return identifier;
  }

  @Override
  public List<Identifier> getIdentifiers() {
    return Collections.unmodifiableList(this.identifiers);
  }

  @Override
  public Identifier getIdExact(final Object value) {
    return this.valueIdCache.get(value);
  }

  @Override
  public String getIdFieldName() {
    return getName();
  }

  public Identifier getIdFromString(Object id) {
    final String idString = id.toString();
    Identifier identifier = this.stringIdMap.get(idString);
    if (identifier == null) {
      if (!isCaseSensitive()) {
        final String lowerId = idString.toLowerCase();
        identifier = this.stringIdMap.get(lowerId);
      }
    }
    return identifier;
  }

  @Override
  public String getName() {
    return this.name;
  }

  protected long getNextId() {
    return this.maxId.incrementAndGet();
  }

  protected Object getNormalizedValue(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return Numbers.toString(number);
    } else if (this.caseSensitive) {
      return value;
    } else {
      return value.toString().toLowerCase();
    }
  }

  @Override
  public JComponent getSwingEditor() {
    return this.swingEditor;
  }

  @Override
  public FieldDefinition getValueFieldDefinition() {
    return this.valueFieldDefinition;
  }

  @Override
  public int getValueFieldLength() {
    if (this.valueFieldLength == -1) {
      final int length = calculateValueFieldLength();
      this.valueFieldLength = length;
    }
    return this.valueFieldLength;
  }

  @SuppressWarnings("unchecked")
  protected <V> V getValueFromId(final Object id) {
    return (V)this.idValueCache.get(id);
  }

  public boolean hasIdentifier(final Identifier id) {
    return this.idValueCache.containsKey(id);
  }

  public boolean hasValue(final Object value) {
    return this.valueIdCache.containsKey(value);
  }

  public boolean isCaseSensitive() {
    return this.caseSensitive;
  }

  @Override
  public boolean isEmpty() {
    return this.identifiers.isEmpty();
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

  public void setValueFieldDefinition(final FieldDefinition valueFieldDefinition) {
    this.valueFieldDefinition = valueFieldDefinition;
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
