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

import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.util.CaseConverter;
import com.revolsys.xml.io.XmlWriter;

public class PathBreadcrumbView extends Element {

  private String contextPath;

  private String path;

  private boolean addSlash ;

  public PathBreadcrumbView(String contextPath, String path) {
    this.contextPath = contextPath;
    this.path = path;
  }

  public PathBreadcrumbView(String contextPath, String path, boolean addSlash) {
    this.contextPath = contextPath;
    this.path = path;
    this.addSlash = addSlash;
  }

  public void serializeElement(final XmlWriter out) {
    path = path.substring(contextPath.length());
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    int queryIndex = path.indexOf('?');
    if (queryIndex != -1) {
      path = path.substring(0, queryIndex - 1);
    }
    int fragmentIndex = path.indexOf('#');
    if (fragmentIndex != -1) {
      path = path.substring(0, fragmentIndex - 1);
    }
    path = path.replaceAll("//+", "/");
    path = path.replaceAll("/?index/?$", "");

    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "breadcrumb");
    out.startTag(HtmlUtil.UL);
    if (path.length() == 0 || path.equals("index")) {
      out.startTag(HtmlUtil.LI);
      out.text("HOME");
      out.endTag(HtmlUtil.LI);
    } else {
      out.startTag(HtmlUtil.LI);
      HtmlUtil.serializeA(out, null, contextPath + "/", "HOME");
      out.endTag(HtmlUtil.LI);
      String[] segments = path.split("/");
      String crumbPath = contextPath;
      if (addSlash) {
        crumbPath += "/";
      }
      for (int i = 0; i < segments.length - 1; i++) {
        String segment = segments[i];
        if (addSlash) {
          crumbPath += segment + "/";
        } else {
          crumbPath += "/" + segment;
        }
        out.startTag(HtmlUtil.LI);
        HtmlUtil.serializeA(out, null, crumbPath,
          CaseConverter.toCapitalizedWords(segment));
        out.endTag(HtmlUtil.LI);
      }
      String segment = segments[segments.length - 1];
      out.startTag(HtmlUtil.LI);
      out.text(CaseConverter.toCapitalizedWords(segment));
      out.endTag(HtmlUtil.LI);
    }

    out.endTag(HtmlUtil.UL);
    out.endTag(HtmlUtil.DIV);
  }
}
