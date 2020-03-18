package com.revolsys.ui.html.fields;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

public class WeekField extends TextField {

  public WeekField(final String name, final boolean required) {
    super(name, required);
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    final String id = getName();
    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.ID, id);
    out.text(" ");
    out.endTag(HtmlElem.DIV);
    out.startTag(HtmlElem.SCRIPT);
    out.attribute(HtmlAttr.TYPE, "text/javascript");
    out.text("new WeekField('");
    out.text(id);
    out.text("');");
    out.endTag(HtmlElem.SCRIPT);
  }

}
