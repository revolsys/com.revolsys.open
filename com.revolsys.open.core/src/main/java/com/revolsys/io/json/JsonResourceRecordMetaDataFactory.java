package com.revolsys.io.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionFactory;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.map.MapObjectFactoryRegistry;

public class JsonResourceRecordMetaDataFactory extends
  AbstractObjectWithProperties implements ApplicationContextAware,
  RecordDefinitionFactory {

  private final Map<String, RecordDefinition> metaDataMap = new HashMap<String, RecordDefinition>();

  private String locationPattern;

  private ApplicationContext applicationContext;

  public String getLocationPattern() {
    return locationPattern;
  }

  @Override
  public RecordDefinition getRecordDefinition(final String typePath) {
    return metaDataMap.get(typePath);
  }

  @PostConstruct
  public void init() {
    try {
      for (final Resource resource : applicationContext.getResources(locationPattern)) {
        final RecordDefinition metaData = MapObjectFactoryRegistry.toObject(resource);
        final String name = metaData.getPath();
        metaDataMap.put(name, metaData);
      }
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to get resources for "
        + locationPattern);
    }
  }

  @Override
  public void setApplicationContext(final ApplicationContext applicationContext)
    throws BeansException {
    this.applicationContext = applicationContext;
  }

  public void setLocationPattern(final String locationPattern) {
    this.locationPattern = locationPattern;
  }
}
