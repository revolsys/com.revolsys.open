package com.revolsys.gis.parallel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import com.revolsys.data.record.ArrayRecord;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionFactory;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class CopyProcess extends BaseInOutProcess<Record, Record> {

  private String typeName;

  private RecordDefinitionFactory recordDefinitionFactory;

  private RecordDefinition recordDefinition;

  private Map<String, Map<Object, Object>> valueMaps = new HashMap<String, Map<Object, Object>>();

  private Map<String, String> attributeMap = new HashMap<String, String>();

  public CopyProcess() {
  }

  protected Record copy(final Record object) {
    Record targetObject;
    if (recordDefinition == null) {
      targetObject = object;
    } else {
      targetObject = new ArrayRecord(recordDefinition);
      for (final String attributeName : recordDefinition.getAttributeNames()) {
        copyAttribute(object, attributeName, targetObject, attributeName);
      }
      if (attributeMap != null) {
        for (final Entry<String, String> mapping : attributeMap.entrySet()) {
          final String sourceAttributeName = mapping.getKey();
          final String targetAttributeName = mapping.getValue();
          copyAttribute(object, sourceAttributeName, targetObject,
            targetAttributeName);
        }
      }
    }
    return targetObject;
  }

  private void copyAttribute(final Record sourceObject,
    final String sourceAttributeName, final Record targetObject,
    final String targetAttributeName) {
    Object value = sourceObject.getValueByPath(sourceAttributeName);
    final Map<Object, Object> valueMap = valueMaps.get(targetAttributeName);
    if (valueMap != null) {
      final Object mappedValue = valueMap.get(value);
      if (mappedValue != null) {
        value = mappedValue;
      }
    }
    targetObject.setValue(targetAttributeName, value);
  }

  public Map<String, String> getAttributeMap() {
    return attributeMap;
  }

  public RecordDefinition getRecordDefinition() {
    return recordDefinition;
  }

  public RecordDefinitionFactory getRecordDefinitionFactory() {
    return recordDefinitionFactory;
  }

  public String getTypeName() {
    return typeName;
  }

  public Map<String, Map<Object, Object>> getValueMaps() {
    return valueMaps;
  }

  @Override
  @PostConstruct
  protected void init() {
    super.init();
    if (recordDefinition == null) {
      recordDefinition = recordDefinitionFactory.getRecordDefinition(typeName);
    }
  }

  @Override
  protected void process(final Channel<Record> in,
    final Channel<Record> out, final Record object) {
    final Record targetObject = copy(object);
    out.write(targetObject);
  }

  public void setAttributeMap(final Map<String, String> attributeMap) {
    this.attributeMap = attributeMap;
  }

  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
  }

  public void setRecordDefinitionFactory(final RecordDefinitionFactory recordDefinitionFactory) {
    this.recordDefinitionFactory = recordDefinitionFactory;
  }

  public void setTypeName(final String typeName) {
    this.typeName = typeName;
  }

  public void setValueMaps(final Map<String, Map<Object, Object>> valueMaps) {
    this.valueMaps = valueMaps;
  }

}
