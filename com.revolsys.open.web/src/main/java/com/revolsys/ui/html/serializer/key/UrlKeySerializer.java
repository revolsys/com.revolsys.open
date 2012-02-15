package com.revolsys.ui.html.serializer.key;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.util.JavaBeanUtil;

/**
 * Serialize a url as a hyperlink
 * 
 * @author Paul Austin
 */
public class UrlKeySerializer extends AbstractKeySerializer {

  public UrlKeySerializer(final String name) {
    super(name);
  }

  /**
   * Serialize the value to the XML writer.
   * 
   * @param out The XML writer to serialize to.
   * @param object The object to get the value from.
   */
  public void serialize(final XmlWriter out, final Object object) {
    final Object url = JavaBeanUtil.getProperty(object, getName());
    if (url != null) {
      out.startTag(HtmlUtil.A);
      out.attribute(HtmlUtil.ATTR_HREF, url);
      out.text(url);
      out.endTag(HtmlUtil.A);
    } else {
      out.text("-");
    }
  }
}
