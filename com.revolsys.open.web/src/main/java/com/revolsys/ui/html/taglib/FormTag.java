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

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.form.Form;

public class FormTag extends BodyTagSupport {
  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = 7145894717210883702L;

  private Form form;

  private String name;

  private Object oldFormAttribute;

  private ServletRequest request;

  public int doStartTag() throws JspException {
    if (name != null) {
      request = pageContext.getRequest();
      form = (Form)request.getAttribute(name);
      oldFormAttribute = request.getAttribute("form");
      request.setAttribute("form", form);
    }
    return (EVAL_BODY_INCLUDE);
  }

  public int doEndTag() throws JspException {
    try {
      if (name != null) {
        if (form != null) {
          Writer out = pageContext.getOut();
          out.flush();
          XmlWriter xmlOut = new XmlWriter(out);
          form.serializeStartTag(xmlOut);
          bodyContent.writeOut(xmlOut);
          form.serializeEndTag(xmlOut);
          xmlOut.flush();
        }
        request.setAttribute("form", oldFormAttribute);
      }
      return EVAL_PAGE;
    } catch (Throwable t) {
      throw new JspException(t.getMessage(), t);
    }
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }
}
