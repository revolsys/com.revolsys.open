package com.revolsys.io.ecsv;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataFactory;
import com.revolsys.io.AbstractObjectWithProperties;

public class EcsvResourceDataObjectMetaDataFactory extends
  AbstractObjectWithProperties implements ApplicationContextAware,
  DataObjectMetaDataFactory {

  private final Map<String, DataObjectMetaData> metaDataMap = new HashMap<String, DataObjectMetaData>();

  private String locationPattern;

  private ApplicationContext applicationContext;

  public String getLocationPattern() {
    return locationPattern;
  }

  public DataObjectMetaData getMetaData(final String typePath) {
    return metaDataMap.get(typePath);
  }

  @PostConstruct
  public void init() {
    try {
      for (final Resource resource : applicationContext.getResources(locationPattern)) {
        final DataObjectMetaData metaData = EcsvIoFactory.readSchema(resource);
        final String name = metaData.getPath();
        metaDataMap.put(name, metaData);
      }
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to get resources for "
        + locationPattern);
    }
  }

  public void setApplicationContext(final ApplicationContext applicationContext)
    throws BeansException {
    this.applicationContext = applicationContext;
  }

  public void setLocationPattern(final String locationPattern) {
    this.locationPattern = locationPattern;
  }
}
