package com.revolsys.ui.html.serializer.key;

import com.revolsys.util.CaseConverter;

public abstract class AbstractKeySerializer implements KeySerializer {
  private String name;

  private String label;

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
}
