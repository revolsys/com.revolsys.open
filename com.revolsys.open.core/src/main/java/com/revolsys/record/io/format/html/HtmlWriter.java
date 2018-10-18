package com.revolsys.record.io.format.html;

import java.io.OutputStream;
import java.io.Writer;
import java.util.function.Consumer;

import javax.xml.namespace.QName;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

public class HtmlWriter extends XmlWriter implements HtmlElem, HtmlAttr {

  public HtmlWriter(final OutputStream out) {
    super(out);
  }

  public HtmlWriter(final OutputStream out, final boolean useNamespaces) {
    super(out, useNamespaces);
  }

  public HtmlWriter(final Writer out) {
    super(out);
  }

  public HtmlWriter(final Writer out, final boolean useNamespaces) {
    super(out, useNamespaces);
  }

  public HtmlWriter divClass(final String cssClass) {
    startTag(DIV);
    attribute(CLASS, cssClass);
    return this;
  }

  public HtmlWriter element(final QName element, final Consumer<HtmlWriter> action) {
    startTag(element);
    action.accept(this);
    endTag();
    return this;
  }

  public HtmlWriter elementClass(final QName element, final String cssClass,
    final Consumer<HtmlWriter> action) {
    startTag(element);
    attribute(CLASS, cssClass);
    action.accept(this);
    endTag();
    return this;
  }

  public HtmlWriter table() {
    startTag(TABLE);
    return this;
  }

  public HtmlWriter tableRowLabelValue(final String label, final Object value) {
    startTag(TR);
    startTag(TH);
    attribute(HtmlAttr.STYLE, "text-align:left");
    text(label);
    endTag(TH);
    element(TD, value);
    endTag();
    return this;
  }

  public HtmlWriter tableRowTd(final Object... cells) {
    startTag(TR);
    for (final Object cell : cells) {
      element(TD, cell);
    }
    endTag();
    return this;
  }

  public HtmlWriter tableRowTh(final Object... cells) {
    startTag(TR);
    for (final Object cell : cells) {
      element(TH, cell);
    }
    endTag();
    return this;
  }

  public HtmlWriter write(final Consumer<HtmlWriter> action) {
    action.accept(this);
    return this;
  }

}
