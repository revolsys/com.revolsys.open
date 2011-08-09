package com.revolsys.gis.converter.process;

import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.gis.data.model.codes.CodeTable;

public class MapValues implements SourceToTargetProcess<DataObject, DataObject> {
  private String sourceAttributeName;

  private String targetAttributeName;

  private Map<Object, Object> valueMap = new LinkedHashMap<Object, Object>();

  public MapValues() {
  }

  public MapValues(
    final String sourceAttributeName,
    final String targetAttributeName) {
    this.sourceAttributeName = sourceAttributeName;
    this.targetAttributeName = targetAttributeName;
  }

  public MapValues(
    final String sourceAttributeName,
    final String targetAttributeName,
    final Map<Object, Object> valueMap) {
    this.sourceAttributeName = sourceAttributeName;
    this.targetAttributeName = targetAttributeName;
    this.valueMap = valueMap;
  }

  public void addValueMap(
    final Object sourceValue,
    final Object targetValue) {
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

  public void process(
    final DataObject source,
    final DataObject target) {
    final Object sourceValue = DataObjectUtil.getAttributeByPath(source,
      sourceAttributeName);
    if (sourceValue != null) {
      final Object targetValue = valueMap.get(sourceValue);
      if (targetValue != null) {
        final DataObjectMetaData targetMetaData = target.getMetaData();
        final DataObjectStore targetDataObjectStore = targetMetaData.getDataObjectStore();
        final CodeTable codeTable = targetDataObjectStore.getCodeTableByColumn(targetAttributeName);
        if (codeTable == null) {
          target.setValue(targetAttributeName, targetValue);
        } else {
          final Object codeId = codeTable.getId(targetValue);
          target.setValue(targetAttributeName, codeId);
        }
      }
    }
  }

  public void setSourceAttributeName(
    final String sourceAttributeName) {
    this.sourceAttributeName = sourceAttributeName;
  }

  public void setTargetAttributeName(
    final String targetAttributeName) {
    this.targetAttributeName = targetAttributeName;
  }

  public void setValueMap(
    final Map<Object, Object> attributeNames) {
    this.valueMap = attributeNames;
  }

  @Override
  public String toString() {
    return "copy" + valueMap;
  }
}
