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
package com.revolsys.ui.html.taglib;

import java.io.Writer;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.form.Form;

public class FormTag extends BodyTagSupport {
  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = 7145894717210883702L;

  private Form form;

  private String name;

  private Object oldFormAttribute;

  private ServletRequest request;

  @Override
  public int doEndTag() throws JspException {
    try {
      if (this.name != null) {
        if (this.form != null) {
          final Writer out = this.pageContext.getOut();
          out.flush();
          final XmlWriter xmlOut = new XmlWriter(out);
          this.form.serializeStartTag(xmlOut);
          this.bodyContent.writeOut(xmlOut);
          this.form.serializeEndTag(xmlOut);
          xmlOut.flush();
        }
        this.request.setAttribute("form", this.oldFormAttribute);
      }
      return EVAL_PAGE;
    } catch (final Throwable t) {
      throw new JspException(t.getMessage(), t);
    }
  }

  @Override
  public int doStartTag() throws JspException {
    if (this.name != null) {
      this.request = this.pageContext.getRequest();
      this.form = (Form)this.request.getAttribute(this.name);
      this.oldFormAttribute = this.request.getAttribute("form");
      this.request.setAttribute("form", this.form);
    }
    return EVAL_BODY_INCLUDE;
  }

  public String getName() {
    return this.name;
  }

  public void setName(final String name) {
    this.name = name;
  }
}
