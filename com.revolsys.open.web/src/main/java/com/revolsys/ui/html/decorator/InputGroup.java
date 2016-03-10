package com.revolsys.ui.html.decorator;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.util.HtmlUtil;

public class InputGroup extends ElementContainer {

  private final Element input;

  private final List<Element> buttons = new ArrayList<>();

  public InputGroup(final Element input) {
    this.input = input;
    add(input);
  }

  public InputGroup addButton(final Element button) {
    this.buttons.add(button);
    add(button);
    return this;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    if (this.buttons.isEmpty()) {
      this.input.serialize(out);
    } else {
      out.startTag(HtmlUtil.DIV);
      out.attribute(HtmlUtil.ATTR_CLASS, "input-group");
      {
        this.input.serialize(out);
        QName tag;
        if (this.buttons.size() == 1) {
          tag = HtmlUtil.SPAN;
        } else {
          tag = HtmlUtil.DIV;
        }
        out.startTag(tag);
        out.attribute(HtmlUtil.ATTR_CLASS, "input-group-btn");
        for (final Element button : this.buttons) {
          button.serialize(out);
        }
        out.endTag(tag);
      }
      out.endTag(HtmlUtil.DIV);
    }
  }

}
