package com.revolsys.gis.converter.process;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.core.convert.converter.Converter;

import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;

public class SetCodeTableId extends
  AbstractSourceToTargetProcess<Record, Record> {
  private final CodeTable codeTable;

  private final Map<String, Converter<Record, Object>> codeTableValueConverters = new HashMap<String, Converter<Record, Object>>();

  private final String targetAttributeName;

  public SetCodeTableId(final CodeTable codeTable,
    final String targetAttributeName) {
    this.codeTable = codeTable;
    this.targetAttributeName = targetAttributeName;
  }

  @Override
  public void process(final Record source, final Record target) {
    final Map<String, Object> codeTableValues = new HashMap<String, Object>();

    for (final Entry<String, Converter<Record, Object>> entry : codeTableValueConverters.entrySet()) {
      String codeTableAttributeName = entry.getKey();
      final Converter<Record, Object> sourceAttributeConverter = entry.getValue();
      Object sourceValue = sourceAttributeConverter.convert(source);
      if (sourceValue != null) {
        final RecordDefinition targetMetaData = target.getMetaData();
        String codeTableValueName = null;
        final int dotIndex = codeTableAttributeName.indexOf(".");
        if (dotIndex != -1) {
          codeTableValueName = codeTableAttributeName.substring(dotIndex + 1);
          codeTableAttributeName = codeTableAttributeName.substring(0, dotIndex);
        }
        final CodeTable targetCodeTable = targetMetaData.getCodeTableByColumn(codeTableAttributeName);
        if (targetCodeTable != null) {
          if (codeTableValueName == null) {
            sourceValue = targetCodeTable.getId(sourceValue);
          } else {
            sourceValue = targetCodeTable.getId(Collections.singletonMap(
              codeTableValueName, sourceValue));
          }
        }
      }
      codeTableValues.put(codeTableAttributeName, sourceValue);
    }
    final Object codeId = codeTable.getId(codeTableValues);
    target.setValue(targetAttributeName, codeId);
  }

  public void setValueMapping(final String codeTableAttribute,
    final Converter<Record, Object> valueConverter) {
    codeTableValueConverters.put(codeTableAttribute, valueConverter);

  }

  @Override
  public String toString() {
    return "setCodeTableId" + codeTableValueConverters;
  }
}
