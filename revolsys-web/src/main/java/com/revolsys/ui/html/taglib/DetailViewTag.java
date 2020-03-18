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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.revolsys.ui.html.serializer.LabelValueListSerializer;
import com.revolsys.ui.html.view.DetailView;
import com.revolsys.util.Property;

public class DetailViewTag extends TagSupport {
  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = 5852237847322159567L;

  private LabelValueListSerializer model;

  private String modelClass;

  private String name;

  @Override
  public int doEndTag() throws JspException {
    return EVAL_PAGE;
  }

  @Override
  public int doStartTag() throws JspException {
    if (this.name != null) {
      try {
        final Object object = this.pageContext.findAttribute(this.name);
        if (object != null) {
          final Writer out = this.pageContext.getOut();
          Property.setSimple(this.model, "object", object);
          final DetailView view = new DetailView(this.model);
          view.serialize(out);
        }
      } catch (final Throwable t) {
        throw new JspException(t);
      }
    }
    return SKIP_BODY;
  }

  public String getModelClass() {
    return this.modelClass;
  }

  public String getName() {
    return this.name;
  }

  public void setModelClass(final String modelClass) {
    this.modelClass = modelClass;
    try {
      final Class klass = Class.forName(modelClass);
      this.model = (LabelValueListSerializer)klass.newInstance();
    } catch (final Throwable t) {
      throw new IllegalArgumentException("Unable to create class " + modelClass);
    }
  }

  public void setName(final String name) {
    this.name = name;
  }
}
