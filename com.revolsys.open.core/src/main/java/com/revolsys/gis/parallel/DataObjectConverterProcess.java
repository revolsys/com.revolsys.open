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

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionFactory;
import com.revolsys.filter.Filter;
import com.revolsys.gis.converter.FilterDataObjectConverter;
import com.revolsys.gis.converter.SimpleDataObjectConveter;
import com.revolsys.gis.converter.process.CopyValues;
import com.revolsys.gis.io.Statistics;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class DataObjectConverterProcess extends
  BaseInOutProcess<Record, Record> {
  private static final Logger LOG = LoggerFactory.getLogger(DataObjectConverterProcess.class);

  private Converter<Record, Record> defaultConverter;

  private Map<String, Collection<FilterDataObjectConverter>> typeFilterConverterMap = new LinkedHashMap<String, Collection<FilterDataObjectConverter>>();

  private Map<String, Converter<Record, Record>> typeConverterMap = new HashMap<String, Converter<Record, Record>>();

  private RecordDefinitionFactory targetMetaDataFactory;

  private Map<Object, Map<String, Object>> simpleMapping;

  private Statistics statistics = new Statistics("Converted");

  //
  // private Statistics ignoredStatistics = new Statistics("Ignored");

  public void addTypeConverter(final String typePath,
    final Converter<Record, Record> converter) {
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

  protected Record convert(final Record source) {
    int matchCount = 0;
    final RecordDefinition sourceMetaData = source.getMetaData();
    final String sourceTypeName = sourceMetaData.getPath();
    final Collection<FilterDataObjectConverter> converters = typeFilterConverterMap.get(sourceTypeName);
    Record target = null;
    if (converters != null && !converters.isEmpty()) {
      for (final FilterDataObjectConverter filterConverter : converters) {
        final Filter<Record> filter = filterConverter.getFilter();
        if (filter.accept(source)) {
          final Converter<Record, Record> converter = filterConverter.getConverter();
          target = converter.convert(source);
          matchCount++;
        }
      }
      if (matchCount == 1) {
        return target;
      }
    }
    if (matchCount == 0) {
      final Converter<Record, Record> typeConveter = typeConverterMap.get(sourceTypeName);
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
        final Filter<Record> filter = filterConverter.getFilter();
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

  protected Record convertObjectWithNoConverter(final Record source) {
    LOG.error("No converter found for: " + source);
    return null;
  }

  public Converter<Record, Record> getDefaultConverter() {
    return defaultConverter;
  }

  public Map<String, Collection<FilterDataObjectConverter>> getFilterTypeConverterMap() {
    return typeFilterConverterMap;
  }

  public Statistics getStatistics() {
    return statistics;
  }

  public RecordDefinition getTargetMetaData(final String typePath) {
    return targetMetaDataFactory.getRecordDefinition(typePath);
  }

  public RecordDefinitionFactory getTargetMetaDataFactory() {
    return targetMetaDataFactory;
  }

  public Map<String, Converter<Record, Record>> getTypeConverterMap() {
    return typeConverterMap;
  }

  @Override
  protected void postRun(final Channel<Record> in,
    final Channel<Record> out) {
    super.postRun(in, out);
    statistics.disconnect();
    // ignoredStatistics.disconnect();
  }

  @Override
  protected void preRun(final Channel<Record> in,
    final Channel<Record> out) {
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

        final RecordDefinition targetMetaData = getTargetMetaData(targetTypeName);
        final SimpleDataObjectConveter converter = new SimpleDataObjectConveter(
          targetMetaData);
        converter.addProcessor(new CopyValues(attributeMapping));
        addTypeConverter(sourceTypeName, converter);
      }
    }
  }

  @Override
  protected void process(final Channel<Record> in,
    final Channel<Record> out, final Record source) {
    final Record target = convert(source);
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
    final Converter<Record, Record> defaultConverter) {
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
    final RecordDefinitionFactory targetMetaDataFactory) {
    this.targetMetaDataFactory = targetMetaDataFactory;
  }

  public void setTypeConverterMap(
    final Map<String, Converter<Record, Record>> typeConverterMap) {
    this.typeConverterMap = typeConverterMap;
  }

  public void setTypeFilterConverterMap(
    final Map<String, Collection<FilterDataObjectConverter>> typeConverterMap) {
    this.typeFilterConverterMap = typeConverterMap;
  }
}
