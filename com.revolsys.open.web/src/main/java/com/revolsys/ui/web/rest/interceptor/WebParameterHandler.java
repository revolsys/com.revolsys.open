package com.revolsys.ui.web.rest.interceptor;

import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.revolsys.util.function.Function2;

public interface WebParameterHandler {
  static WebParameterHandler fixed(final Object value) {
    return (request, response) -> {
      return value;
    };
  }

  static WebParameterHandler function(final String name,
    final Function2<HttpServletRequest, HttpServletResponse, Object> function,
    final Function<Object, Object> converter, final boolean required, final Object defaultValue) {
    if (defaultValue == null) {
      if (required) {
        return (request, response) -> {
          final Object value = function.apply(request, response);
          if (value == null) {
            throw new IllegalArgumentException(name + " is required.");
          } else {
            return converter.apply(value);
          }
        };
      } else {
        return (request, response) -> {
          final Object value = function.apply(request, response);
          return converter.apply(value);
        };
      }
    } else {
      return (request, response) -> {
        final Object value = function.apply(request, response);
        if (value == null) {
          return defaultValue;
        } else {
          return converter.apply(value);
        }
      };
    }
  }

  static WebParameterHandler request() {
    return (request, response) -> {
      return request;
    };
  }

  static WebParameterHandler response() {
    return (request, response) -> {
      return response;
    };
  }

  Object getParameter(HttpServletRequest request, HttpServletResponse response);
}
