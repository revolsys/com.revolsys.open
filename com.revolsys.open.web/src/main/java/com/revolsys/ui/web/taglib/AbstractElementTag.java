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
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.ui.html.view.Element;

/**

 */
public abstract class AbstractElementTag extends SimpleTagSupport {
  /** The log instance. */
  private static final Logger log = LoggerFactory.getLogger(AbstractElementTag.class);

  /** The exression to get the elements to write. */
  private final String elementExpression;

  /**
   * Construct a new AbstractElementTag.
   *
   * @param elementExpression The exression to get the elements to write.
   */
  public AbstractElementTag(final String elementExpression) {
    this.elementExpression = elementExpression;
  }

  /**
   * Process the tag.
   *
   * @throws JspException If there was an exception processing the tag.
   * @throws IOException If an i/o error occurs.
   */
  @Override
  public void doTag() throws JspException, IOException {
    try {
      final JspContext jspContext = getJspContext();
      final JspWriter out = jspContext.getOut();
      final ExpressionEvaluator expressionEvaluator = jspContext.getExpressionEvaluator();
      final Collection elements = (Collection)expressionEvaluator.evaluate(this.elementExpression,
        Collection.class, jspContext.getVariableResolver(), null);
      if (elements != null) {
        serializeElements(out, elements);
      }

    } catch (final Throwable t) {
      log.error(t.getMessage(), t);
      throw new JspTagException(t.getMessage(), t);
    }
  }

  /**
   * Write out the HTML tags for each element.
   *
   * @param out The writer.
   * @param elements The elements to write.
   * @throws IOException If there was an error writing the elements.
   */
  private void serializeElements(final Writer out, final Collection elements) throws IOException {
    final Iterator elementIter = elements.iterator();
    while (elementIter.hasNext()) {
      final Element element = (Element)elementIter.next();
      element.serialize(out, false);
    }
  }
}
