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
package com.revolsys.ui.html.view;


import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.builder.HtmlUiBuilderFactory;
import com.revolsys.ui.web.config.WebUiContext;
import com.revolsys.xml.io.XmlWriter;

public class ObjectDetailView extends ObjectView {
  private static final Logger log = Logger.getLogger(ObjectDetailView.class);

  private String cssClass = "";

  private String keyListName;

  public void processProperty(final String name, final Object value) {
    String stringValue = (String)value;
    if (name.equals("cssClass")) {
      cssClass = stringValue;
    } else if (name.equals("keyList")) {
      keyListName = stringValue;
    } else if (name.equals("name")) {
      HttpServletRequest request = WebUiContext.get().getRequest();
      setObject(request.getAttribute(stringValue));
    }
  }

  public void serializeElement(final XmlWriter out) {
    Object object = getObject();
    if (object != null) {
      WebUiContext context = WebUiContext.get();
      ServletContext servletContext = WebUiContext.getServletContext();

      WebApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
      HttpServletRequest request = context.getRequest();
      HtmlUiBuilder builder = HtmlUiBuilderFactory.get(applicationContext,
        object.getClass());
      builder.createDetailView(object, "objectView", "view",
        request.getLocale()).serialize(out);
    }
  }
}
