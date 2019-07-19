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

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.ui.html.view.Element;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

public class FieldLabelDecorator implements Decorator {
  private String instructions = "";

  private String label = "";

  public FieldLabelDecorator(final String label) {
    this.label = label;
  }

  public FieldLabelDecorator(final String label, final String instructions) {
    this.label = label;
    this.instructions = instructions;
  }

  public String getInstructions() {
    return this.instructions;
  }

  public String getLabel() {
    return this.label;
  }

  @Override
  public void serialize(final XmlWriter out, final Element element) {
    final Field field = (Field)element;
    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, "field");
    serializeLabel(out, field);
    serializeField(out, field);
    serializeInstructions(out);
    serializeErrors(out, field);
    out.endTag(HtmlElem.DIV);
  }

  protected void serializeErrors(final XmlWriter out, final Field field) {
    if (field.hasValidationErrors()) {
      out.startTag(HtmlElem.DIV);
      out.attribute(HtmlAttr.CLASS, "errors");
      for (final Iterator<String> validationErrors = field.getValidationErrors()
        .iterator(); validationErrors.hasNext();) {
        final String error = validationErrors.next();
        out.text(error);
        if (validationErrors.hasNext()) {
          out.text(", ");
        }
      }
      out.endTag(HtmlElem.DIV);
    }
  }

  protected void serializeField(final XmlWriter out, final Field field) {
    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, "contents");
    field.serializeElement(out);
    out.endTag(HtmlElem.DIV);
  }

  protected void serializeInstructions(final XmlWriter out) {
    final String instructions = getInstructions();
    if (instructions != null) {
      out.startTag(HtmlElem.DIV);
      out.attribute(HtmlAttr.CLASS, "instructions");
      out.text(instructions);
      out.endTag(HtmlElem.DIV);
    }
  }

  protected void serializeLabel(final XmlWriter out, final Field field) {
    final String label = getLabel();
    if (label != null) {
      out.startTag(HtmlElem.DIV);
      if (field.isRequired()) {
        out.attribute(HtmlAttr.CLASS, "label required");
      } else {
        out.attribute(HtmlAttr.CLASS, "label");
      }
      out.startTag(HtmlElem.LABEL);
      out.attribute(HtmlAttr.FOR, field.getName());
      out.text(label);
      out.endTag(HtmlElem.LABEL);
      out.endTag(HtmlElem.DIV);
    }
  }

  public void setInstructions(final String instructions) {
    this.instructions = instructions;
  }

  public void setLabel(final String label) {
    this.label = label;
  }
}
