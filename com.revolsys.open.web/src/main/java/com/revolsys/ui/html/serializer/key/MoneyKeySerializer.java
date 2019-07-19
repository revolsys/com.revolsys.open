package com.revolsys.ui.html.serializer.key;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.Property;

/**
 * Serialize a money ammount with the $ sign.
 *
 * @author Paul Austin
 */
public class MoneyKeySerializer extends AbstractKeySerializer {

  /**
   * Convert a BigDecimal amount to a currency string prefixed by the "$" sign.
   *
   * @param amount The BigDecimal amount.
   * @return The currency String
   */
  static String currencyToString(final BigDecimal amount) {
    if (amount != null) {
      return "$" + getCurrency(amount);
    } else {
      return null;
    }
  }

  static BigDecimal getCurrency(final BigDecimal amount) {
    if (amount != null) {
      return amount.setScale(2, RoundingMode.HALF_UP);
    } else {
      return null;
    }
  }

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
  @Override
  public void serialize(final XmlWriter out, final Object object) {
    final BigDecimal value = (BigDecimal)Property.get(object, getName());
    if (value != null) {
      out.text(currencyToString(value));
    } else {
      out.text("-");
    }
  }
}
