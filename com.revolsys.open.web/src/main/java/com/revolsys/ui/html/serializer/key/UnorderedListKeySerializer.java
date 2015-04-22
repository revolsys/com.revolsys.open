package com.revolsys.ui.html.serializer.key;

import java.util.List;

import com.revolsys.format.xml.XmlWriter;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.JavaBeanUtil;
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
    final Object value = JavaBeanUtil.getProperty(object, getName());
    if (value == null) {
      out.text("-");
    } else if (value instanceof List) {
      final List<?> list = (List<?>)value;
      if (list.isEmpty()) {
        out.text("-");
      } else {
        out.startTag(HtmlUtil.UL);
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
          out.element(HtmlUtil.LI, text);
        }
        out.endTag(HtmlUtil.UL);
      }
    } else {
      out.text(value.toString());
    }
  }
}
