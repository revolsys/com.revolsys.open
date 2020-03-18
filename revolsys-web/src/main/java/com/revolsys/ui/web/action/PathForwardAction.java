package com.revolsys.ui.web.action;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;

import com.revolsys.ui.web.config.Action;

public class PathForwardAction implements Action {
  private String path;

  /**
   * @return Returns the path.
   */
  public String getPath() {
    return this.path;
  }

  @Override
  public void init(final ServletContext context) throws ServletException {
  }

  @Override
  public void process(final HttpServletRequest request, final HttpServletResponse response)
    throws IOException, ServletException {
    final RequestDispatcher requestDispatcher = request.getRequestDispatcher(this.path);
    LoggerFactory.getLogger(PathForwardAction.class).debug(this.path + '=' + requestDispatcher);
    if (requestDispatcher != null) {
      requestDispatcher.forward(request, response);
    }
  }

  /**
   * @param path The path to set.
   */
  public void setPath(final String path) {
    this.path = path;
  }

}
