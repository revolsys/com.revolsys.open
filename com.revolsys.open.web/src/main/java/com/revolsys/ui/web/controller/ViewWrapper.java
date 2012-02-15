package com.revolsys.ui.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class ViewWrapper implements Controller {
  private String viewName = "jspView";

  private String attributeName = "body";

  private String prefix = "/jsp";

  private String suffix = ".jsp";

  public String getAttributeName() {
    return attributeName;
  }

  public String getPrefix() {
    return prefix;
  }

  public String getSuffix() {
    return suffix;
  }

  public String getViewName() {
    return viewName;
  }

  public ModelAndView handleRequest(
    final HttpServletRequest request,
    final HttpServletResponse response) throws Exception {
    String path = request.getPathInfo();
    if ("/".equals(path)) {
      path = "/index";
    }
    final ModelAndView view = new ModelAndView(viewName);

    view.addObject(attributeName, prefix + path + suffix);
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
