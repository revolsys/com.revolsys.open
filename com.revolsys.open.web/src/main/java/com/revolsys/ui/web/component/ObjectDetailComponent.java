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
package com.revolsys.ui.web.component;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

import javax.servlet.ServletContext;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.builder.HtmlUiBuilderFactory;
import com.revolsys.ui.web.config.WebUiContext;

public class ObjectDetailComponent {
  private HtmlUiBuilder builder;

  private Object object;

  private String cssClass;

  private String title;

  public void setProperty(final String name, final Object value) {
    if (name.equals("objectClass")) {
      setObjectClass(value.toString());
    } else if (name.equals("object")) {
      this.object = value;
    } else if (name.equals("cssClass")) {
      this.cssClass = value.toString();
    } else if (name.equals("title")) {
      this.title = value.toString();
    }
  }

  private void setObjectClass(final String objectClassName) {
    ServletContext servletContext = WebUiContext.get()
      .getConfig()
      .getServletContext();
    WebApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
    builder = HtmlUiBuilderFactory.get(applicationContext, objectClassName);
  }

  public final void serialize(final Writer out) throws IOException {
    if (object != null && !(object instanceof String)) {
      builder.createDetailView(object, cssClass, "view", Locale.getDefault())
        .serialize(out);
    }
  }
}
