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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.web.servlet.HttpServletLogUtil;

public class DisplayElementTag extends TagSupport {
  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = 7616198383718213550L;

  private String name;

  private boolean useNamespaces = true;

  @Override
  public int doEndTag() throws JspException {
    return EVAL_PAGE;
  }

  @Override
  public int doStartTag() throws JspException {
    if (this.name != null) {
      final HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
      try {
        final Element element = (Element)request.getAttribute(this.name);
        if (element != null) {
          final Writer out = this.pageContext.getOut();
          element.serialize(out, this.useNamespaces);
        }
      } catch (final Throwable t) {
        HttpServletLogUtil.logRequestException(this, request, t);
        throw new JspException(t.getMessage(), t);
      }
    }
    return SKIP_BODY;
  }

  public String getName() {
    return this.name;
  }

  public boolean isUseNamespaces() {
    return this.useNamespaces;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setUseNamespaces(final boolean useNamespaces) {
    this.useNamespaces = useNamespaces;
  }
}
