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
import java.util.Map;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.jeometry.common.logging.Logs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**

 */
public abstract class AbstractMapElementTag extends SimpleTagSupport {
  /** The log instance. */
  private static final Logger log = LoggerFactory.getLogger(AbstractMapElementTag.class);

  /** The key to the element in the map. */
  private String key;

  /** The exression to get the elements to write. */
  private final String mapExpression;

  /**
   * Construct a new AbstractElementTag.
   *
   * @param mapExpression The exression to get the map containing the element to
   *          write.
   */
  public AbstractMapElementTag(final String mapExpression) {
    this.mapExpression = mapExpression;
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
      final Object t = expressionEvaluator.evaluate(this.mapExpression, Object.class,
        jspContext.getVariableResolver(), null);
      if (t instanceof Map) {
        final Map map = (Map)t;
        if (map != null) {
          final Object object = map.get(this.key);
          serializeObject(out, object);
        }
      } else {
        Logs.debug(this, t.toString());
      }

    } catch (final Throwable t) {
      log.error(t.getMessage(), t);
      throw new JspTagException(t.getMessage(), t);
    }
  }

  /**
   * @return Returns the key.
   */
  public String getKey() {
    return this.key;
  }

  protected abstract void serializeObject(Writer out, Object object) throws IOException;

  /**
   * @param key The key to set.
   */
  public void setKey(final String key) {
    this.key = key;
  }

}
