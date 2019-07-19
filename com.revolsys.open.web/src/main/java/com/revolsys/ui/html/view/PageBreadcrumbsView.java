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

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.web.config.Page;
import com.revolsys.ui.web.config.WebUiContext;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

public class PageBreadcrumbsView extends ObjectView {
  private final WebUiContext context;

  private String cssClass = "breadcrumbsMenu";

  public PageBreadcrumbsView() {
    this.context = WebUiContext.get();
    setObject(this.context.getPage());
  }

  private void crumb(final XmlWriter out, final Page page, final boolean current) {
    if (page == null) {
      out.startTag(HtmlElem.LI);
      out.startTag(HtmlElem.A);
      out.attribute(HtmlAttr.HREF, this.context.getConfig().getBasePath() + "/");
      out.text("HOME");
      out.endTag(HtmlElem.A);
      out.text(" >");
      out.endTag(HtmlElem.LI);
    } else {
      crumb(out, page.getParent(), false);
      out.startTag(HtmlElem.LI);
      if (current) {
        out.attribute(HtmlAttr.CLASS, "current");
        out.text(page.getTitle());
      } else {
        out.startTag(HtmlElem.A);
        out.attribute(HtmlAttr.HREF, page.getFullUrl());
        out.text(page.getTitle());
        out.endTag(HtmlElem.A);
        out.text(" >");
      }
      out.endTag(HtmlElem.LI);
    }
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    final Page page = (Page)getObject();
    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, this.cssClass);

    out.startTag(HtmlElem.UL);
    crumb(out, page, true);
    out.endTag(HtmlElem.UL);

    out.endTag(HtmlElem.DIV);
  }

  @Override
  public void setProperty(final String name, final Object value) {
    if (value != null) {
      super.setProperty(name, value.toString());
      if (name.equals("cssClass")) {
        this.cssClass = value.toString();
      }
    }
  }
}
