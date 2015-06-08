package com.revolsys.format.json;

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

public class JsonResourceRecordDefinitionFactory extends AbstractObjectWithProperties implements
  ApplicationContextAware, RecordDefinitionFactory {

  private final Map<String, RecordDefinition> recordDefinitionMap = new HashMap<String, RecordDefinition>();

  private String locationPattern;

  private ApplicationContext applicationContext;

  public String getLocationPattern() {
    return this.locationPattern;
  }

  @Override
  public RecordDefinition getRecordDefinition(final String typePath) {
    return this.recordDefinitionMap.get(typePath);
  }

  @PostConstruct
  public void init() {
    try {
      for (final Resource resource : this.applicationContext.getResources(this.locationPattern)) {
        final RecordDefinition recordDefinition = MapObjectFactoryRegistry.toObject(resource);
        final String name = recordDefinition.getPath();
        this.recordDefinitionMap.put(name, recordDefinition);
      }
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to get resources for " + this.locationPattern);
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
