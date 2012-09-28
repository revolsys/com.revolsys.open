package com.revolsys.ui.html.view;

import java.util.Collection;

import javax.xml.namespace.QName;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.io.xml.XmlWriter;

public class ListElement extends Element {
  private Collection<? extends Object> objects;

  private QName parentTag;

  private QName elementTag;

  public ListElement(QName parentTag, QName elementTag,
    Collection<? extends Object> objects) {
    this.parentTag = parentTag;
    this.elementTag = elementTag;
    this.objects = objects;
  }

  @Override
  public void serializeElement(XmlWriter out) {
    if (parentTag != null) {
      out.startTag(parentTag);

    }
    if (elementTag == null) {
      for (Object value : objects) {
        out.text(StringConverterRegistry.toString(value));
      }
    } else {
      for (Object value : objects) {
        out.element(elementTag, StringConverterRegistry.toString(value));
      }

    }
    if (parentTag != null) {
      out.endTag(parentTag);
    }
  }
}
