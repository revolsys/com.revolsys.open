package com.revolsys.format.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionFactory;
import com.revolsys.spring.resource.Resource;

public class JsonResourceRecordDefinitionFactory extends BaseObjectWithProperties
  implements ApplicationContextAware, RecordDefinitionFactory {

  private ApplicationContext applicationContext;

  private String locationPattern;

  private final Map<String, RecordDefinition> recordDefinitionMap = new HashMap<String, RecordDefinition>();

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
      for (final org.springframework.core.io.Resource resource : this.applicationContext
        .getResources(this.locationPattern)) {
        final RecordDefinition recordDefinition = MapObjectFactoryRegistry
          .toObject(Resource.getResource(resource));
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
