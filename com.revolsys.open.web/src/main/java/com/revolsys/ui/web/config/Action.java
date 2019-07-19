package com.revolsys.ui.web.config;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Action {
  void init(ServletContext context) throws ServletException;

  void process(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException;
}
