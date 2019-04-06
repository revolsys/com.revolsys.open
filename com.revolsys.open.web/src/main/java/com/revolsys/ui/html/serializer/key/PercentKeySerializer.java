package com.revolsys.ui.html.serializer.key;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.Property;

/**
 * Serialize a percent with the % sign.
 *
 * @author Paul Austin
 */
public class PercentKeySerializer extends AbstractKeySerializer {

  /**
   * Convert a BigDecimal decimal percent to a percent string suffixed by the
   * "%" sign with the specified number of decimal places.
   *
   * @param decimalPercent The BigDecimal percent.
   * @param scale The number of decimal places to show.
   * @return The percent String
   */
  static String percentToString(final BigDecimal decimalPercent, final int scale) {
    if (decimalPercent != null) {
      final DecimalFormat format = new DecimalFormat();
      format.setMinimumFractionDigits(0);
      format.setMaximumFractionDigits(scale);
      final String string = format.format(
        decimalPercent.multiply(new BigDecimal(100)).setScale(scale, RoundingMode.HALF_UP)) + "%";

      return string;
    } else {
      return null;
    }
  }

  /** The scale to display the percent as. */
  private int scale = 2;

  public PercentKeySerializer() {
  }

  public PercentKeySerializer(final String name) {
    super(name);
  }

  /**
   * Construct a new PercentKeySerializer.
   */
  public PercentKeySerializer(final String name, final int displayScale) {
    super(name);
    this.scale = displayScale;
  }

  /**
   * Serialize the value to the XML writer.
   *
   * @param out The XML writer to serialize to.
   * @param object The object to get the value from.
   */
  @Override
  public void serialize(final XmlWriter out, final Object object) {
    final BigDecimal value = Property.get(object, getName());
    if (value != null) {
      out.text(percentToString(value, this.scale));
    } else {
      out.text("-");
    }
  }
}
