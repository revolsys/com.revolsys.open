package com.revolsys.ui.html.serializer.key;

import java.math.BigDecimal;
import java.util.Locale;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.MathUtil;

/**
 * Serialize a percent with the % sign.
 * 
 * @author Paul Austin
 */
public class PercentKeySerializer implements KeySerializer {
  /** The scale to display the percent as. */
  private int scale = MathUtil.PERCENT_SCALE - 2;

  /**
   * Construct a new PercentKeySerializer.
   */
  public PercentKeySerializer() {
  }

  /**
   * Construct a new PercentKeySerializer.
   * 
   * @param displayScale The scale to display the percent as.
   */
  public PercentKeySerializer(final int displayScale) {
    this.scale = displayScale;
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
    BigDecimal value = JavaBeanUtil.getProperty(object, key);
    if (value != null) {
      out.text(MathUtil.percentToString(value, scale));
    } else {
      out.text("-");
    }
  }
}
