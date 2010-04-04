package com.revolsys.jump.ui.swing.table;

import com.vividsolutions.jump.feature.Feature;

public class FeatureTableRow {
  private int indent = 0;

  private Feature feature;

  private String attributeName;

  private int attributeIndex;

  private Object value;

  public FeatureTableRow(final int indent, final Feature feature,
    final String attributeName, final Object value, final int attributeIndex) {
    this.indent = indent;
    this.feature = feature;
    this.attributeName = attributeName;
    this.value = value;
    this.attributeIndex = attributeIndex;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(final Object value) {
    this.value = value;
  }

  public String getAttributeName() {
    return attributeName;
  }

  public Feature getFeature() {
    return feature;
  }

  public int getIndent() {
    return indent;
  }

  public int getAttributeIndex() {
    return attributeIndex;
  }

}
