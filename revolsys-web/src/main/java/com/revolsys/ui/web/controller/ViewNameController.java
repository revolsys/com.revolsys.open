package com.revolsys.ui.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class ViewNameController implements Controller {

  private String viewName;

  public String getViewName() {
    return this.viewName;
  }

  @Override
  public ModelAndView handleRequest(final HttpServletRequest request,
    final HttpServletResponse response) throws Exception {
    return new ModelAndView(this.viewName);
  }

  public void setViewName(final String viewName) {
    this.viewName = viewName;
  }

}
