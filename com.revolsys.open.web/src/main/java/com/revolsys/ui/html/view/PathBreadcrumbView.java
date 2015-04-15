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
import com.revolsys.ui.web.controller.PathAliasController;
import com.revolsys.ui.web.utils.HttpServletUtils;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.HtmlUtil;

public class PathBreadcrumbView extends Element {

  private final String contextPath;

  private final String path;

  private boolean addSlash;

  public PathBreadcrumbView(final String contextPath, final String path) {
    this.contextPath = contextPath;
    this.path = path;
  }

  public PathBreadcrumbView(final String contextPath, final String path, final boolean addSlash) {
    this.contextPath = contextPath;
    this.path = path;
    this.addSlash = addSlash;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    String path = this.path.substring(this.contextPath.length());
    final String pathPrefix = HttpServletUtils.getAttribute(PathAliasController.PATH_PREFIX);
    String crumbPath = this.contextPath;
    if (pathPrefix != null && path.startsWith(pathPrefix)) {
      path = path.substring(pathPrefix.length());
      crumbPath += pathPrefix;
    }
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    final int queryIndex = path.indexOf('?');
    if (queryIndex != -1) {
      path = path.substring(0, queryIndex - 1);
    }
    final int fragmentIndex = path.indexOf('#');
    if (fragmentIndex != -1) {
      path = path.substring(0, fragmentIndex - 1);
    }
    path = path.replaceAll("//+", "/");
    path = path.replaceAll("/?index/?$", "");

    out.startTag(HtmlUtil.OL);
    out.attribute(HtmlUtil.ATTR_CLASS, "breadcrumb");
    if (path.length() == 0 || path.equals("index")) {
      out.startTag(HtmlUtil.LI);
      out.text("HOME");
      out.endTag(HtmlUtil.LI);
    } else {
      out.startTag(HtmlUtil.LI);
      HtmlUtil.serializeA(out, null, crumbPath + "/", "HOME");
      out.endTag(HtmlUtil.LI);
      final String[] segments = path.split("/");
      if (this.addSlash) {
        crumbPath += "/";
      }
      for (int i = 0; i < segments.length - 1; i++) {
        final String segment = segments[i];
        if (this.addSlash) {
          crumbPath += segment + "/";
        } else {
          crumbPath += "/" + segment;
        }
        out.startTag(HtmlUtil.LI);
        HtmlUtil.serializeA(out, null, crumbPath, CaseConverter.toCapitalizedWords(segment));
        out.endTag(HtmlUtil.LI);
      }
      final String segment = segments[segments.length - 1];
      out.startTag(HtmlUtil.LI);
      out.attribute(HtmlUtil.ATTR_CLASS, "active");
      out.text(CaseConverter.toCapitalizedWords(segment));
      out.endTag(HtmlUtil.LI);
    }

    out.endTag(HtmlUtil.OL);
  }
}
