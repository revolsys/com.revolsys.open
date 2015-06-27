package com.revolsys.data.record.property;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.revolsys.data.equals.RecordEquals;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.attribute.InvokeMethodObjectPropertyProxy;
import com.revolsys.gis.graph.attribute.PseudoNodeAttribute;
import com.revolsys.properties.ObjectPropertyProxy;

public class PseudoNodeProperty extends AbstractRecordDefinitionProperty {
  protected static final List<String> DEFAULT_EXCLUDE = Arrays.asList(RecordEquals.EXCLUDE_ID,
    RecordEquals.EXCLUDE_GEOMETRY);

  public static final String PROPERTY_NAME = PseudoNodeProperty.class.getName() + ".propertyName";

  public static AbstractRecordDefinitionProperty getProperty(final Record object) {
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    return getProperty(recordDefinition);
  }

  public static PseudoNodeProperty getProperty(final RecordDefinition recordDefinition) {
    PseudoNodeProperty property = recordDefinition.getProperty(PROPERTY_NAME);
    if (property == null) {
      property = new PseudoNodeProperty();
      property.setRecordDefinition(recordDefinition);
    }
    return property;
  }

  private Set<String> equalExcludeAttributes = new HashSet<String>(DEFAULT_EXCLUDE);

  public PseudoNodeProperty() {
  }

  public PseudoNodeAttribute createProperty(final Node<Record> node) {
    return new PseudoNodeAttribute(node, getTypePath(), this.equalExcludeAttributes);
  }

  public Collection<String> getEqualExcludeAttributes() {
    return this.equalExcludeAttributes;
  }

  public PseudoNodeAttribute getProperty(final Node<Record> node) {
    final String fieldName = PseudoNodeProperty.PROPERTY_NAME;
    if (!node.hasProperty(fieldName)) {
      final ObjectPropertyProxy<PseudoNodeAttribute, Node<Record>> proxy = new InvokeMethodObjectPropertyProxy<>(
        this, "createProperty", Node.class);
      node.setProperty(fieldName, proxy);
    }
    final PseudoNodeAttribute value = node.getProperty(fieldName);
    return value;
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public void setEqualExcludeAttributes(final Collection<String> equalExcludeAttributes) {
    if (equalExcludeAttributes == null) {
      this.equalExcludeAttributes.clear();
    } else {
      this.equalExcludeAttributes = new HashSet<String>(equalExcludeAttributes);
    }
    this.equalExcludeAttributes.addAll(DEFAULT_EXCLUDE);
  }

  @Override
  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    super.setRecordDefinition(recordDefinition);
  }

  @Override
  public String toString() {
    return "Pseudo Node";
  }
}
