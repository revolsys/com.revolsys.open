package com.revolsys.ui.web.controller;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

public class PathAliasController implements Controller {

  private static final Logger LOG = LoggerFactory.getLogger(PathAliasController.class);

  public static boolean forward(final HttpServletRequest request,
    final HttpServletResponse response, final String path) throws IOException,
    ServletException {
    try {
      final RequestDispatcher requestDispatcher = request.getRequestDispatcher(path);
      if (requestDispatcher == null) {
        return false;
      } else {
        final HttpServletRequest wrappedRequest;
        if (request instanceof DefaultMultipartHttpServletRequest) {
          DefaultMultipartHttpServletRequest multiPartRequest = (DefaultMultipartHttpServletRequest)request;
          wrappedRequest = new DefaultMultipartHttpServletRequest(
            multiPartRequest, multiPartRequest.getMultiFileMap(),
            new HashMap<String, String[]>()) {
            @Override
            public String getPathInfo() {
              return path;
            }
          };
        } else {
          wrappedRequest = new HttpServletRequestWrapper(request) {
            @Override
            public String getPathInfo() {
              return path;
            }
          };
        }

        requestDispatcher.forward(wrappedRequest, response);
      }
    } catch (final ServletException e) {
      LOG.error("Unable to include path " + path, e);
    }
    return true;
  }

  private String prefix;

  private String aliasPrefix;

  public String getAliasPrefix() {
    return aliasPrefix;
  }

  public String getPrefix() {
    return prefix;
  }

  public ModelAndView handleRequest(final HttpServletRequest request,
    final HttpServletResponse response) throws Exception {
    String path = request.getServletPath() + request.getPathInfo();
    if (path.startsWith(prefix)) {
      path = path.replaceFirst(prefix, aliasPrefix);
      if (forward(request, response, path)) {
        return null;
      } else {
        throw new NoSuchRequestHandlingMethodException(request);
      }
    }
    return null;
  }

  public void setAliasPrefix(final String aliasPrefix) {
    this.aliasPrefix = aliasPrefix;
  }

  public void setPrefix(final String prefix) {
    this.prefix = prefix;
  }

}
