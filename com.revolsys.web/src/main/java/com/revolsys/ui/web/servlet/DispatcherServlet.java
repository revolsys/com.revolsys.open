package com.revolsys.ui.web.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.util.WebUtils;

public class DispatcherServlet extends
  org.springframework.web.servlet.DispatcherServlet {
  
   @Override
  public void init(
    ServletConfig config)
    throws ServletException {
    // TODO Auto-generated method stub
    super.init(config);
  }
  @Override
  protected void doService(
    HttpServletRequest request,
    HttpServletResponse response)
    throws Exception {
    super.doService(request, response);
    request.removeAttribute(WebUtils.INCLUDE_CONTEXT_PATH_ATTRIBUTE);
    request.removeAttribute(WebUtils.INCLUDE_PATH_INFO_ATTRIBUTE);
    request.removeAttribute(WebUtils.INCLUDE_QUERY_STRING_ATTRIBUTE);
    request.removeAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE);
    request.removeAttribute(WebUtils.INCLUDE_SERVLET_PATH_ATTRIBUTE);
  }
}
