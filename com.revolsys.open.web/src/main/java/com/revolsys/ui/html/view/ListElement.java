package com.revolsys.ui.html.view;

import java.util.Collection;

import javax.xml.namespace.QName;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.io.xml.XmlWriter;

public class ListElement extends Element {
  private final Collection<? extends Object> objects;

  private final QName parentTag;

  private final QName elementTag;

  public ListElement(final QName parentTag, final QName elementTag,
    final Collection<? extends Object> objects) {
    this.parentTag = parentTag;
    this.elementTag = elementTag;
    this.objects = objects;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    if (this.parentTag != null) {
      out.startTag(this.parentTag);

    }
    if (this.elementTag == null) {
      for (final Object value : this.objects) {
        out.text(StringConverterRegistry.toString(value));
      }
    } else {
      for (final Object value : this.objects) {
        out.element(this.elementTag, StringConverterRegistry.toString(value));
      }

    }
    if (this.parentTag != null) {
      out.endTag(this.parentTag);
    }
  }
}
