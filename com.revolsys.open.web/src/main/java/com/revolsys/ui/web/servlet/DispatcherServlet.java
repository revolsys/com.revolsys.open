package com.revolsys.ui.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.util.WebUtils;

public class DispatcherServlet extends
  org.springframework.web.servlet.DispatcherServlet {
  private static final Logger LOG = LoggerFactory.getLogger(DispatcherServlet.class);

  @Override
  protected void doService(
    final HttpServletRequest request,
    final HttpServletResponse response) throws Exception {
    try {
      super.doService(request, response);
      request.removeAttribute(WebUtils.INCLUDE_CONTEXT_PATH_ATTRIBUTE);
      request.removeAttribute(WebUtils.INCLUDE_PATH_INFO_ATTRIBUTE);
      request.removeAttribute(WebUtils.INCLUDE_QUERY_STRING_ATTRIBUTE);
      request.removeAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE);
      request.removeAttribute(WebUtils.INCLUDE_SERVLET_PATH_ATTRIBUTE);
    } catch (AccessDeniedException e) {
      throw e;
    } catch (final Exception e) {
      LOG.error(e.getMessage(), e);
      throw e;
    }
  }
}
