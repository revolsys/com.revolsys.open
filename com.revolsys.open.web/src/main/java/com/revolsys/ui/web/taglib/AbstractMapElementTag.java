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

import org.apache.log4j.Logger;

/**

 */
public abstract class AbstractMapElementTag extends SimpleTagSupport {
  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = 6250507916829639809L;

  /** The log instance. */
  private static final Logger log = Logger.getLogger(AbstractMapElementTag.class);

  /** The exression to get the elements to write. */
  private final String mapExpression;

  /** The key to the element in the map. */
  private String key;

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
      final Object t = expressionEvaluator.evaluate(mapExpression,
        Object.class, jspContext.getVariableResolver(), null);
      if (t instanceof Map) {
        final Map map = (Map)t;
        if (map != null) {
          final Object object = map.get(key);
          serializeObject(out, object);
        }
      } else {
        log.debug(t);
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
    return key;
  }

  protected abstract void serializeObject(Writer out, Object object)
    throws IOException;

  /**
   * @param key The key to set.
   */
  public void setKey(final String key) {
    this.key = key;
  }

}
