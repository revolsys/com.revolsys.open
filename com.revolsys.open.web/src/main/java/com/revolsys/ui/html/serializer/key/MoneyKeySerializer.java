package com.revolsys.ui.html.serializer.key;

import java.math.BigDecimal;
import java.util.Locale;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.MathUtil;

/**
 * Serialize a money ammount with the $ sign.
 * 
 * @author Paul Austin
 */
public class MoneyKeySerializer implements KeySerializer {

  /**
   * Construct a new MoneyKeySerializer.
   */
  public MoneyKeySerializer() {
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
    BigDecimal value = (BigDecimal)JavaBeanUtil.getProperty(object, key);
    if (value != null) {
      out.text(MathUtil.currencyToString(value));
    } else {
      out.text("-");
    }
  }
}
