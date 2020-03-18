package com.revolsys.ui.html.serializer.key;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.domain.PhoneNumber;
import com.revolsys.util.Property;

/**
 * Serialize a url as a hyperlink
 *
 * @author Paul Austin
 */
public class PhoneNumberKeySerializer extends AbstractKeySerializer {
  public PhoneNumberKeySerializer() {
  }

  public PhoneNumberKeySerializer(final String name) {
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
    final String phoneNumber = Property.get(object, getName());
    if (phoneNumber != null) {
      out.text(PhoneNumber.format(phoneNumber));
    } else {
      out.text("-");
    }
  }
}
