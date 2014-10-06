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

  private String targetFieldName;

  private Map<Object, Object> valueMap = new LinkedHashMap<Object, Object>();

  public MapValues() {
  }

  public MapValues(final String sourceAttributeName,
    final String targetFieldName) {
    this.sourceAttributeName = sourceAttributeName;
    this.targetFieldName = targetFieldName;
  }

  public MapValues(final String sourceAttributeName,
    final String targetFieldName, final Map<Object, Object> valueMap) {
    this.sourceAttributeName = sourceAttributeName;
    this.targetFieldName = targetFieldName;
    this.valueMap = valueMap;
  }

  public void addValueMap(final Object sourceValue, final Object targetValue) {
    valueMap.put(sourceValue, targetValue);
  }

  public String getSourceAttributeName() {
    return sourceAttributeName;
  }

  public String getTargetFieldName() {
    return targetFieldName;
  }

  public Map<Object, Object> getValueMap() {
    return valueMap;
  }

  @Override
  public void process(final Record source, final Record target) {
    final Object sourceValue = RecordUtil.getFieldByPath(source,
      sourceAttributeName);
    if (sourceValue != null) {
      final Object targetValue = valueMap.get(sourceValue);
      if (targetValue != null) {
        final RecordDefinition targetRecordDefinition = target.getRecordDefinition();
        final CodeTable codeTable = targetRecordDefinition.getCodeTableByColumn(targetFieldName);
        if (codeTable == null) {
          target.setValue(targetFieldName, targetValue);
        } else {
          final Object codeId = codeTable.getId(targetValue);
          target.setValue(targetFieldName, codeId);
        }
      }
    }
  }

  public void setSourceAttributeName(final String sourceAttributeName) {
    this.sourceAttributeName = sourceAttributeName;
  }

  public void setTargetFieldName(final String targetFieldName) {
    this.targetFieldName = targetFieldName;
  }

  public void setValueMap(final Map<Object, Object> attributeNames) {
    this.valueMap = attributeNames;
  }

  @Override
  public String toString() {
    return "copy" + valueMap;
  }
}
