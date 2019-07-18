/*
 Copyright 2009 Revolution Systems Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 $URL$
 $Author$
 $Date$
 $Revision$
 */
package com.revolsys.ui.web.controller;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class PathViewController implements Controller {

  public static boolean include(final HttpServletRequest request,
    final HttpServletResponse response, final Object object) throws IOException, ServletException {
    if (object == null) {
      return false;
    } else {
      try {
        final String path = object.toString();
        final RequestDispatcher requestDispatcher = request.getRequestDispatcher(path);
        if (requestDispatcher == null) {
          return false;
        } else {
          requestDispatcher.include(new HttpServletRequestWrapper(request) {
            @Override
            public String getPathInfo() {
              return path;
            }
          }, response);
        }
      } catch (final ServletException e) {
        e.printStackTrace();
      }
      return true;
    }
  }

  @Override
  public ModelAndView handleRequest(final HttpServletRequest request,
    final HttpServletResponse response) throws Exception {
    final String attributeName = request.getParameter("attributeName");
    if (attributeName != null) {
      final Object attribute = request.getAttribute(attributeName);
      if (attribute instanceof Collection) {
        final Collection collection = (Collection)attribute;
        for (final Object object : collection) {
          include(request, response, object);
        }
      } else {
        include(request, response, attribute);
      }
    }
    return null;
  }

}
