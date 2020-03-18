package com.revolsys.ui.html.serializer.key;

import java.io.StringWriter;

import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.Property;

public abstract class AbstractKeySerializer extends BaseObjectWithProperties
  implements KeySerializer {
  private String key;

  private String label;

  private String name;

  private String width;

  private String sortFieldName;

  public AbstractKeySerializer() {
  }

  public AbstractKeySerializer(final String name) {
    setName(name);
  }

  public AbstractKeySerializer(final String name, final String label) {
    setName(name);
    setLabel(label);
  }

  @Override
  public String getKey() {
    if (Property.hasValue(this.key)) {
      return this.key;
    } else {
      return this.name;
    }
  }

  @Override
  public String getLabel() {
    return this.label;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getSortFieldName() {
    if (Property.hasValue(this.sortFieldName)) {
      return this.sortFieldName;
    } else {
      return getKey();
    }
  }

  @Override
  public String getWidth() {
    return this.width;
  }

  @Override
  public AbstractKeySerializer setKey(final String key) {
    this.key = key;
    return this;
  }

  @Override
  public AbstractKeySerializer setLabel(final String label) {
    this.label = label;
    return this;
  }

  public AbstractKeySerializer setName(final String name) {
    this.name = name;
    if (this.label == null && name != null) {
      this.label = CaseConverter.toCapitalizedWords(name);
    }
    return this;
  }

  public AbstractKeySerializer setSortFieldName(final String sortFieldName) {
    this.sortFieldName = sortFieldName;
    return this;
  }

  public AbstractKeySerializer setWidth(final String width) {
    this.width = width;
    return this;
  }

  @Override
  public String toString() {
    if (Property.hasValue(this.name)) {
      return this.name;
    } else if (Property.hasValue(this.key)) {
      return this.key;
    } else {
      return super.toString();
    }
  }

  @Override
  public String toString(final Object object) {
    final StringWriter out = new StringWriter();
    final XmlWriter xmlOut = new XmlWriter(out);
    serialize(xmlOut, object);
    xmlOut.flush();
    xmlOut.close();
    return out.toString();
  }
}
