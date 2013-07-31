package com.revolsys.gis.data.model.property;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.revolsys.gis.data.model.AbstractDataObjectMetaDataProperty;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.attribute.InvokeMethodObjectAttributeProxy;
import com.revolsys.gis.graph.attribute.ObjectAttributeProxy;
import com.revolsys.gis.graph.attribute.PseudoNodeAttribute;
import com.revolsys.gis.model.data.equals.DataObjectEquals;

public class PseudoNodeProperty extends AbstractDataObjectMetaDataProperty {
  protected static final List<String> DEFAULT_EXCLUDE = Arrays.asList(
    DataObjectEquals.EXCLUDE_ID, DataObjectEquals.EXCLUDE_GEOMETRY);

  public static final String PROPERTY_NAME = PseudoNodeProperty.class.getName()
    + ".propertyName";

  public static AbstractDataObjectMetaDataProperty getProperty(
    final DataObject object) {
    final DataObjectMetaData metaData = object.getMetaData();
    return getProperty(metaData);
  }

  public static PseudoNodeProperty getProperty(final DataObjectMetaData metaData) {
    PseudoNodeProperty property = metaData.getProperty(PROPERTY_NAME);
    if (property == null) {
      property = new PseudoNodeProperty();
      property.setMetaData(metaData);
    }
    return property;
  }

  private Set<String> equalExcludeAttributes = new HashSet<String>(
    DEFAULT_EXCLUDE);

  public PseudoNodeProperty() {
  }

  public PseudoNodeAttribute createAttribute(final Node<DataObject> node) {
    return new PseudoNodeAttribute(node, getTypePath(), equalExcludeAttributes);
  }

  public PseudoNodeAttribute getAttribute(final Node<DataObject> node) {
    final String attributeName = PseudoNodeProperty.PROPERTY_NAME;
    if (!node.hasAttribute(attributeName)) {
      final ObjectAttributeProxy<PseudoNodeAttribute, Node<DataObject>> proxy = new InvokeMethodObjectAttributeProxy<PseudoNodeAttribute, Node<DataObject>>(
        this, "createAttribute", Node.class);
      node.setAttribute(attributeName, proxy);
    }
    final PseudoNodeAttribute value = node.getAttribute(attributeName);
    return value;
  }

  public Collection<String> getEqualExcludeAttributes() {
    return equalExcludeAttributes;
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public void setEqualExcludeAttributes(
    final Collection<String> equalExcludeAttributes) {
    if (equalExcludeAttributes == null) {
      this.equalExcludeAttributes.clear();
    } else {
      this.equalExcludeAttributes = new HashSet<String>(equalExcludeAttributes);
    }
    this.equalExcludeAttributes.addAll(DEFAULT_EXCLUDE);
  }

  @Override
  public void setMetaData(final DataObjectMetaData metaData) {
    super.setMetaData(metaData);
  }

  @Override
  public String toString() {
    return "Pseudo Node";
  }
}
