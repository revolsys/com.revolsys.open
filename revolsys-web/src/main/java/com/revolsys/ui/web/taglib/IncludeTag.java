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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.ui.web.config.Component;
import com.revolsys.ui.web.config.Layout;
import com.revolsys.ui.web.config.WebUiContext;

/**
 * <p>
 * Include the file that is defined within the child Component that has the
 * specified name. The Layout for this template must be in the "nice.layout"
 * attribute. The name parameter will be used to obtain the child attribute from
 * the Layout and if found the url from the child will be included. The
 * "nice.layout" attribute for the included template will be equal to it's
 * Component.
 * </p>
 * <p>
 * <b>Example </b>
 * </p>
 *
 * <pre>
 *
 *      &lt;%@ taglib uri=&quot;http://dev.nhigh.com/taglibs/nice&quot; prefix=&quot;nice&quot; %&gt;
 *      &lt;html&gt;
 *      &lt;head&gt;
 *      &lt;nhigh:Styles /&gt;
 *      &lt;/head&gt;
 *      &lt;body&gt;
 *        .
 *      &lt;nice:Include name=&quot;body&quot; /&gt;
 *        .
 *      &lt;/body&gt;
 *      &lt;/html&gt;
 *
 * </pre>
 * <dl>
 * <dt><B>Input Attributes: </B>
 * <dd><code>nice</code>- A Layout bean containing the component for the current
 * template.</dd>
 * </dl>
 *
 * @author P. D. Austin
 * @version 1.0
 * @see Component
 * @see Layout
 */
public class IncludeTag extends TagSupport {
  private static final Logger log = LoggerFactory.getLogger(IncludeTag.class);

  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = -4012541088677153589L;

  /** The name of the component to include */
  private String name;

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
        final Layout layout = context.getCurrentLayout();
        if (layout != null) {
          final Component component = layout.getComponent(this.name);
          if (component != null) {
            component.includeComponent(this.pageContext);
          }
        }
      }
    } catch (final Throwable t) {
      log.error("Error including component: " + this.name, t);
    }
    return SKIP_BODY;
  }

  /**
   * Get the name of the component to be included.
   *
   * @return name the name of the child component to be included
   */
  public String getName() {
    return this.name;
  }

  /**
   * Set the name of the component to be included.
   *
   * @param name the name of the child component to be included
   */
  public void setName(final String name) {
    this.name = name;
  }
}
