package com.revolsys.ui.web.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.revolsys.ui.web.config.ActionConfig;
import com.revolsys.ui.web.config.IafAction;
import com.revolsys.ui.web.exception.ActionException;
import com.revolsys.ui.web.exception.ActionInitException;

public class PathRedirect extends IafAction {
  private String basePath;

  private String path;

  @Override
  public void init(final ActionConfig config) throws ActionInitException {
    super.init(config);
    this.path = (String)config.getParameter("path");
    this.basePath = getConfig().getConfig().getBasePath();

  }

  @Override
  public void process(final HttpServletRequest request, final HttpServletResponse response)
    throws ActionException, IOException {
    response.sendRedirect(this.basePath + this.path);
  }

}
