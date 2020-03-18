package com.revolsys.ui.html.serializer.key;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.Property;

/**
 * Serialize a url as a hyperlink
 *
 * @author Paul Austin
 */
public class UrlKeySerializer extends AbstractKeySerializer {
  public UrlKeySerializer() {
  }

  public UrlKeySerializer(final String name) {
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
    final Object url = Property.get(object, getName());
    if (url != null) {
      out.startTag(HtmlElem.A);
      out.attribute(HtmlAttr.HREF, url);
      out.text(url);
      out.endTag(HtmlElem.A);
    } else {
      out.text("-");
    }
  }
}
