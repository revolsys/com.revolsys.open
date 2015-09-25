package com.revolsys.ui.html.fields;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.HtmlUtil;

public class WeekField extends TextField {

  public WeekField(final String name, final boolean required) {
    super(name, required);
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    final String id = getName();
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_ID, id);
    out.text(" ");
    out.endTag(HtmlUtil.DIV);
    out.startTag(HtmlUtil.SCRIPT);
    out.attribute(HtmlUtil.ATTR_TYPE, "text/javascript");
    out.text("new WeekField('");
    out.text(id);
    out.text("');");
    out.endTag(HtmlUtil.SCRIPT);
  }

}
