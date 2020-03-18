package com.revolsys.ui.web.exception;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class RsExceptionResolver implements HandlerExceptionResolver {

  @Override
  public ModelAndView resolveException(final HttpServletRequest request,
    final HttpServletResponse response, final Object handler, final Exception exception) {
    try {
      if (exception instanceof PageNotFoundException) {
        final PageNotFoundException pageNotFound = (PageNotFoundException)exception;
        response.sendError(HttpServletResponse.SC_NOT_FOUND, pageNotFound.getMessage());
        return new ModelAndView();
      } else if (exception instanceof IllegalArgumentException) {
        final IllegalArgumentException illegalArgument = (IllegalArgumentException)exception;
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, illegalArgument.getMessage());
        return new ModelAndView();
      } else if (exception instanceof RedirectException) {
        final RedirectException redirect = (RedirectException)exception;
        response.sendRedirect(redirect.getUrl());
        return new ModelAndView();
      } else if (exception instanceof MultipartException) {
        final MultipartException multiPartException = (MultipartException)exception;
        Throwable cause = multiPartException.getCause();
        if (cause == null) {
          cause = exception;
        }
        response.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "Invalid HTTP multi-part request: " + cause.getMessage());
        return new ModelAndView();
      }
    } catch (final IOException e) {
    }
    return null;
  }
}
