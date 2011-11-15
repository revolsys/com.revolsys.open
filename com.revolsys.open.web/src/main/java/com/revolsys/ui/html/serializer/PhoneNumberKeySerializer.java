package com.revolsys.ui.html.serializer;

import java.util.Locale;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.domain.PhoneNumber;
import com.revolsys.ui.html.serializer.key.KeySerializer;
import com.revolsys.util.JavaBeanUtil;

/**
 * Serialize a url as a hyperlink
 * 
 * @author Paul Austin
 */
public class PhoneNumberKeySerializer implements KeySerializer {

  /**
   * Serialize the value to the XML writer using the settings from the Locale.
   * 
   * @param out The XML writer to serialize to.
   * @param object The object to get the value from.
   * @param key The key of the property on the object to serialize.
   * @param locale The locale.
   */
  public void serialize(
    final XmlWriter out,
    final Object object,
    final String key,
    final Locale locale)
    {
    String phoneNumber = JavaBeanUtil.getProperty(object, key);
    if (phoneNumber != null) {
      if (locale == null) {
        out.text(PhoneNumber.format(phoneNumber));
      } else {
        out.text(PhoneNumber.format(phoneNumber, locale));
      }
    } else {
      out.text("-");
    }
  }
}
