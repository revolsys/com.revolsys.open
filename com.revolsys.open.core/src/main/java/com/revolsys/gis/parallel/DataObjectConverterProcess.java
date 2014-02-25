package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.converter.FilterDataObjectConverter;
import com.revolsys.gis.converter.SimpleDataObjectConveter;
import com.revolsys.gis.converter.process.CopyValues;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataFactory;
import com.revolsys.gis.io.Statistics;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class DataObjectConverterProcess extends
  BaseInOutProcess<DataObject, DataObject> {
  private static final Logger LOG = LoggerFactory.getLogger(DataObjectConverterProcess.class);

  private Converter<DataObject, DataObject> defaultConverter;

  private Map<String, Collection<FilterDataObjectConverter>> typeFilterConverterMap = new LinkedHashMap<String, Collection<FilterDataObjectConverter>>();

  private Map<String, Converter<DataObject, DataObject>> typeConverterMap = new HashMap<String, Converter<DataObject, DataObject>>();

  private DataObjectMetaDataFactory targetMetaDataFactory;

  private Map<Object, Map<String, Object>> simpleMapping;

  private Statistics statistics = new Statistics("Converted");

  //
  // private Statistics ignoredStatistics = new Statistics("Ignored");

  public void addTypeConverter(final String typePath,
    final Converter<DataObject, DataObject> converter) {
    typeConverterMap.put(typePath, converter);
  }

  public void addTypeFilterConverter(final String typePath,
    final FilterDataObjectConverter filterConverter) {

    Collection<FilterDataObjectConverter> converters = typeFilterConverterMap.get(typePath);
    if (converters == null) {
      converters = new ArrayList<FilterDataObjectConverter>();
      typeFilterConverterMap.put(typePath, converters);
    }
    converters.add(filterConverter);
  }

  protected DataObject convert(final DataObject source) {
    int matchCount = 0;
    final DataObjectMetaData sourceMetaData = source.getMetaData();
    final String sourceTypeName = sourceMetaData.getPath();
    final Collection<FilterDataObjectConverter> converters = typeFilterConverterMap.get(sourceTypeName);
    DataObject target = null;
    if (converters != null && !converters.isEmpty()) {
      for (final FilterDataObjectConverter filterConverter : converters) {
        final Filter<DataObject> filter = filterConverter.getFilter();
        if (filter.accept(source)) {
          final Converter<DataObject, DataObject> converter = filterConverter.getConverter();
          target = converter.convert(source);
          matchCount++;
        }
      }
      if (matchCount == 1) {
        return target;
      }
    }
    if (matchCount == 0) {
      final Converter<DataObject, DataObject> typeConveter = typeConverterMap.get(sourceTypeName);
      if (typeConveter != null) {
        target = typeConveter.convert(source);
        return target;
      } else if (defaultConverter == null) {
        return convertObjectWithNoConverter(source);
      } else {
        return defaultConverter.convert(source);

      }
    } else {
      final StringBuffer sb = new StringBuffer("Multiple conveters found: \n  ");
      for (final FilterDataObjectConverter filterConverter : converters) {
        final Filter<DataObject> filter = filterConverter.getFilter();
        if (filter.accept(source)) {
          sb.append(filter.toString());
          sb.append("\n  ");
        }
      }
      sb.append(source);
      LOG.error(sb.toString());
      return null;
    }
  }

  protected DataObject convertObjectWithNoConverter(final DataObject source) {
    LOG.error("No converter found for: " + source);
    return null;
  }

  public Converter<DataObject, DataObject> getDefaultConverter() {
    return defaultConverter;
  }

  public Map<String, Collection<FilterDataObjectConverter>> getFilterTypeConverterMap() {
    return typeFilterConverterMap;
  }

  public Statistics getStatistics() {
    return statistics;
  }

  public DataObjectMetaData getTargetMetaData(final String typePath) {
    return targetMetaDataFactory.getMetaData(typePath);
  }

  public DataObjectMetaDataFactory getTargetMetaDataFactory() {
    return targetMetaDataFactory;
  }

  public Map<String, Converter<DataObject, DataObject>> getTypeConverterMap() {
    return typeConverterMap;
  }

  @Override
  protected void postRun(final Channel<DataObject> in,
    final Channel<DataObject> out) {
    super.postRun(in, out);
    statistics.disconnect();
    // ignoredStatistics.disconnect();
  }

  @Override
  protected void preRun(final Channel<DataObject> in,
    final Channel<DataObject> out) {
    statistics.connect();
    // ignoredStatistics.connect();
    if (simpleMapping != null) {
      for (final Entry<Object, Map<String, Object>> entry : simpleMapping.entrySet()) {
        final Object key = entry.getKey();
        String sourceTypeName;
        if (key instanceof String) {
          sourceTypeName = (String)key;
        } else {
          sourceTypeName = String.valueOf(key.toString());
        }
        final Map<String, Object> map = entry.getValue();
        final Object targetName = map.get("typePath");
        String targetTypeName;
        if (key instanceof String) {
          targetTypeName = (String)targetName;
        } else {
          targetTypeName = String.valueOf(targetName.toString());
        }
        @SuppressWarnings("unchecked")
        final Map<String, String> attributeMapping = (Map<String, String>)map.get("attributeMapping");

        final DataObjectMetaData targetMetaData = getTargetMetaData(targetTypeName);
        final SimpleDataObjectConveter converter = new SimpleDataObjectConveter(
          targetMetaData);
        converter.addProcessor(new CopyValues(attributeMapping));
        addTypeConverter(sourceTypeName, converter);
      }
    }
  }

  @Override
  protected void process(final Channel<DataObject> in,
    final Channel<DataObject> out, final DataObject source) {
    final DataObject target = convert(source);
    if (target == null) {
      // ignoredStatistics.add(source);
    } else {
      out.write(target);
      if (source != target) {
        statistics.add(target);
      }
    }
  }

  public void setDefaultConverter(
    final Converter<DataObject, DataObject> defaultConverter) {
    this.defaultConverter = defaultConverter;
  }

  public void setSimpleMapping(
    final Map<Object, Map<String, Object>> simpleMapping) {
    this.simpleMapping = simpleMapping;
  }

  public void setStatistics(final Statistics statistics) {
    if (this.statistics != statistics) {
      this.statistics = statistics;
      statistics.connect();
    }
  }

  public void setTargetMetaDataFactory(
    final DataObjectMetaDataFactory targetMetaDataFactory) {
    this.targetMetaDataFactory = targetMetaDataFactory;
  }

  public void setTypeConverterMap(
    final Map<String, Converter<DataObject, DataObject>> typeConverterMap) {
    this.typeConverterMap = typeConverterMap;
  }

  public void setTypeFilterConverterMap(
    final Map<String, Collection<FilterDataObjectConverter>> typeConverterMap) {
    this.typeFilterConverterMap = typeConverterMap;
  }
}
