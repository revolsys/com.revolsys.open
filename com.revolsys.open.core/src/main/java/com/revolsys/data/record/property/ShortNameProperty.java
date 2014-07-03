package com.revolsys.data.record.property;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;

public class ShortNameProperty extends AbstractRecordDefinitionProperty {
  static final String PROPERTY_NAME = "http://revolsys.com/gis/shortName";

  public static ShortNameProperty getProperty(final Record object) {
    final RecordDefinition metaData = object.getMetaData();
    return getProperty(metaData);
  }

  public static ShortNameProperty getProperty(final RecordDefinition metaData) {
    return metaData.getProperty(PROPERTY_NAME);
  }

  public static String getShortName(final Record object) {
    final ShortNameProperty property = getProperty(object);
    if (property == null) {
      return null;
    } else {
      return property.getShortName();
    }
  }

  public static String getShortName(final RecordDefinition metaData) {
    final ShortNameProperty property = getProperty(metaData);
    if (property == null) {
      return null;
    } else {
      return property.getShortName();
    }
  }

  private boolean useForSequence = true;

  private String shortName;

  public ShortNameProperty() {
  }

  public ShortNameProperty(final String shortName) {
    this.shortName = shortName;
  }

  @Override
  public ShortNameProperty clone() {
    return new ShortNameProperty(shortName);
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public String getShortName() {
    return shortName;
  }

  public boolean isUseForSequence() {
    return useForSequence;
  }

  public void setShortName(final String shortName) {
    this.shortName = shortName;
  }

  public void setUseForSequence(final boolean useForSequence) {
    this.useForSequence = useForSequence;
  }

  @Override
  public String toString() {
    return shortName;
  }
}
