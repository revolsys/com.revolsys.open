package com.revolsys.ui.html.serializer.key;

import java.util.Locale;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.util.JavaBeanUtil;

/**
 * Serialize a boolean as the Yes or No strings.
 * 
 * @author Paul Austin
 */
public class BooleanYesNoKeySerializer implements KeySerializer {

  /**
   * Construct a new BooleanYesNoKeySerializer.
   */
  public BooleanYesNoKeySerializer() {
  }

  /**
   * Serialize the value to the XML writer using the settings from the Locale.
   * 
   * @param out The XML writer to serialize to.
   * @param object The object to get the value from.
   * @param key The key of the property on the object to serialize.
   * @param locale The locale.
    */
  public void serialize(final XmlWriter out, final Object object,
    final String key, final Locale locale) {
    Object value = JavaBeanUtil.getProperty(object, key);
    if (Boolean.TRUE.equals(value)) {
      out.text("Yes");
    } else {
      out.text("No");
    }
  }
}
