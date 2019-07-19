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
import com.revolsys.ui.web.controller.PathAliasController;
import com.revolsys.ui.web.utils.HttpServletUtils;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.Property;

public class PathBreadcrumbView extends Element {

  private boolean addSlash;

  private final String contextPath;

  private final String path;

  private boolean showHome;

  private String hidePrefix;

  public PathBreadcrumbView(final String contextPath, final String path) {
    this.contextPath = contextPath;
    this.path = path;
  }

  public PathBreadcrumbView(final String contextPath, final String path, final boolean addSlash,
    final boolean showHome, final String hidePrefix) {
    this.contextPath = contextPath;
    this.path = path;
    this.addSlash = addSlash;
    this.showHome = showHome;
    if (addSlash && Property.hasValue(hidePrefix)
      && hidePrefix.charAt(hidePrefix.length() - 1) != '/') {
      this.hidePrefix = hidePrefix + "/";
    } else {
      this.hidePrefix = hidePrefix;
    }
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    String path = this.path.substring(this.contextPath.length());
    final String pathPrefix = HttpServletUtils.getAttribute(PathAliasController.PATH_PREFIX);
    String baseCrumbPath = this.contextPath;
    if (pathPrefix != null && path.startsWith(pathPrefix)) {
      path = path.substring(pathPrefix.length());
      baseCrumbPath += pathPrefix;
    }
    String hidePrefix = this.hidePrefix;
    final boolean hasHidePrefix = Property.hasValue(hidePrefix);
    final int pathLength = path.length();
    if (hasHidePrefix) {
      if (path.startsWith(hidePrefix)) {
        final int hidePrefixLength = hidePrefix.length();
        if (pathLength == hidePrefixLength) {
          return;
        } else if (pathLength == hidePrefixLength + 1) {
          if (path.charAt(hidePrefixLength) == '/') {
            return;
          }
        }
      }
      hidePrefix = baseCrumbPath + hidePrefix;
    }
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    if (path.endsWith("/")) {
      path = path.substring(0, pathLength - 1);
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

    out.startTag(HtmlElem.OL);
    out.attribute(HtmlAttr.CLASS, "breadcrumb");
    if (pathLength == 0 || path.equals("index")) {
      out.startTag(HtmlElem.LI);
      out.text("HOME");
      out.endTag(HtmlElem.LI);
    } else {
      String crumbPath = baseCrumbPath;
      if (this.showHome) {
        out.startTag(HtmlElem.LI);
        HtmlUtil.serializeA(out, null, crumbPath + "/", "HOME");
        out.endTag(HtmlElem.LI);
      }
      if (this.addSlash) {
        crumbPath += "/";
      }
      final String[] segments = path.split("/");
      for (int i = 0; i < segments.length - 1; i++) {
        final String segment = segments[i];
        if (this.addSlash) {
          crumbPath += segment + "/";
        } else {
          crumbPath += "/" + segment;
        }
        if (!hasHidePrefix || !hidePrefix.startsWith(crumbPath)) {
          out.startTag(HtmlElem.LI);
          HtmlUtil.serializeA(out, null, crumbPath, CaseConverter.toCapitalizedWords(segment));
          out.endTag(HtmlElem.LI);
        }
      }
      final String segment = segments[segments.length - 1];
      out.startTag(HtmlElem.LI);
      out.attribute(HtmlAttr.CLASS, "active");
      out.text(CaseConverter.toCapitalizedWords(segment));
      out.endTag(HtmlElem.LI);
    }

    out.endTag(HtmlElem.OL);
  }
}
