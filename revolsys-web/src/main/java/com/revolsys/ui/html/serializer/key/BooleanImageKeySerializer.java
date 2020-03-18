package com.revolsys.ui.html.serializer.key;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.web.utils.HttpServletUtils;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.JavaBeanUtil;

public class BooleanImageKeySerializer extends AbstractKeySerializer {
  public static boolean serialize(final XmlWriter out, final Object object, final String name) {
    final Object value = JavaBeanUtil.getBooleanValue(object, name);
    String text;
    String imageName;
    final boolean result = Boolean.TRUE.equals(value);
    if (result) {
      imageName = "tick";
      text = "Yes";
    } else {
      imageName = "cross";
      text = "No";
    }
    out.startTag(HtmlElem.IMG);
    out.attribute(HtmlAttr.SRC, HttpServletUtils.getAbsoluteUrl("/images/" + imageName + ".png"));
    out.attribute(HtmlAttr.ALT, text);
    out.attribute(HtmlAttr.TITLE, text);
    out.endTag(HtmlElem.IMG);
    return result;
  }

  public BooleanImageKeySerializer() {
    setProperty("searchable", false);
  }

  public BooleanImageKeySerializer(final String name) {
    super(name);
    setProperty("searchable", false);
  }

  public BooleanImageKeySerializer(final String name, final String label) {
    super(name, label);
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
    final String name = getName();
    serialize(out, object, name);
  }
}
