package com.revolsys.ui.web.controller;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.util.WebUtils;

import com.revolsys.ui.web.exception.PageNotFoundException;
import com.revolsys.ui.web.utils.HttpServletUtils;
import com.revolsys.util.Property;

public class PathAliasController implements Controller {
  public static final String PATH_PREFIX = PathAliasController.class.getName() + ".originalPrefix";

  public static boolean forward(final HttpServletRequest request,
    final HttpServletResponse response, final String path) throws IOException, ServletException {
    final RequestDispatcher requestDispatcher = request.getRequestDispatcher(path);
    if (requestDispatcher == null) {
      return false;
    } else {
      final HttpServletRequest wrappedRequest;
      if (request instanceof DefaultMultipartHttpServletRequest) {
        final DefaultMultipartHttpServletRequest multiPartRequest = (DefaultMultipartHttpServletRequest)request;
        wrappedRequest = new DefaultMultipartHttpServletRequest(multiPartRequest,
          multiPartRequest.getMultiFileMap(), new HashMap<String, String[]>(),
          new HashMap<String, String>()) {
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
      final Object forwardPath = request.getAttribute(WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE);
      if (forwardPath == null) {
        final String originalUri = request.getRequestURI();
        wrappedRequest.setAttribute(WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE, originalUri);
      }

      requestDispatcher.forward(wrappedRequest, response);
      wrappedRequest.setAttribute(WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE, forwardPath);
    }
    return true;
  }

  public static String getAlias() {
    return (String)HttpServletUtils.getAttribute(PATH_PREFIX);
  }

  public static String getOriginalPrefix() {
    final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    final String prefix = (String)requestAttributes.getAttribute(PATH_PREFIX,
      RequestAttributes.SCOPE_REQUEST);
    if (Property.hasValue(prefix)) {
      return prefix;
    } else {
      return "";
    }
  }

  public static String getPath(final String path) {
    if (path == null) {
      return path;
    } else if (path.startsWith("/")) {
      final String prefix = getOriginalPrefix();
      if (prefix.length() > 0) {
        return prefix + path;
      } else {
        return path;
      }
    } else {
      return path;
    }
  }

  private String aliasPrefix;

  private String prefix;

  public String getAliasPrefix() {
    return this.aliasPrefix;
  }

  public String getPrefix() {
    return this.prefix;
  }

  @Override
  public ModelAndView handleRequest(final HttpServletRequest request,
    final HttpServletResponse response) throws Exception {
    String path = request.getServletPath() + request.getPathInfo();
    if (path.startsWith(this.prefix)) {
      if (getOriginalPrefix().length() == 0) {
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        requestAttributes.setAttribute(PATH_PREFIX,
          this.prefix.replaceAll(this.aliasPrefix + "$", ""), RequestAttributes.SCOPE_REQUEST);
      }
      path = path.replaceFirst(this.prefix, this.aliasPrefix);
      if (!forward(request, response, path)) {
        throw new PageNotFoundException();
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
