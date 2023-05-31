package com.revolsys.record.io.format.esri.gdb.xml.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.jeometry.common.compare.CompareUtil;
import org.jeometry.common.data.identifier.Identifier;

import com.revolsys.record.code.AbstractCodeTable;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.MergePolicyType;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.SplitPolicyType;
import com.revolsys.record.io.format.json.JsonObject;

public class Domain extends AbstractCodeTable {
  private List<CodedValue> codedValues = new ArrayList<>();

  private String description;

  private String domainName;

  private FieldType fieldType = FieldType.esriFieldTypeSmallInteger;

  private String maxValue;

  private MergePolicyType mergePolicy = MergePolicyType.esriMPTAreaWeighted;

  private String minValue;

  private String owner;

  private SplitPolicyType splitPolicy = SplitPolicyType.esriSPTDuplicate;

  private JComponent swingEditor;

  private int maxId;

  public Domain() {
  }

  public Domain(final String domainName, final FieldType fieldType, final String description) {
    this.domainName = domainName;
    this.fieldType = fieldType;
    this.description = description;
  }

  public synchronized Domain addCodedValue(final Object code, final String name) {
    final Identifier identifier = Identifier.newIdentifier(code);
    final CodedValue value = new CodedValue(code, name);
    this.codedValues.add(value);
    super.addEntry(identifier, name);
    if (code instanceof Number) {
      final int id = ((Number)code).intValue();
      if (this.maxId < id) {
        this.maxId = id;
      }
    }
    return this;
  }

  public synchronized Domain addCodedValue(final String name) {
    newCodedValue(name);
    return this;
  }

  @Override
  public Domain clone() {
    final Domain clone = (Domain)super.clone();
    clone.codedValues = new ArrayList<>();
    for (final CodedValue codedValue : this.codedValues) {
      clone.addCodedValue(codedValue.getCode(), codedValue.getName());
    }
    return clone;
  }

  @Override
  public void close() {
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
      final Object codeValue1 = getValue(Identifier.newIdentifier(value1));
      final Object codeValue2 = getValue(Identifier.newIdentifier(value2));
      return CompareUtil.compare(codeValue1, codeValue2);
    }
  }

  public List<CodedValue> getCodedValues() {
    return this.codedValues;
  }

  public String getDescription() {
    return this.description;
  }

  public String getDomainName() {
    return this.domainName;
  }

  @Override
  public List<String> getFieldNameAliases() {
    return Collections.emptyList();
  }

  public FieldType getFieldType() {
    return this.fieldType;
  }

  @Override
  public Identifier getIdentifier(final Map<String, ? extends Object> values) {
    if (this.codedValues.isEmpty()) {
      return null;
    } else {
      final Object name = getName(values);
      return getIdentifier(name);
    }
  }

  @Override
  public String getIdFieldName() {
    return getDomainName() + "_ID";
  }

  @Override
  public JsonObject getMap(final Identifier id) {
    final Object value = getValue(id);
    return JsonObject.hash("NAME", value);
  }

  public String getMaxValue() {
    return this.maxValue;
  }

  public MergePolicyType getMergePolicy() {
    return this.mergePolicy;
  }

  public String getMinValue() {
    return this.minValue;
  }

  @Override
  public String getName() {
    return getDomainName();
  }

  public String getName(final Map<String, ? extends Object> values) {
    return (String)values.get("NAME");
  }

  public String getOwner() {
    return this.owner;
  }

  public SplitPolicyType getSplitPolicy() {
    return this.splitPolicy;
  }

  @Override
  public JComponent getSwingEditor() {
    return this.swingEditor;
  }

  @Override
  public List<String> getValueFieldNames() {
    return Arrays.asList("NAME");
  }

  public boolean hasCodedValues() {
    return !isEmpty();
  }

  public synchronized Identifier newCodedValue(final String name) {
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
    return Identifier.newIdentifier(id);
  }

  public synchronized void setCodedValues(final List<CodedValue> codedValues) {
    this.codedValues = new ArrayList<>();
    for (final CodedValue codedValue : codedValues) {
      final Object code = codedValue.getCode();
      final String name = codedValue.getName();
      addCodedValue(code, name);

    }
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public void setDomainName(final String domainName) {
    this.domainName = domainName;
  }

  public void setFieldType(final FieldType fieldType) {
    this.fieldType = fieldType;
  }

  public void setMaxValue(final String maxValue) {
    this.maxValue = maxValue;
  }

  public void setMergePolicy(final MergePolicyType mergePolicy) {
    this.mergePolicy = mergePolicy;
  }

  public void setMinValue(final String minValue) {
    this.minValue = minValue;
  }

  public void setOwner(final String owner) {
    this.owner = owner;
  }

  public void setSplitPolicy(final SplitPolicyType splitPolicy) {
    this.splitPolicy = splitPolicy;
  }

  @Override
  public void setSwingEditor(final JComponent swingEditor) {
    this.swingEditor = swingEditor;
  }

  @Override
  public String toString() {
    return this.domainName;
  }

}
