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

  public ModelAndView handleRequest(final HttpServletRequest request,
    final HttpServletResponse response) throws Exception {
    String attributeName = request.getParameter("attributeName");
    if (attributeName != null) {
      Object attribute = request.getAttribute(attributeName);
      if (attribute instanceof Collection) {
        Collection collection = (Collection)attribute;
        for (Object object : collection) {
          include(request, response, object);
        }
      } else {
        include(request, response, attribute);
      }
    }
    return null;
  }

  private void include(final HttpServletRequest request,
    final HttpServletResponse response, final Object object)
    throws IOException, ServletException {
    if (object != null) {
      try {
        final String path = object.toString();
        RequestDispatcher requestDispatcher = request.getRequestDispatcher(path);
        
        requestDispatcher.include(new HttpServletRequestWrapper(request) {
          public String getPathInfo() {
            return path;
          }
        }, response);
      } catch (ServletException e) {
        e.printStackTrace();
      }
    }
  }

}
