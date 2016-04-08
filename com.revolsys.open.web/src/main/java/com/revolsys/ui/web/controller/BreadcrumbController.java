/*
 Copyright 2009 Revolution Systems Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 $URL$
 $Author$
 $Date$
 $Revision$
 */
package com.revolsys.ui.web.controller;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.util.UrlPathHelper;

import com.revolsys.ui.html.view.PathBreadcrumbView;
import com.revolsys.util.Booleans;

public class BreadcrumbController implements Controller {

  private boolean addSlash;

  private final UrlPathHelper urlPathHelper = new UrlPathHelper();

  public BreadcrumbController() {
    this.urlPathHelper.setAlwaysUseFullPath(true);
  }

  @Override
  public ModelAndView handleRequest(final HttpServletRequest request,
    final HttpServletResponse response) throws Exception {
    final Boolean showHome = Booleans.getBoolean(request.getAttribute("breadcrumbShowHome"));
    final String hidePrefix = (String)request.getAttribute("breadcrumbHidePrefix");
    if (!Booleans.isTrue(request.getAttribute("breadcrumbHide"))) {
      final String path = this.urlPathHelper.getOriginatingRequestUri(request);
      final String contextPath = this.urlPathHelper.getOriginatingContextPath(request);
      final PathBreadcrumbView view = new PathBreadcrumbView(contextPath, path, this.addSlash,
        showHome != Boolean.FALSE, hidePrefix);
      final PrintWriter writer = response.getWriter();
      view.serialize(writer, false);
      writer.flush();
    }
    return null;
  }

  public boolean isAddSlash() {
    return this.addSlash;
  }

  public void setAddSlash(final boolean addSlash) {
    this.addSlash = addSlash;
  }

}
