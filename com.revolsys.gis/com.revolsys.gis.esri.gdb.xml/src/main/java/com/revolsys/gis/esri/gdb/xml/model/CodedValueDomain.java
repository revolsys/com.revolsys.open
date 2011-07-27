package com.revolsys.gis.esri.gdb.xml.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.data.model.codes.CodeTable;

public class CodedValueDomain extends Domain implements CodeTable<Object> {
  private List<CodedValue> codedValues = new ArrayList<CodedValue>();

  private Map<Object, List<Object>> idValueMap = new HashMap<Object, List<Object>>();

  private Map<String, Object> valueIdMap = new HashMap<String, Object>();

  private int maxId = 0;


  public synchronized void addCodedValue(final Object code, final String name) {
    final CodedValue value = new CodedValue(code, name);
    codedValues.add(value);
    final List<Object> values = Collections.<Object> singletonList(name);
    idValueMap.put(code, values);
    valueIdMap.put(name.toLowerCase(), code);
    if (code instanceof Number) {
      final int id = ((Number)code).intValue();
      if (maxId < id) {
        maxId = id;
      }
    }
  }

  @Override
  public CodedValueDomain clone() {
    final CodedValueDomain clone = (CodedValueDomain)super.clone();
    clone.idValueMap = new HashMap<Object, List<Object>>();
    clone.valueIdMap = new HashMap<String, Object>();
    clone.codedValues = new ArrayList<CodedValue>();
    for (final CodedValue codedValue : codedValues) {
      clone.addCodedValue(codedValue.getCode(), codedValue.getName());
    }
    return clone;
  }

  public List<String> getAttributeAliases() {
    return Collections.emptyList();
  }

  public List<CodedValue> getCodedValues() {
    return codedValues;
  }

  public Map<Object, List<Object>> getCodes() {
    return Collections.unmodifiableMap(idValueMap);
  }

  public Object getId(final Map<String, ? extends Object> values) {
    final Object name = getName(values);
    return getId(name);
  }

  public Object getId(final Object... values) {
    if (values.length == 1) {
      final Object value = values[0];
      if (value == null) {
        return null;
      } else if (idValueMap.containsKey(value)) {
        return value;
      } else {
        String lowerValue = ((String)value).toLowerCase();
        final Object id = valueIdMap.get(lowerValue);
        return id;
      }
    } else {
      throw new IllegalArgumentException("Expecting only a single value "
        + values);
    }
  }

  public String getIdAttributeName() {
    return getDomainName() + "_ID";
  }

  public Map<String, ? extends Object> getMap(final Object id) {
    final Object value = getValue(id);
    return Collections.singletonMap("NAME", value);
  }

  public String getName(final Map<String, ? extends Object> values) {
    return (String)values.get("NAME");
  }

  @SuppressWarnings("unchecked")
  public <V> V getValue(final Object id) {
    final List<Object> values = getValues(id);
    final Object value = values.get(0);
    return (V)value;
  }

  public List<String> getValueAttributeNames() {
    return Arrays.asList("NAME");
  }

  public List<Object> getValues(final Object id) {
    final List<Object> values = idValueMap.get(id);
    return Collections.unmodifiableList(values);
  }

  public synchronized void setCodedValues(final List<CodedValue> codedValues) {
    this.codedValues = new ArrayList<CodedValue>();
    for (final CodedValue codedValue : codedValues) {
      final Object code = codedValue.getCode();
      final String name = codedValue.getName();
      addCodedValue(code, name);

    }
  }

  public synchronized Object addCodedValue(String name) {
    Object id;
    switch (getFieldType()) {
      case esriFieldTypeInteger:
        id = (int)++maxId;
      break;
      case esriFieldTypeSmallInteger:
        id = (short)++maxId;
      break;

      default:
        throw new RuntimeException("Cannot generate code for field type "
          + getFieldType());
    }
    addCodedValue(id, name);
    return id;
  }
}
