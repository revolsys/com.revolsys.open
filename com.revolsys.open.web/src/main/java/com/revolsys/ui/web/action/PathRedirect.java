package com.revolsys.ui.web.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.revolsys.ui.web.config.ActionConfig;
import com.revolsys.ui.web.config.IafAction;
import com.revolsys.ui.web.exception.ActionException;
import com.revolsys.ui.web.exception.ActionInitException;

public class PathRedirect extends IafAction {
  private String path;

  private String basePath;

  public void init(final ActionConfig config) throws ActionInitException {
    super.init(config);
    path = (String)config.getParameter("path");
    basePath = getConfig().getConfig().getBasePath();

  }

  public void process(final HttpServletRequest request,
    final HttpServletResponse response) throws ActionException, IOException {
    response.sendRedirect(basePath + path);
  }

}
