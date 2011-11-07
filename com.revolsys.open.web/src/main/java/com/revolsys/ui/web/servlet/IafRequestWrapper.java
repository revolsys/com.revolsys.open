package com.revolsys.ui.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class IafRequestWrapper extends HttpServletRequestWrapper {

  public IafRequestWrapper(HttpServletRequest request) {
    super(request);
  }

}
