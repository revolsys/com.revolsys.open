package com.revolsys.gis.converter.process;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;

public class CopyValues extends
  AbstractSourceToTargetProcess<Record, Record> {
  private Map<String, String> attributeNames = new LinkedHashMap<String, String>();

  public CopyValues() {
  }

  public CopyValues(final Map<String, String> attributeNames) {
    this.attributeNames = attributeNames;
  }

  public CopyValues(final String sourceName, final String targetName) {
    addFieldName(sourceName, targetName);
  }

  public void addFieldName(final String sourceName, final String targetName) {
    attributeNames.put(sourceName, targetName);
  }

  public Map<String, String> getFieldNames() {
    return attributeNames;
  }

  @Override
  public void process(final Record source, final Record target) {
    for (final Entry<String, String> entry : attributeNames.entrySet()) {
      final String sourceName = entry.getKey();
      final String targetName = entry.getValue();
      final Object value;
      if (sourceName.startsWith("~")) {
        value = sourceName.substring(1);
      } else {
        value = source.getValueByPath(sourceName);
      }
      if (value != null) {
        final RecordDefinition targetRecordDefinition = target.getRecordDefinition();
        final CodeTable codeTable = targetRecordDefinition.getCodeTableByColumn(targetName);
        if (codeTable == null) {
          target.setValue(targetName, value);
        } else {
          final Object codeId = codeTable.getId(value);
          target.setValue(targetName, codeId);
        }
      }
    }
  }

  public void setAttributeNames(final Map<String, String> attributeNames) {
    this.attributeNames = attributeNames;
  }

  @Override
  public String toString() {
    return "copy" + attributeNames;
  }
}
