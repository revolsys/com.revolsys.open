package com.revolsys.ui.html.serializer.key;

import java.math.BigDecimal;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.MathUtil;

/**
 * Serialize a money ammount with the $ sign.
 * 
 * @author Paul Austin
 */
public class MoneyKeySerializer extends AbstractKeySerializer {
  public MoneyKeySerializer() {
  }

  /**
   * Construct a new MoneyKeySerializer.
   */
  public MoneyKeySerializer(final String name) {
    super(name);
  }

  /**
   * Serialize the value to the XML writer.
   * 
   * @param out The XML writer to serialize to.
   * @param object The object to get the value from.
   */
  public void serialize(final XmlWriter out, final Object object) {
    final BigDecimal value = (BigDecimal)JavaBeanUtil.getProperty(object,
      getName());
    if (value != null) {
      out.text(MathUtil.currencyToString(value));
    } else {
      out.text("-");
    }
  }
}
