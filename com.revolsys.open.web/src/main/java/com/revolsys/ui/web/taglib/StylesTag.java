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

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.web.config.Page;
import com.revolsys.ui.web.config.PageController;
import com.revolsys.ui.web.config.SiteNodeController;
import com.revolsys.ui.web.config.WebUiContext;

/**
 * <p>
 * Add a HTML link to each the style sheets in the PageDefinition object in the
 * "page" attribute. The following line will appear in the output for each
 * style.
 * </p>
 *
 * <pre>
 *       &lt;link rel=&quot;stylesheet&quot; href=&quot;/style/main.css&quot; text=&quot;text/css&quot; /&gt;
 * </pre>
 * <p>
 * <i>NOTE: The Style tag must only be used in the head section of a HTML
 * page.</i>
 * </p>
 * <p>
 * <b>Example</b>
 * </p>
 *
 * <pre>
 *       &lt;%@ taglib uri=&quot;http://dev.nhigh.com/taglibs/nhigh&quot; prefix=&quot;nhigh&quot; %&gt;
 *       &lt;html&gt;
 *       &lt;head&gt;
 *       &lt;nhigh:Styles /&gt;
 *       &lt;/head&gt;
 *         .
 *         .
 *       &lt;/html&gt;
 * </pre>
 * <dl>
 * <dt><B>Input Attributes: </B>
 * <dd><code>page</code> - A PageDefinition bean containing the defintion of
 * this page.</dd>
 * </dl>
 *
 * @author P. D. Austin
 * @version 1.0
 * @see Page#getStyles()
 * @see PageController#getStyles()
 */
public class StylesTag extends TagSupport {
  /** The log instance. */
  private static final Logger log = LoggerFactory.getLogger(StylesTag.class);

  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = 6250507916829639809L;

  /**
   * Process the end tag.
   *
   * @return EVAL_PAGE
   * @throws JspException If there was an exception processing the tag.
   */
  @Override
  public int doEndTag() throws JspException {
    return EVAL_PAGE;
  }

  /**
   * Process the start tag.
   *
   * @return SKIP_BODY
   * @throws JspException If there was an exception processing the tag.
   */
  @Override
  public int doStartTag() throws JspException {
    try {
      final WebUiContext context = WebUiContext.get();
      if (context != null) {
        final Page page = context.getPage();
        if (page != null) {
          final Collection styles = page.getStyles();
          String contextPath = context.getContextPath();
          if (contextPath.equals("/")) {
            contextPath = "";
          }
          final JspWriter out = this.pageContext.getOut();
          final Iterator styleIter = styles.iterator();
          while (styleIter.hasNext()) {
            final String style = (String)styleIter.next();
            out.print("<link rel=\"stylesheet\" href=\"");
            out.print(style);
            out.println("\" type=\"text/css\" />");
          }
        }
      }
      final SiteNodeController controller = (SiteNodeController)this.pageContext
        .findAttribute("rsWebController");
      if (controller instanceof PageController) {
        final PageController page = (PageController)controller;
        serializeElements(page.getStyles());
      }

      return SKIP_BODY;
    } catch (final Throwable t) {
      log.error(t.getMessage(), t);
      throw new JspTagException(t.getMessage(), t);
    }
  }

  /**
   * Write out the HTML tags for each element.
   *
   * @param styles The styles.
   * @throws IOException If there was an error writing the styles.
   */
  private void serializeElements(final Collection styles) throws IOException {
    final JspWriter out = this.pageContext.getOut();
    final Iterator elements = styles.iterator();
    while (elements.hasNext()) {
      final Element element = (Element)elements.next();
      element.serialize(out);
    }
  }

}
