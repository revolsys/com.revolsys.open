package com.revolsys.gis.ecsv.io;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;

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

  private Map<QName, DataObjectMetaData> metaDataMap = new HashMap<QName, DataObjectMetaData>();

  private String locationPattern;

  private ApplicationContext applicationContext;

  @PostConstruct
  public void init() {
    try {
      for (Resource resource : applicationContext.getResources(locationPattern)) {
        DataObjectMetaData metaData = EcsvIoFactory.readSchema(resource);
        final QName name = metaData.getName();
        metaDataMap.put(name, metaData);
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to get resources for "
        + locationPattern);
    }
  }

  public DataObjectMetaData getMetaData(QName typeName) {
    return metaDataMap.get(typeName);
  }

  public String getLocationPattern() {
    return locationPattern;
  }

  public void setLocationPattern(String locationPattern) {
    this.locationPattern = locationPattern;
  }

  public void setApplicationContext(ApplicationContext applicationContext)
    throws BeansException {
    this.applicationContext = applicationContext;
  }
}
