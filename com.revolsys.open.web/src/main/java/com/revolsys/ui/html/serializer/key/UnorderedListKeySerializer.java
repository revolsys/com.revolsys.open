package com.revolsys.ui.html.serializer.key;

import java.util.List;

import org.springframework.util.StringUtils;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.util.JavaBeanUtil;

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
  public void serialize(final XmlWriter out, final Object object) {
    final Object value = JavaBeanUtil.getProperty(object, getName());
    if (value == null) {
      out.text("-");
    } else if (value instanceof List) {
      List<?> list = (List<?>)value;
      if (list.isEmpty()) {
        out.text("-");
      } else {
        out.startTag(HtmlUtil.UL);
        for (Object item : list) {
          String text;
          if (item == null) {
            text = "-";
          } else {
            text = object.toString();
          }
          if (!StringUtils.hasText(text)) {
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
