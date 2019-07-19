package com.revolsys.ui.html.serializer.key;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.JavaBeanUtil;

/**
 * Serialize a boolean as the Yes or No strings.
 *
 * @author Paul Austin
 */
public class BooleanYesNoKeySerializer extends AbstractKeySerializer {
  public BooleanYesNoKeySerializer() {
    setProperty("searchable", false);
  }

  /**
   * Construct a new BooleanYesNoKeySerializer.
   */
  public BooleanYesNoKeySerializer(final String name) {
    super(name);
    setProperty("searchable", false);
  }

  /**
   * Serialize the value to the XML writer.
   *
   * @param out The XML writer to serialize to.
   * @param object The object to get the value from.
   */
  @Override
  public void serialize(final XmlWriter out, final Object object) {
    final Object value = JavaBeanUtil.getBooleanValue(object, getName());
    if (Boolean.TRUE.equals(value)) {
      out.text("Yes");
    } else {
      out.text("No");
    }
  }
}
