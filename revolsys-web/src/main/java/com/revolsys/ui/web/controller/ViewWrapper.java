package com.revolsys.ui.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class ViewWrapper implements Controller {
  private String attributeName = "body";

  private String prefix = "/jsp";

  private String suffix = ".jsp";

  private String viewName = "jspView";

  public String getFieldName() {
    return this.attributeName;
  }

  public String getPrefix() {
    return this.prefix;
  }

  public String getSuffix() {
    return this.suffix;
  }

  public String getViewName() {
    return this.viewName;
  }

  @Override
  public ModelAndView handleRequest(final HttpServletRequest request,
    final HttpServletResponse response) throws Exception {
    String path = request.getPathInfo();
    if ("/".equals(path)) {
      path = "/index";
    }
    final ModelAndView view = new ModelAndView(this.viewName);

    view.addObject(this.attributeName, this.prefix + path + this.suffix);
    return view;
  }

  public void setAttributeName(final String attributeName) {
    this.attributeName = attributeName;
  }

  public void setPrefix(final String prefix) {
    this.prefix = prefix;
  }

  public void setSuffix(final String suffix) {
    this.suffix = suffix;
  }

  public void setViewName(final String viewName) {
    this.viewName = viewName;
  }

}
