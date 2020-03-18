/*
 * Copyright 2004-2005 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.ui.html.fields;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.form.Form;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

public class HtmlField extends Field {

  private String inputValue = "";

  private int maxLength = Integer.MAX_VALUE;

  private int minLength = 0;

  public HtmlField(final String name, final boolean required) {
    super(name, required);
  }

  public HtmlField(final String name, final int maxLength, final boolean required) {
    this(name, required);
    this.maxLength = maxLength;
  }

  public HtmlField(final String name, final int minLength, final int maxLength,
    final String defaultValue, final boolean required) {
    this(name, maxLength, defaultValue, required);
    if (minLength <= maxLength) {
      throw new IllegalArgumentException(
        "minLength (" + minLength + ") must be <= maxLength (" + minLength + ")");
    }
    this.minLength = minLength;
  }

  public HtmlField(final String name, final int maxLength, final String defaultValue,
    final boolean required) {
    this(name, defaultValue, required);
    this.maxLength = maxLength;
  }

  public HtmlField(final String name, final String defaultValue, final boolean required) {
    this(name, required);
    this.inputValue = defaultValue;
  }

  public String getInputValue() {
    return this.inputValue;
  }

  public final int getMaxLength() {
    return this.maxLength;
  }

  public int getMinLength() {
    return this.minLength;
  }

  @Override
  public boolean hasValue() {
    return this.inputValue != null && !this.inputValue.equals("");
  }

  /*
   * (non-Javadoc)
   * @see
   * com.revolsys.ui.html.form.Field#initialize(com.revolsys.ui.html.form.Form,
   * javax.servlet.http.HttpServletRequest)
   */
  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    form.addOnSubmit(this.getName() + "IafHtml.updateTextArea()");
    this.inputValue = request.getParameter(getName());
    if (this.inputValue == null) {
      setValue(getInitialValue(request));
      if (getValue() != null) {
        this.inputValue = getValue().toString();
      }
    }
  }

  @Override
  public boolean isValid() {
    boolean valid = true;
    if (!super.isValid()) {
      valid = false;
    } else if (hasValue()) {
      final int length = this.inputValue.length();
      if (length > this.maxLength) {
        this.inputValue = this.inputValue.substring(0, this.maxLength);
        addValidationError("Cannot exceed " + this.maxLength + " characters");
        valid = false;
      } else if (length < this.minLength) {
        addValidationError("Must be at least " + this.minLength + " characters");
        valid = false;
      }
    }
    if (valid) {
      setValue(this.inputValue);
    }
    return valid;
  }

  private void serializeBlockFormatOption(final XmlWriter out, final String tag,
    final String title) {
    out.startTag(HtmlElem.OPTION);
    out.attribute(HtmlAttr.VALUE, tag);
    out.text(title);
    out.endTag(HtmlElem.OPTION);
  }

  private void serializeBlockFormatToolbarList(final XmlWriter out) {
    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, "blockFormat");

    out.startTag(HtmlElem.SELECT);
    out.attribute(HtmlAttr.ON_CHANGE, getName() + "IafHtml.formatBlock(this.value);this.value=''");
    serializeBlockFormatOption(out, "", ".. Select Paragraph Format");
    serializeBlockFormatOption(out, "<p>", "Normal");
    serializeBlockFormatOption(out, "<h1>", "Heading 1");
    serializeBlockFormatOption(out, "<h2>", "Heading 2");
    serializeBlockFormatOption(out, "<h3>", "Heading 3");
    serializeBlockFormatOption(out, "<h4>", "Heading 4");
    serializeBlockFormatOption(out, "<h5>", "Heading 5");
    serializeBlockFormatOption(out, "<h6>", "Heading 6");
    out.endTag(HtmlElem.SELECT);

    out.endTag(HtmlElem.DIV);
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, "htmlField");

    serializeHtmlWidget(out);
    serializePlainTextWidget(out);

    out.startTag(HtmlElem.SCRIPT);
    out.attribute(HtmlAttr.TYPE, "text/javascript");
    out.text("var " + getName() + "IafHtml = new HtmlField('" + getName() + "');");
    out.endTag(HtmlElem.SCRIPT);

    out.endTag(HtmlElem.DIV);
  }

  private void serializeHtmlWidget(final XmlWriter out) {
    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, "htmlFieldWidget");
    out.attribute(HtmlAttr.ID, getName() + "HtmlWidget");

    serializeToolbar(out);

    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, "htmlFieldEditPanel");
    out.attribute(HtmlAttr.ID, getName() + "HtmlWidgetPanel");
    out.endTag(HtmlElem.DIV);

    out.endTag(HtmlElem.DIV);
  }

  private void serializePlainTextWidget(final XmlWriter out) {
    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, "htmlFieldPlain");
    out.attribute(HtmlAttr.ID, getName() + "PlainWidget");

    out.startTag(HtmlElem.TEXT_AREA);
    out.attribute(HtmlAttr.ID, getName() + "TextArea");
    out.attribute(HtmlAttr.NAME, getName());
    out.attribute(HtmlAttr.COLS, "40");
    out.attribute(HtmlAttr.ROWS, "10");
    if (this.inputValue != null) {
      out.text(this.inputValue);
    } else {
      out.text("");
    }
    out.endTag(HtmlElem.TEXT_AREA);

    out.endTag(HtmlElem.DIV);
  }

  private void serializeToolbar(final XmlWriter out) {
    // Toolbar row 1
    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, "toolBar ");

    // Text decoration group
    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, "group ");
    serializeToolbarButton(out, "bold", "Bold", getName() + "IafHtml.bold()");
    serializeToolbarButton(out, "italic", "Italic", getName() + "IafHtml.italic()");
    serializeToolbarButton(out, "underline", "Underline", getName() + "IafHtml.underline()");
    serializeToolbarButton(out, "superscript", "Superscript", getName() + "IafHtml.superscript()");
    serializeToolbarButton(out, "subscript", "Subscript", getName() + "IafHtml.subscript()");
    out.endTag(HtmlElem.DIV);

    // Link and Image decoration group
    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, "group ");
    serializeToolbarButton(out, "createLink", "Create Link", getName() + "IafHtml.createLink()");
    serializeToolbarButton(out, "unlink", "Remove Link", getName() + "IafHtml.unLink()");
    out.endTag(HtmlElem.DIV);

    // Paragraph style group
    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, "group ");
    serializeToolbarButton(out, "orderedList", "Numbered List",
      getName() + "IafHtml.orderedList()");
    serializeToolbarButton(out, "unorderedList", "Bulleted List",
      getName() + "IafHtml.unorderedList()");
    serializeBlockFormatToolbarList(out);
    out.endTag(HtmlElem.DIV);

    out.endTag(HtmlElem.DIV);
  }

  private void serializeToolbarButton(final XmlWriter out, final String cssClass,
    final String title, final String onClick) {
    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, "button " + cssClass);
    out.attribute(HtmlAttr.TITLE, title);
    out.attribute(HtmlAttr.ON_CLICK, onClick);
    out.text("");
    out.endTag(HtmlElem.DIV);

  }
}
