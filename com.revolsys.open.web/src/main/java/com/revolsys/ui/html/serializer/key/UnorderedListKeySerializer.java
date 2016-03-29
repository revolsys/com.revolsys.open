package com.revolsys.ui.html.serializer.key;

import java.util.List;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.Property;

public class UnorderedListKeySerializer extends AbstractKeySerializer {
  public UnorderedListKeySerializer() {
  }

  public UnorderedListKeySerializer(final String name) {
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
    final Object value = Property.get(object, getName());
    if (value == null) {
      out.text("-");
    } else if (value instanceof List) {
      final List<?> list = (List<?>)value;
      if (list.isEmpty()) {
        out.text("-");
      } else {
        out.startTag(HtmlElem.UL);
        for (final Object item : list) {
          String text;
          if (item == null) {
            text = "-";
          } else {
            text = object.toString();
          }
          if (!Property.hasValue(text)) {
            text = "-";
          }
          out.element(HtmlElem.LI, text);
        }
        out.endTag(HtmlElem.UL);
      }
    } else {
      out.text(value.toString());
    }
  }
}
