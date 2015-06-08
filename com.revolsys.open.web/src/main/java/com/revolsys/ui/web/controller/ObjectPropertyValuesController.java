package com.revolsys.ui.web.controller;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.revolsys.data.io.DataAccessObject;

public class ObjectPropertyValuesController implements Controller {

  private DataAccessObject<?> dataAccessObject;

  private int maxResults = 25;

  public DataAccessObject<?> getDataAccessObject() {
    return this.dataAccessObject;
  }

  public int getMaxResults() {
    return this.maxResults;
  }

  @Override
  public ModelAndView handleRequest(final HttpServletRequest request,
    final HttpServletResponse response) throws Exception {
    final String path = request.getPathInfo();
    final int index = path.lastIndexOf('/');
    if (index != -1) {
      final String propertyName = path.substring(index + 1);
      final String query = request.getParameter("q");
      final String limit = request.getParameter("limit");
      int max = this.maxResults;
      if (limit != null) {
        try {
          max = Integer.parseInt(limit);
        } catch (final NumberFormatException e) {
        }
      }

      final Map<String, Object> where = new HashMap<String, Object>();
      where.put(propertyName, "%" + query + "%");
      final Map<String, Boolean> order = Collections.singletonMap(propertyName, true);
      final List<String> results = this.dataAccessObject.list(propertyName, where, order, max);
      response.setContentType("text/plain");
      final PrintWriter writer = response.getWriter();
      for (final String result : results) {
        writer.println(result);
      }
    }
    return null;
  }

  public void setDataAccessObject(final DataAccessObject<?> dataAccessObject) {
    this.dataAccessObject = dataAccessObject;
  }

  public void setMaxResults(final int maxResults) {
    this.maxResults = maxResults;
  }
}
