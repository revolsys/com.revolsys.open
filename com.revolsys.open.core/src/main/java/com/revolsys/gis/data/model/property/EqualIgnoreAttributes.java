package com.revolsys.gis.data.model.property;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.revolsys.gis.data.model.AbstractDataObjectMetaDataProperty;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.model.data.equals.DataObjectEquals;

public class EqualIgnoreAttributes extends AbstractDataObjectMetaDataProperty {
  public static final String PROPERTY_NAME = EqualIgnoreAttributes.class.getName()
    + ".propertyName";

  public static EqualIgnoreAttributes getProperty(final DataObject object) {
    final DataObjectMetaData metaData = object.getMetaData();
    return getProperty(metaData);
  }

  public static EqualIgnoreAttributes getProperty(
    final DataObjectMetaData metaData) {
    EqualIgnoreAttributes property = metaData.getProperty(PROPERTY_NAME);
    if (property == null) {
      property = new EqualIgnoreAttributes();
      property.setMetaData(metaData);
    }
    return property;
  }

  private Set<String> attributeNames = new LinkedHashSet<String>();

  public EqualIgnoreAttributes() {
  }

  public EqualIgnoreAttributes(final Collection<String> attributeNames) {
    this.attributeNames.addAll(attributeNames);
  }

  public EqualIgnoreAttributes(final String... attributeNames) {
    this(Arrays.asList(attributeNames));
  }

  public void addAttributeNames(final Collection<String> attributeNames) {
    this.attributeNames.addAll(attributeNames);
  }

  public void addAttributeNames(final String... attributeNames) {
    addAttributeNames(Arrays.asList(attributeNames));
  }

  public Set<String> getAttributeNames() {
    return attributeNames;
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public boolean isAttributeIgnored(final String attributeName) {
    return attributeNames.contains(attributeName);
  }

  public void setAttributeNames(final Collection<String> attributeNames) {
    setAttributeNames(new LinkedHashSet<String>(attributeNames));
  }

  public void setAttributeNames(final Set<String> attributeNames) {
    this.attributeNames = attributeNames;
  }

  public void setAttributeNames(final String... attributeNames) {
    setAttributeNames(Arrays.asList(attributeNames));
  }

  @Override
  public void setMetaData(final DataObjectMetaData metaData) {
    super.setMetaData(metaData);
    if (attributeNames.contains(DataObjectEquals.EXCLUDE_ID)) {
      final String idAttributeName = metaData.getIdAttributeName();
      attributeNames.add(idAttributeName);
    }
    if (attributeNames.contains(DataObjectEquals.EXCLUDE_GEOMETRY)) {
      final String geometryAttributeName = metaData.getGeometryAttributeName();
      attributeNames.add(geometryAttributeName);
    }
  }

  @Override
  public String toString() {
    return "EqualIgnore " + attributeNames;
  }
}
