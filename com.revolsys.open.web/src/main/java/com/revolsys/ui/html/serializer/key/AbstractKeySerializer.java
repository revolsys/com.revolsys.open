package com.revolsys.ui.html.serializer.key;

import java.io.StringWriter;

import org.springframework.util.StringUtils;

import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.util.CaseConverter;

public abstract class AbstractKeySerializer extends
  AbstractObjectWithProperties implements KeySerializer {
  private String name;

  private String label;

  private String key;

  private String width;

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
    if (StringUtils.hasText(key)) {
      return key;
    } else {
      return name;
    }
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public String getName() {
    return name;
  }

  public String getWidth() {
    return width;
  }

  public void setKey(final String key) {
    this.key = key;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  public void setName(final String name) {
    this.name = name;
    if (label == null && name != null) {
      label = CaseConverter.toCapitalizedWords(name);
    }
  }

  public void setWidth(final String width) {
    this.width = width;
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
