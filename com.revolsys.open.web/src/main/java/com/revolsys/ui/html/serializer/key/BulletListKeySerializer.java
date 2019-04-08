package com.revolsys.ui.html.serializer.key;

import java.util.Collection;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.Property;

public class BulletListKeySerializer extends AbstractKeySerializer {

  public BulletListKeySerializer() {
    setProperty("sortable", false);
    setProperty("searchable", false);
  }

  public BulletListKeySerializer(final String name) {
    this();
    setName(name);
  }

  /**
   * Serialize the value to the XML writer.
   *
   * @param out The XML writer to serialize to.
   * @param object The object to get the value from.
   */
  @Override
  public void serialize(final XmlWriter out, final Object object) {
    final Object value = Property.get(object, getKey());
    if (value == null) {
      out.text("-");
    } else {
      if (value instanceof Collection) {
        final Collection<?> collection = (Collection<?>)value;
        if (collection.isEmpty()) {
          out.text("-");
        } else {
          out.startTag(HtmlElem.UL);
          for (final Object item : collection) {
            out.element(HtmlElem.LI, DataTypes.toString(item));
          }
          out.endTag(HtmlElem.UL);
        }
      } else {
        out.text(DataTypes.toString(value));
      }
    }
  }
}
