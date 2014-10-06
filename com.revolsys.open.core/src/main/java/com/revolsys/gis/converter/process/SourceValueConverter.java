package com.revolsys.gis.converter.process;

import org.springframework.core.convert.converter.Converter;

import com.revolsys.data.record.Record;

public class SourceValueConverter extends
  AbstractSourceToTargetProcess<Record, Record> {
  private Converter<Record, ? extends Object> sourceValueConverter;

  private String targetFieldName;

  public SourceValueConverter() {
  }

  public SourceValueConverter(final String targetFieldName,
    final Converter<Record, ? extends Object> sourceValueConverter) {
    this.targetFieldName = targetFieldName;
    this.sourceValueConverter = sourceValueConverter;
  }

  @Override
  public void process(final Record source, final Record target) {
    final Object value = sourceValueConverter.convert(source);
    if (value != null && (!(value instanceof String) || !("".equals(value)))) {
      target.setValueByPath(targetFieldName, value);
    }
  }

  @Override
  public String toString() {
    return "set " + targetFieldName + "=" + sourceValueConverter;
  }
}
