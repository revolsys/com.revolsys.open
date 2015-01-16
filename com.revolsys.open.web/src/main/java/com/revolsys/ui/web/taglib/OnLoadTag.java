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
package com.revolsys.ui.web.taglib;

import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import com.revolsys.ui.web.config.Page;
import com.revolsys.ui.web.config.WebUiContext;

/**
 * <p>
 * Add an onLoad handler to the HTML body tag for each onload handler defined in
 * the PageDefinition object in the "page" attribute. The following attribute
 * will appear in the output.
 * </p>
 *
 * <pre>
 * onload = &quot;alert('hello');&quot;
 * </pre>
 * <p>
 * <i>NOTE: The OnLoads tag must only be used within the attribute list for the
 * body tag.</i>
 * </p>
 * <p>
 * <b>Example</b>
 * </p>
 *
 * <pre>
 *     &lt;%@ taglib uri=&quot;http://dev.nhigh.com/taglibs/nhigh&quot; prefix=&quot;nhigh&quot; %&gt;
 *     &lt;html&gt;
 *     &lt;head&gt;
 *     &lt;/head&gt;
 *     &lt;body &lt;nhigh:OnLoads /&gt;&gt;
 *       .
 *       .
 *     &lt;/html&gt;
 * </pre>
 * <dl>
 * <dt><B>Input Attributes: </B>
 * <dd><code>page</code> - A PageDefinition bean containing the defintion of
 * this page.</dd>
 * </dl>
 *
 * @author P. D. Austin
 * @version 1.0
 * @see PageDefinition#getOnLoads()
 */
public class OnLoadTag extends TagSupport {
  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = -6153525324186562225L;

  /**
   * Process the end tag.
   *
   * @return EVAL_PAGE
   */
  @Override
  public int doEndTag() throws JspException {
    return EVAL_PAGE;
  }

  /**
   * Process the start tag.
   *
   * @return SKIP_BODY
   */
  @Override
  public int doStartTag() throws JspException {
    try {
      final WebUiContext context = WebUiContext.get();
      if (context != null) {
        final Page page = context.getPage();
        if (page != null) {
          final JspWriter out = this.pageContext.getOut();
          final Iterator onLoads = page.getOnLoads().iterator();
          out.print("onload=\"");
          while (onLoads.hasNext()) {
            final String onLoad = (String)onLoads.next();
            out.print(onLoad);
            out.print("; ");
          }
          out.print("\"");
        }
      }
      return SKIP_BODY;
    } catch (final Throwable t) {
      throw new JspTagException(t);
    }
  }
}
