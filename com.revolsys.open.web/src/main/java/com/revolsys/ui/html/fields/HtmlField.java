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

import com.revolsys.format.xml.XmlWriter;
import com.revolsys.ui.html.form.Form;
import com.revolsys.util.HtmlUtil;

public class HtmlField extends Field {

  private int minLength = 0;

  private int maxLength = Integer.MAX_VALUE;

  private String inputValue = "";

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
      throw new IllegalArgumentException("minLength (" + minLength + ") must be <= maxLength ("
        + minLength + ")");
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

  private void serializeBlockFormatOption(final XmlWriter out, final String tag, final String title) {
    out.startTag(HtmlUtil.OPTION);
    out.attribute(HtmlUtil.ATTR_VALUE, tag);
    out.text(title);
    out.endTag(HtmlUtil.OPTION);
  }

  private void serializeBlockFormatToolbarList(final XmlWriter out) {
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "blockFormat");

    out.startTag(HtmlUtil.SELECT);
    out.attribute(HtmlUtil.ATTR_ON_CHANGE, getName()
      + "IafHtml.formatBlock(this.value);this.value=''");
    serializeBlockFormatOption(out, "", ".. Select Paragraph Format");
    serializeBlockFormatOption(out, "<p>", "Normal");
    serializeBlockFormatOption(out, "<h1>", "Heading 1");
    serializeBlockFormatOption(out, "<h2>", "Heading 2");
    serializeBlockFormatOption(out, "<h3>", "Heading 3");
    serializeBlockFormatOption(out, "<h4>", "Heading 4");
    serializeBlockFormatOption(out, "<h5>", "Heading 5");
    serializeBlockFormatOption(out, "<h6>", "Heading 6");
    out.endTag(HtmlUtil.SELECT);

    out.endTag(HtmlUtil.DIV);
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "htmlField");

    serializeHtmlWidget(out);
    serializePlainTextWidget(out);

    out.startTag(HtmlUtil.SCRIPT);
    out.attribute(HtmlUtil.ATTR_TYPE, "text/javascript");
    out.text("var " + getName() + "IafHtml = new HtmlField('" + getName() + "');");
    out.endTag(HtmlUtil.SCRIPT);

    out.endTag(HtmlUtil.DIV);
  }

  private void serializeHtmlWidget(final XmlWriter out) {
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "htmlFieldWidget");
    out.attribute(HtmlUtil.ATTR_ID, getName() + "HtmlWidget");

    serializeToolbar(out);

    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "htmlFieldEditPanel");
    out.attribute(HtmlUtil.ATTR_ID, getName() + "HtmlWidgetPanel");
    out.endTag(HtmlUtil.DIV);

    out.endTag(HtmlUtil.DIV);
  }

  private void serializePlainTextWidget(final XmlWriter out) {
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "htmlFieldPlain");
    out.attribute(HtmlUtil.ATTR_ID, getName() + "PlainWidget");

    out.startTag(HtmlUtil.TEXT_AREA);
    out.attribute(HtmlUtil.ATTR_ID, getName() + "TextArea");
    out.attribute(HtmlUtil.ATTR_NAME, getName());
    out.attribute(HtmlUtil.ATTR_COLS, "40");
    out.attribute(HtmlUtil.ATTR_ROWS, "10");
    if (this.inputValue != null) {
      out.text(this.inputValue);
    } else {
      out.text("");
    }
    out.endTag(HtmlUtil.TEXT_AREA);

    out.endTag(HtmlUtil.DIV);
  }

  private void serializeToolbar(final XmlWriter out) {
    // Toolbar row 1
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "toolBar ");

    // Text decoration group
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "group ");
    serializeToolbarButton(out, "bold", "Bold", getName() + "IafHtml.bold()");
    serializeToolbarButton(out, "italic", "Italic", getName() + "IafHtml.italic()");
    serializeToolbarButton(out, "underline", "Underline", getName() + "IafHtml.underline()");
    serializeToolbarButton(out, "superscript", "Superscript", getName() + "IafHtml.superscript()");
    serializeToolbarButton(out, "subscript", "Subscript", getName() + "IafHtml.subscript()");
    out.endTag(HtmlUtil.DIV);

    // Link and Image decoration group
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "group ");
    serializeToolbarButton(out, "createLink", "Create Link", getName() + "IafHtml.createLink()");
    serializeToolbarButton(out, "unlink", "Remove Link", getName() + "IafHtml.unLink()");
    out.endTag(HtmlUtil.DIV);

    // Paragraph style group
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "group ");
    serializeToolbarButton(out, "orderedList", "Numbered List", getName() + "IafHtml.orderedList()");
    serializeToolbarButton(out, "unorderedList", "Bulleted List", getName()
      + "IafHtml.unorderedList()");
    serializeBlockFormatToolbarList(out);
    out.endTag(HtmlUtil.DIV);

    out.endTag(HtmlUtil.DIV);
  }

  private void serializeToolbarButton(final XmlWriter out, final String cssClass,
    final String title, final String onClick) {
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "button " + cssClass);
    out.attribute(HtmlUtil.ATTR_TITLE, title);
    out.attribute(HtmlUtil.ATTR_ON_CLICK, onClick);
    out.text("");
    out.endTag(HtmlUtil.DIV);

  }
}
