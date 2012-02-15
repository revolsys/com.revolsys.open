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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.revolsys.ui.html.view.ObjectView;

public class ObjectViewTag extends TagSupport {
  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = 7234548548120214880L;

  private String name;

  private String viewClass;

  private ObjectView view;

  @Override
  public int doEndTag() throws JspException {
    return EVAL_PAGE;
  }

  @Override
  public int doStartTag() throws JspException {
    if (name != null) {
      try {
        final Object object = pageContext.findAttribute(name);
        if (object != null) {
          view.setObject(object);
          view.serialize(pageContext.getOut());
        }
      } catch (final Throwable t) {
        throw new JspException(t);
      }
    }
    return SKIP_BODY;
  }

  public String getName() {
    return name;
  }

  public String getViewClass() {
    return viewClass;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setViewClass(final String viewClass) {
    this.viewClass = viewClass;
    try {
      final Class klass = Class.forName(viewClass);
      view = (ObjectView)klass.newInstance();
    } catch (final Throwable t) {
      throw new IllegalArgumentException("Unable to create class " + viewClass);
    }
  }
}
