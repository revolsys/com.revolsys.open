package com.revolsys.ui.web.rest.interceptor;

import java.util.function.BiFunction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeometry.common.data.type.DataType;

public interface WebParameterHandler {
  static WebParameterHandler fixed(final Object value) {
    return (request, response) -> {
      return value;
    };
  }

  static WebParameterHandler function(final String name,
    final BiFunction<HttpServletRequest, HttpServletResponse, Object> function,
    final DataType dataType, final boolean required, final Object defaultValue) {
    if (defaultValue == null) {
      if (required) {
        return (request, response) -> {
          final Object value = function.apply(request, response);
          if (value == null) {
            throw new IllegalArgumentException(name + " is required.");
          } else {
            return dataType.toObject(value);
          }
        };
      } else {
        return (request, response) -> {
          final Object value = function.apply(request, response);
          return dataType.toObject(value);
        };
      }
    } else {
      return (request, response) -> {
        final Object value = function.apply(request, response);
        if (value == null) {
          return defaultValue;
        } else {
          return dataType.toObject(value);
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
