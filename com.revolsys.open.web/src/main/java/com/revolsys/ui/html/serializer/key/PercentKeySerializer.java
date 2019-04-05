package com.revolsys.ui.html.serializer.key;

import java.math.BigDecimal;

import org.jeometry.common.math.MathUtil;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.Property;

/**
 * Serialize a percent with the % sign.
 *
 * @author Paul Austin
 */
public class PercentKeySerializer extends AbstractKeySerializer {
  /** The scale to display the percent as. */
  private int scale = MathUtil.PERCENT_SCALE - 2;

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
      out.text(MathUtil.percentToString(value, this.scale));
    } else {
      out.text("-");
    }
  }
}
