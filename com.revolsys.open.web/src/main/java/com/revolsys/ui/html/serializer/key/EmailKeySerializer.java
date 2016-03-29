package com.revolsys.ui.html.serializer.key;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.Property;

/**
 * Serialize an email address as a mailto:link.
 *
 * @author Paul Austin
 */
public class EmailKeySerializer extends AbstractKeySerializer {
  public EmailKeySerializer() {
  }

  public EmailKeySerializer(final String name) {
    super(name);
  }

  /**
   * Serialize the value to the XML writer.
   *
   * @param out The XML writer to serialize to.
   * @param object The object to get the value from.
   */
  @Override
  public void serialize(final XmlWriter out, final Object object) {
    final Object email = Property.get(object, getName());
    if (email != null) {
      out.startTag(HtmlElem.A);
      out.attribute(HtmlAttr.HREF, "mailto:" + email);
      out.text(email);
      out.endTag(HtmlElem.A);
    } else {
      out.text("-");
    }
  }
}
