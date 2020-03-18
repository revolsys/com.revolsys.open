package com.revolsys.ui.html.serializer;

import javax.xml.namespace.QName;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.serializer.key.AbstractKeySerializer;
import com.revolsys.util.Property;

public class ElementKeySerializer extends AbstractKeySerializer {

  private final QName element;

  public ElementKeySerializer(final QName element, final String name) {
    this(element, name, null);
  }

  public ElementKeySerializer(final QName element, final String name, final String label) {
    super(name);
    this.element = element;
    if (label != null) {
      setLabel(label);
    }
  }

  /**
   * Serialize the value to the XML writer.
   *
   * @param out The XML writer to serialize to.
   * @param object The object to get the value from.
   */
  @Override
  public void serialize(final XmlWriter out, final Object object) {
    out.startTag(this.element);
    final Object value = Property.get(object, getName());
    if (value == null) {
      out.text("-");
    } else {
      out.text(value);
    }
    out.endTag(this.element);
  }
}
