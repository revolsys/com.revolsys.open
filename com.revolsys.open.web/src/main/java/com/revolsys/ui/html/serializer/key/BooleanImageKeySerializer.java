package com.revolsys.ui.html.serializer.key;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.web.config.Page;
import com.revolsys.util.JavaBeanUtil;

public class BooleanImageKeySerializer extends AbstractKeySerializer {
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
  public void serialize(final XmlWriter out, final Object object) {
    final Object value = JavaBeanUtil.getBooleanValue(object, getName());
    String text;
    String imageName;
    if (Boolean.TRUE.equals(value)) {
      imageName = "tick";
      text = "Yes";
    } else {
      imageName = "cross";
      text = "No";
    }
    out.startTag(HtmlUtil.IMG);
    out.attribute(HtmlUtil.ATTR_SRC,
      Page.getAbsoluteUrl("/images/" + imageName + ".png"));
    out.attribute(HtmlUtil.ATTR_ALT, text);
    out.attribute(HtmlUtil.ATTR_TITLE, text);
    out.endTag(HtmlUtil.IMG);
  }
}
