package com.revolsys.gis.converter.process;

import org.springframework.core.convert.converter.Converter;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectUtil;

public class SourceValueConverter extends
  AbstractSourceToTargetProcess<DataObject, DataObject> {
  private Converter<DataObject, ? extends Object> sourceValueConverter;

  private String targetAttributeName;

  public SourceValueConverter() {
  }

  public SourceValueConverter(final String targetAttributeName,
    final Converter<DataObject, ? extends Object> sourceValueConverter) {
    this.targetAttributeName = targetAttributeName;
    this.sourceValueConverter = sourceValueConverter;
  }

  public void process(final DataObject source, final DataObject target) {
    final Object value = sourceValueConverter.convert(source);
    if (value != null && (!(value instanceof String) || !("".equals(value)))) {
      DataObjectUtil.setAttributeValue(target, targetAttributeName, value);
    }
  }

  @Override
  public String toString() {
    return "set " + targetAttributeName + "=" + sourceValueConverter;
  }
}
