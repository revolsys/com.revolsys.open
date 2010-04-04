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

import org.apache.log4j.Logger;

import com.revolsys.ui.html.view.Script;
import com.revolsys.ui.web.config.Page;
import com.revolsys.ui.web.config.PageController;
import com.revolsys.ui.web.config.SiteNodeController;
import com.revolsys.ui.web.config.WebUiContext;

/**
 * <p>
 * Add a HTML script tag linking to each the scripts in the PageDefinition
 * object in the "page" attribute. The following line will appear in the output
 * for each script.
 * </p>
 * 
 * <pre>
 *       &lt;script language=&quot;JavaScript&quot; src=&quot;/js/main.js&quot;&gt;&lt;/script&gt;
 * </pre>
 * 
 * <p>
 * <i>NOTE: The Scripts tag should only be used in the head section of a HTML
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
 *       &lt;nhigh:Scripts /&gt;
 *       &lt;/head&gt;
 *         .
 *         .
 *       &lt;/html&gt;
 * </pre>
 * 
 * <dl>
 * <dt><B>Input Attributes: </B>
 * <dd><code>page</code> - A PageDefinition bean containing the defintion of
 * this page.</dd>
 * </dl>
 * 
 * @author P. D. Austin
 * @version 1.0
 * @see Page#getScripts()
 * @see PageController#getScripts()
 */
public class ScriptsTag extends TagSupport {
  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = -2928296078151434180L;

  /** The logging instance. */
  private static final Logger log = Logger.getLogger(ScriptsTag.class);

  /**
   * Process the start tag.
   * 
   * @return SKIP_BODY
   * @throws JspException If there was an exception processing the tag.
   */
  public int doStartTag() throws JspException {
    try {
      WebUiContext context = WebUiContext.get();
      if (context != null) {
        Page page = (Page)context.getPage();
        if (page != null) {
          JspWriter out = pageContext.getOut();
          String contextPath = context.getContextPath();
          if (contextPath.equals("/")) {
            contextPath = "";
          }
          Iterator scripts = page.getScripts().iterator();
          while (scripts.hasNext()) {
            String script = (String)scripts.next();
            out.print("<script type=\"text/javascript\" src=\"");
            out.print(contextPath);
            out.print(script);
            out.println("\">\n</script>");
          }
        }
      }
      SiteNodeController controller = (SiteNodeController)pageContext.findAttribute("rsWebController");
      if (controller instanceof PageController) {
        PageController page = (PageController)controller;
        writeScripts(page.getScripts());
      }
    } catch (Throwable t) {
      log.error(t.getMessage(), t);
    }
    return SKIP_BODY;
  }

  /**
   * Write out the HTML tags for each script.
   * 
   * @param scripts The scripts.
   * @throws IOException If there was an error writing the scripts.
   */
  private void writeScripts(final Collection scripts) throws IOException {
    JspWriter out = pageContext.getOut();
    for (Iterator scriptIter = scripts.iterator(); scriptIter.hasNext();) {
      Script script = (Script)scriptIter.next();
      String content = script.getContent();
      if (content != null) {
        out.print("<script type=\"");
        out.print(script.getType());
        out.println("\">");
        out.println(content);
        out.println("</script>");
      } else {
        out.print("<script src=\"");
        out.print(script.getFile());
        out.print("\" type=\"");
        out.print(script.getType());
        out.println("\">\n</script>");
      }
    }
  }

  /**
   * Process the end tag.
   * 
   * @return EVAL_PAGE
   * @throws JspException If there was an exception processing the tag.
   */
  public int doEndTag() throws JspException {
    return EVAL_PAGE;
  }
}
