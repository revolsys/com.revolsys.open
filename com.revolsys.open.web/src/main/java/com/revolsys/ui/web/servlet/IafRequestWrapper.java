package com.revolsys.ui.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class IafRequestWrapper extends HttpServletRequestWrapper {

  public IafRequestWrapper(final HttpServletRequest request) {
    super(request);
  }

}
