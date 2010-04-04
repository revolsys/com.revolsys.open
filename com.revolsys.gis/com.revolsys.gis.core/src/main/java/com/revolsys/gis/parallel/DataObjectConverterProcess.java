package com.revolsys.gis.parallel;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.converter.FilterDataObjectConverter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class DataObjectConverterProcess extends BaseInOutProcess<DataObject> {
  private static final Logger LOG = LoggerFactory.getLogger(DataObjectConverterProcess.class);

  private Converter<DataObject, DataObject> defaultConverter;

  private Map<QName, Collection<FilterDataObjectConverter>> typeConverterMap = new LinkedHashMap<QName, Collection<FilterDataObjectConverter>>();

  public Converter<DataObject, DataObject> getDefaultConverter() {
    return defaultConverter;
  }

  public Map<QName, Collection<FilterDataObjectConverter>> getTypeConverterMap() {
    return typeConverterMap;
  }

  @Override
  protected void process(
    final Channel<DataObject> in,
    final Channel<DataObject> out,
    final DataObject sourceObject) {
    if (sourceObject != null) {
      final DataObjectMetaData sourceMetaData = sourceObject.getMetaData();
      final QName sourceTypeName = sourceMetaData.getName();
      final Collection<FilterDataObjectConverter> converters = typeConverterMap.get(sourceTypeName);
      if (converters != null && !converters.isEmpty()) {
        DataObject targetObject = null;
        int matchCount = 0;
        for (final FilterDataObjectConverter filterConverter : converters) {
          final Filter<DataObject> filter = filterConverter.getFilter();
          if (filter.accept(sourceObject)) {
            final Converter<DataObject, DataObject> converter = filterConverter.getConverter();
            targetObject = converter.convert(sourceObject);
            matchCount++;
          }
        }
        if (matchCount == 1) {
          out.write(targetObject);
        } else if (matchCount == 0) {
          if (defaultConverter == null) {
            LOG.error("No converter found for: " + sourceObject);
            for (final FilterDataObjectConverter filterConverter : converters) {
              final Filter<DataObject> filter = filterConverter.getFilter();
              if (filter.accept(sourceObject)) {
                final Converter<DataObject, DataObject> converter = filterConverter.getConverter();

              }
            }
          } else {
            targetObject = defaultConverter.convert(sourceObject);
            out.write(targetObject);
          }
        } else {
          final StringBuffer sb = new StringBuffer(
            "Multiple converers found: \n  ");
          for (final FilterDataObjectConverter filterConverter : converters) {
            final Filter<DataObject> filter = filterConverter.getFilter();
            if (filter.accept(sourceObject)) {
              sb.append(filter.toString());
              sb.append("\n  ");
            }
          }
          sb.append(sourceObject);
        }
      } else {
        // LOG.error("No converter found for: " + sourceObject);
      }
    }
  }

  public void setDefaultConverter(
    final Converter<DataObject, DataObject> defaultConverter) {
    this.defaultConverter = defaultConverter;
  }

  public void setTypeConverterMap(
    final Map<QName, Collection<FilterDataObjectConverter>> typeConverterMap) {
    this.typeConverterMap = typeConverterMap;
  }

}
