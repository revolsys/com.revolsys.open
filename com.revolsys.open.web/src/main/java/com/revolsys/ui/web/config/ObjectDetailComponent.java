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
package com.revolsys.ui.web.config;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;

import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.builder.HtmlUiBuilderFactory;
import com.revolsys.ui.html.view.Element;

public class ObjectDetailComponent extends Component {
  private static final Logger log = Logger.getLogger(ObjectDetailComponent.class);

  private String attribute;

  private String cssClass;

  private String keyList;

  public ObjectDetailComponent(final String area, final String name,
    final String attribute) {
    super(area, name);
    setAttribute(attribute);
  }

  public ObjectDetailComponent(final ObjectDetailComponent component) {
    super(component);
    setAttribute(component.attribute);
    setCssClass(component.getCssClass());
  }

  public Object clone() {
    return new ObjectDetailComponent(this);
  }

  public boolean equals(final Object o) {
    if (o instanceof ObjectDetailComponent) {
      ObjectDetailComponent c = (ObjectDetailComponent)o;
      if (equalsWithNull(c.attribute, attribute)
        && equalsWithNull(c.keyList, keyList)
        && equalsWithNull(c.cssClass, cssClass) && super.equals(o)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Generate the hash code for the object.
   * 
   * @return The hashCode.
   */
  public int hashCode() {
    return super.hashCode() + (attribute.hashCode() << 2)
      + (cssClass.hashCode() << 4) + (keyList.hashCode() << 6);
  }

  public void includeComponent(final PageContext context)
    throws ServletException, IOException {
    Object object = context.findAttribute(attribute);
    if (object != null) {
      ServletContext servletContext = context.getServletContext();
      WebApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
      ServletRequest request = context.getRequest();
      HtmlUiBuilder builder = HtmlUiBuilderFactory.get(applicationContext,
        object.getClass());
      Writer out = context.getOut();
      Element view = builder.createDetailView(object, cssClass, keyList,
        request.getLocale());
      view.serialize(out);
    }
  }

  public String getAttribute() {
    return attribute;
  }

  private void setAttribute(final String attribute) {
    this.attribute = attribute;
  }

  public String getCssClass() {
    return cssClass;
  }

  public void setCssClass(final String cssClass) {
    this.cssClass = cssClass;
  }

  public String getKeyList() {
    return keyList;
  }

  public void setKeyList(final String keyList) {
    this.keyList = keyList;
  }

}
