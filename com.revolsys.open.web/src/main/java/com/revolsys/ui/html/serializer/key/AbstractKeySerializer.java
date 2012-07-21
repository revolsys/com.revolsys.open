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

  public AbstractKeySerializer() {
  }

  public AbstractKeySerializer(final String name) {
    setName(name);
  }

  public AbstractKeySerializer(final String name, final String label) {
    setName(name);
    setLabel(label);
  }

  public String getLabel() {
    return label;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getKey() {
    if (StringUtils.hasText(key)) {
      return key;
    } else {
      return name;
    }
  }

  public String getName() {
    return name;
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

  @Override
  public String toString(Object object) {
    StringWriter out = new StringWriter();
    XmlWriter xmlOut = new XmlWriter(out);
    serialize(xmlOut, object);
    xmlOut.flush();
    xmlOut.close();
    return out.toString();

  }
}
