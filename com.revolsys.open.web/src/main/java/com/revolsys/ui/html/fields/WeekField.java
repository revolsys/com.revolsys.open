package com.revolsys.ui.html.fields;


import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.xml.io.XmlWriter;

public class WeekField extends TextField {

  public WeekField(String name, boolean required) {
    super(name, required);
  }

  public void serializeElement(final XmlWriter out) {
    String id = getName();
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
