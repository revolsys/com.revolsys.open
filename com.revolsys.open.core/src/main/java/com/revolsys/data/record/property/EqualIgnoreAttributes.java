package com.revolsys.data.record.property;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.revolsys.data.equals.RecordEquals;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;

public class EqualIgnoreAttributes extends AbstractRecordDefinitionProperty {
  public static EqualIgnoreAttributes getProperty(final Record object) {
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    return getProperty(recordDefinition);
  }

  public static EqualIgnoreAttributes getProperty(
    final RecordDefinition recordDefinition) {
    EqualIgnoreAttributes property = recordDefinition.getProperty(PROPERTY_NAME);
    if (property == null) {
      property = new EqualIgnoreAttributes();
      property.setRecordDefinition(recordDefinition);
    }
    return property;
  }

  public static final String PROPERTY_NAME = EqualIgnoreAttributes.class.getName()
    + ".propertyName";

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
    return this.attributeNames;
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public boolean isAttributeIgnored(final String attributeName) {
    return this.attributeNames.contains(attributeName);
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
  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    super.setRecordDefinition(recordDefinition);
    if (this.attributeNames.contains(RecordEquals.EXCLUDE_ID)) {
      final String idAttributeName = recordDefinition.getIdAttributeName();
      this.attributeNames.add(idAttributeName);
    }
    if (this.attributeNames.contains(RecordEquals.EXCLUDE_GEOMETRY)) {
      final String geometryAttributeName = recordDefinition.getGeometryAttributeName();
      this.attributeNames.add(geometryAttributeName);
    }
  }

  @Override
  public String toString() {
    return "EqualIgnore " + this.attributeNames;
  }
}
