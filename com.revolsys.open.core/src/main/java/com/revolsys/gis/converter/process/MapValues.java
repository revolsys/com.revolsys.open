package com.revolsys.gis.converter.process;

import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordUtil;
import com.revolsys.data.record.schema.RecordDefinition;

public class MapValues extends
  AbstractSourceToTargetProcess<Record, Record> {
  private String sourceAttributeName;

  private String targetAttributeName;

  private Map<Object, Object> valueMap = new LinkedHashMap<Object, Object>();

  public MapValues() {
  }

  public MapValues(final String sourceAttributeName,
    final String targetAttributeName) {
    this.sourceAttributeName = sourceAttributeName;
    this.targetAttributeName = targetAttributeName;
  }

  public MapValues(final String sourceAttributeName,
    final String targetAttributeName, final Map<Object, Object> valueMap) {
    this.sourceAttributeName = sourceAttributeName;
    this.targetAttributeName = targetAttributeName;
    this.valueMap = valueMap;
  }

  public void addValueMap(final Object sourceValue, final Object targetValue) {
    valueMap.put(sourceValue, targetValue);
  }

  public String getSourceAttributeName() {
    return sourceAttributeName;
  }

  public String getTargetAttributeName() {
    return targetAttributeName;
  }

  public Map<Object, Object> getValueMap() {
    return valueMap;
  }

  @Override
  public void process(final Record source, final Record target) {
    final Object sourceValue = RecordUtil.getAttributeByPath(source,
      sourceAttributeName);
    if (sourceValue != null) {
      final Object targetValue = valueMap.get(sourceValue);
      if (targetValue != null) {
        final RecordDefinition targetMetaData = target.getMetaData();
        final CodeTable codeTable = targetMetaData.getCodeTableByColumn(targetAttributeName);
        if (codeTable == null) {
          target.setValue(targetAttributeName, targetValue);
        } else {
          final Object codeId = codeTable.getId(targetValue);
          target.setValue(targetAttributeName, codeId);
        }
      }
    }
  }

  public void setSourceAttributeName(final String sourceAttributeName) {
    this.sourceAttributeName = sourceAttributeName;
  }

  public void setTargetAttributeName(final String targetAttributeName) {
    this.targetAttributeName = targetAttributeName;
  }

  public void setValueMap(final Map<Object, Object> attributeNames) {
    this.valueMap = attributeNames;
  }

  @Override
  public String toString() {
    return "copy" + valueMap;
  }
}
