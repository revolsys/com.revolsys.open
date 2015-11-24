package com.revolsys.ui.html.serializer.key;

import java.util.Collection;

import com.revolsys.datatype.DataTypes;
import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.JavaBeanUtil;

public class BulletListKeySerializer extends AbstractKeySerializer {

  public BulletListKeySerializer() {
    setProperty("sortable", false);
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
    final Object value = JavaBeanUtil.getProperty(object, getKey());
    if (value == null) {
      out.text("-");
    } else {
      if (value instanceof Collection) {
        final Collection<?> collection = (Collection<?>)value;
        if (collection.isEmpty()) {
          out.text("-");
        } else {
          out.startTag(HtmlUtil.UL);
          for (final Object item : collection) {
            out.element(HtmlUtil.LI, DataTypes.toString(item));
          }
          out.endTag(HtmlUtil.UL);
        }
      } else {
        out.text(DataTypes.toString(value));
      }
    }
  }
}
