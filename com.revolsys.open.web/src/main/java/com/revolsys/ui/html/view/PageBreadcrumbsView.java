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

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.web.config.Page;
import com.revolsys.ui.web.config.WebUiContext;

public class PageBreadcrumbsView extends ObjectView {
  private String cssClass = "breadcrumbsMenu";

  private WebUiContext context;

  private String pageNode;

  public PageBreadcrumbsView() {
    context = WebUiContext.get();
    setObject(context.getPage());
  }

  public void setProperty(final String name, final Object value) {
    if (value != null) {
      super.setProperty(name, value.toString());
      if (name.equals("cssClass")) {
        cssClass = value.toString();
      }
    }
  }

  public void serializeElement(final XmlWriter out) {
    Page page = (Page)getObject();
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, cssClass);

    out.startTag(HtmlUtil.UL);
    crumb(out, page, true);
    out.endTag(HtmlUtil.UL);

    out.endTag(HtmlUtil.DIV);
  }

  private void crumb(final XmlWriter out, final Page page, final boolean current)
    {
    if (page == null) {
      out.startTag(HtmlUtil.LI);
      out.startTag(HtmlUtil.A);
      out.attribute(HtmlUtil.ATTR_HREF, context.getConfig().getBasePath() + "/");
      out.text("HOME");
      out.endTag(HtmlUtil.A);
      out.text(" >");
      out.endTag(HtmlUtil.LI);
    } else {
      crumb(out, page.getParent(), false);
      out.startTag(HtmlUtil.LI);
      if (current) {
        out.attribute(HtmlUtil.ATTR_CLASS, "current");
        out.text(page.getTitle());
      } else {
        out.startTag(HtmlUtil.A);
        out.attribute(HtmlUtil.ATTR_HREF, page.getFullUrl());
        out.text(page.getTitle());
        out.endTag(HtmlUtil.A);
        out.text(" >");
      }
      out.endTag(HtmlUtil.LI);
    }
  }
}
