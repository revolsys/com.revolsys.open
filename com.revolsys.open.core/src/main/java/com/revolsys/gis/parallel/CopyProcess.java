package com.revolsys.gis.parallel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataFactory;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class CopyProcess extends BaseInOutProcess<DataObject, DataObject> {

  private String typeName;

  private DataObjectMetaDataFactory metaDataFactory;

  private DataObjectMetaData metaData;

  private Map<String, Map<Object, Object>> valueMaps = new HashMap<String, Map<Object, Object>>();

  private Map<String, String> attributeMap = new HashMap<String, String>();

  public CopyProcess() {
  }

  private void copyAttribute(
    final DataObject sourceObject,
    final String sourceAttributeName,
    final DataObject targetObject,
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

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  public DataObjectMetaDataFactory getMetaDataFactory() {
    return metaDataFactory;
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
    if (metaData == null) {
      metaData = metaDataFactory.getMetaData(typeName);
    }
  }

  @Override
  protected void process(
    final Channel<DataObject> in,
    final Channel<DataObject> out,
    final DataObject object) {
    if (metaData == null) {
      out.write(object);
    } else {
      final DataObject targetObject = new ArrayDataObject(metaData);
      for (final String attributeName : metaData.getAttributeNames()) {
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
      out.write(targetObject);
    }
  }

  public void setAttributeMap(final Map<String, String> attributeMap) {
    this.attributeMap = attributeMap;
  }

  public void setMetaData(final DataObjectMetaData metaData) {
    this.metaData = metaData;
  }

  public void setMetaDataFactory(final DataObjectMetaDataFactory metaDataFactory) {
    this.metaDataFactory = metaDataFactory;
  }

  public void setTypeName(final String typeName) {
    this.typeName = typeName;
  }

  public void setValueMaps(final Map<String, Map<Object, Object>> valueMaps) {
    this.valueMaps = valueMaps;
  }

}
