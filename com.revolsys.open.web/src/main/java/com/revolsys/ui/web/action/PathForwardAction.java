package com.revolsys.ui.web.action;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.revolsys.ui.web.config.Action;

public class PathForwardAction implements Action {
  private String path;

  public void init(final ServletContext context) throws ServletException {
  }

  public void process(final HttpServletRequest request,
    final HttpServletResponse response) throws IOException, ServletException {
    RequestDispatcher requestDispatcher = request.getRequestDispatcher(path);
    Logger.getLogger(PathForwardAction.class).debug(path +'=' + requestDispatcher);
    if (requestDispatcher != null) {
      requestDispatcher.forward(request, response);
    }
  }

  /**
   * @return Returns the path.
   */
  public String getPath() {
    return path;
  }

  /**
   * @param path The path to set.
   */
  public void setPath(final String path) {
    this.path = path;
  }

}
