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
package com.revolsys.ui.html.decorator;

import java.util.Iterator;

import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.ui.html.view.Element;
import com.revolsys.xml.io.XmlWriter;

public class FieldLabelDecorator implements Decorator {
  private String label = "";

  private String instructions = "";

  public FieldLabelDecorator(
    final String label) {
    this.label = label;
  }

  public FieldLabelDecorator(
    final String label,
    final String instructions) {
    this.label = label;
    this.instructions = instructions;
  }

  public String getInstructions() {
    return instructions;
  }

  public String getLabel() {
    return label;
  }

  public void serialize(
    final XmlWriter out,
    final Element element) {
    Field field = (Field)element;
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "field");
    serializeLabel(out, field);
    serializeField(out, field);
    serializeInstructions(out);
    serializeErrors(out, field);
    out.endTag(HtmlUtil.DIV);
  }

  protected void serializeLabel(
    final XmlWriter out,
    final Field field) {
    String label = getLabel();
    if (label != null) {
      out.startTag(HtmlUtil.DIV);
      if (field.isRequired()) {
        out.attribute(HtmlUtil.ATTR_CLASS, "label required");
      } else {
        out.attribute(HtmlUtil.ATTR_CLASS, "label");
      }
      out.startTag(HtmlUtil.LABEL);
      out.attribute(HtmlUtil.ATTR_FOR, field.getName());
      out.text(label);
      out.endTag(HtmlUtil.LABEL);
      out.endTag(HtmlUtil.DIV);
    }
  }

  protected void serializeField(
    final XmlWriter out,
    final Field field) {
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "contents");
    field.serializeElement(out);
    out.endTag(HtmlUtil.DIV);
  }

  protected void serializeInstructions(
    final XmlWriter out) {
    String instructions = getInstructions();
    if (instructions != null) {
      out.startTag(HtmlUtil.DIV);
      out.attribute(HtmlUtil.ATTR_CLASS, "instructions");
      out.text(instructions);
      out.endTag(HtmlUtil.DIV);
    }
  }

  protected void serializeErrors(
    final XmlWriter out,
    final Field field) {
    if (field.hasValidationErrors()) {
      out.startTag(HtmlUtil.DIV);
      out.attribute(HtmlUtil.ATTR_CLASS, "errors");
      for (Iterator validationErrors = field.getValidationErrors().iterator(); validationErrors.hasNext();) {
        String error = (String)validationErrors.next();
        out.text(error);
        if (validationErrors.hasNext()) {
          out.text(", ");
        }
      }
      out.endTag(HtmlUtil.DIV);
    }
  }

  public void setInstructions(
    final String instructions) {
    this.instructions = instructions;
  }

  public void setLabel(
    final String label) {
    this.label = label;
  }
}
