package com.revolsys.format.esri.gdb.xml.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.util.CompareUtil;

public class CodedValueDomain extends Domain implements CodeTable {
  private List<CodedValue> codedValues = new ArrayList<CodedValue>();

  private Map<Identifier, List<Object>> idValueMap = new HashMap<>();

  private int maxId = 0;

  private Map<String, Identifier> stringIdMap = new HashMap<>();

  private JComponent swingEditor;

  private Map<String, Identifier> valueIdMap = new HashMap<>();

  public synchronized void addCodedValue(final Object code, final String name) {
    final Identifier identifier = Identifier.create(code);
    final CodedValue value = new CodedValue(code, name);
    this.codedValues.add(value);
    final List<Object> values = Collections.<Object> singletonList(name);
    this.idValueMap.put(identifier, values);
    this.stringIdMap.put(code.toString(), identifier);
    this.valueIdMap.put(name.toLowerCase(), identifier);
    if (code instanceof Number) {
      final int id = ((Number)code).intValue();
      if (this.maxId < id) {
        this.maxId = id;
      }
    }
  }

  public synchronized Identifier addCodedValue(final String name) {
    Object id;
    switch (getFieldType()) {
      case esriFieldTypeInteger:
        id = (int)++this.maxId;
      break;
      case esriFieldTypeSmallInteger:
        id = (short)++this.maxId;
      break;

      default:
        throw new RuntimeException("Cannot generate code for field type " + getFieldType());
    }
    addCodedValue(id, name);
    return Identifier.create(id);
  }

  @Override
  public CodedValueDomain clone() {
    final CodedValueDomain clone = (CodedValueDomain)super.clone();
    clone.idValueMap = new HashMap<>();
    clone.stringIdMap = new HashMap<>();
    clone.valueIdMap = new HashMap<>();
    clone.codedValues = new ArrayList<CodedValue>();
    for (final CodedValue codedValue : this.codedValues) {
      clone.addCodedValue(codedValue.getCode(), codedValue.getName());
    }
    return clone;
  }

  @Override
  public int compare(final Object value1, final Object value2) {
    if (value1 == null) {
      if (value2 == null) {
        return 0;
      } else {
        return 1;
      }
    } else if (value2 == null) {
      return -1;
    } else {
      final Object codeValue1 = getValue(Identifier.create(value1));
      final Object codeValue2 = getValue(Identifier.create(value2));
      return CompareUtil.compare(codeValue1, codeValue2);
    }
  }

  public List<CodedValue> getCodedValues() {
    return this.codedValues;
  }

  @Override
  public Map<Identifier, List<Object>> getCodes() {
    return Collections.unmodifiableMap(this.idValueMap);
  }

  @Override
  public List<String> getFieldAliases() {
    return Collections.emptyList();
  }

  @Override
  public Identifier getId(final Map<String, ? extends Object> values) {
    final Object name = getName(values);
    return getId(name);
  }

  @Override
  public Identifier getId(final Object... values) {
    if (values.length == 1) {
      final Object value = values[0];
      if (value == null) {
        return null;
      } else if (this.idValueMap.containsKey(value)) {
        return Identifier.create(value);
      } else if (this.stringIdMap.containsKey(value.toString())) {
        return this.stringIdMap.get(value.toString());
      } else {
        final String lowerValue = ((String)value).toLowerCase();
        final Identifier id = this.valueIdMap.get(lowerValue);
        return id;
      }
    } else {
      throw new IllegalArgumentException("Expecting only a single value " + values);
    }
  }

  @Override
  public List<Identifier> getIdentifiers() {
    return new ArrayList<>(this.idValueMap.keySet());
  }

  @Override
  public Identifier getIdExact(final Object... values) {
    return getId(values);
  }

  @Override
  public String getIdFieldName() {
    return getDomainName() + "_ID";
  }

  @Override
  public Map<String, ? extends Object> getMap(final Identifier id) {
    final Object value = getValue(id);
    return Collections.singletonMap("NAME", value);
  }

  @Override
  public String getName() {
    return super.getDomainName();
  }

  public String getName(final Map<String, ? extends Object> values) {
    return (String)values.get("NAME");
  }

  @Override
  public JComponent getSwingEditor() {
    return this.swingEditor;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getValue(final Identifier id) {
    final List<Object> values = getValues(id);
    if (values == null) {
      return null;
    } else {
      final Object value = values.get(0);
      return (V)value;
    }
  }

  @Override
  public <V> V getValue(final Object id) {
    return getValue(Identifier.create(id));
  }

  @Override
  public List<String> getValueFieldNames() {
    return Arrays.asList("NAME");
  }

  @Override
  public List<Object> getValues(final Identifier id) {
    if (id == null) {
      return null;
    } else {
      List<Object> values = this.idValueMap.get(id);
      if (values == null) {
        final Identifier objectId = this.stringIdMap.get(id.toString());
        if (objectId == null) {
          return null;
        } else {
          values = this.idValueMap.get(objectId);
        }
      }
      return Collections.unmodifiableList(values);
    }
  }

  @Override
  public boolean isEmpty() {
    return this.idValueMap.isEmpty();
  }

  @Override
  public boolean isLoaded() {
    return true;
  }

  @Override
  public boolean isLoading() {
    return false;
  }

  @Override
  public void refresh() {
  }

  public synchronized void setCodedValues(final List<CodedValue> codedValues) {
    this.codedValues = new ArrayList<CodedValue>();
    for (final CodedValue codedValue : codedValues) {
      final Object code = codedValue.getCode();
      final String name = codedValue.getName();
      addCodedValue(code, name);

    }
  }

  public void setSwingEditor(final JComponent swingEditor) {
    this.swingEditor = swingEditor;
  }
}
