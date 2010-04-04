package com.revolsys.ui.html.serializer.key;

import java.io.IOException;
import java.util.Locale;

import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.xml.io.XmlWriter;

/**
 * Serialize a url as a hyperlink
 * 
 * @author Paul Austin
 */
public class UrlKeySerializer implements KeySerializer {

  /**
   * Serialize the value to the XML writer using the settings from the Locale.
   * 
   * @param out The XML writer to serialize to.
   * @param object The object to get the value from.
   * @param key The key of the property on the object to serialize.
   * @param locale The locale.
   * @throws IOException If there was an I/O error serializing the value.
   */
  public void serialize(final XmlWriter out, final Object object,
    final String key, final Locale locale) throws IOException {
    Object url = JavaBeanUtil.getProperty(object, key);
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
