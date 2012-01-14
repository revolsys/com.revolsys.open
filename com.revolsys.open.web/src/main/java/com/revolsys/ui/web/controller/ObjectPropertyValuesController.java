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

import com.revolsys.gis.data.io.DataAccessObject;

public class ObjectPropertyValuesController implements Controller {

  private DataAccessObject<?> dataAccessObject;

  private int maxResults = 25;

  public ModelAndView handleRequest(final HttpServletRequest request,
    final HttpServletResponse response) throws Exception {
    String path = request.getPathInfo();
    int index = path.lastIndexOf('/');
    if (index != -1) {
      String propertyName = path.substring(index + 1);
      String query = request.getParameter("q");
      String limit = request.getParameter("limit");
      int max = maxResults;
      if (limit != null) {
        try {
          max = Integer.parseInt(limit);
        } catch (NumberFormatException e) {
        }
      }

      Map<String, Object> where = new HashMap<String, Object>();
      where.put(propertyName, "%" + query + "%");
      Map<String, Boolean> order = Collections.singletonMap(propertyName, true);
      List<String> results = dataAccessObject.list(propertyName, where, order,
        max);
      response.setContentType("text/plain");
      PrintWriter writer = response.getWriter();
      for (String result : results) {
        writer.println(result);
      }
    }
    return null;
  }

  public DataAccessObject<?> getDataAccessObject() {
    return dataAccessObject;
  }

  public void setDataAccessObject(final DataAccessObject<?> dataAccessObject) {
    this.dataAccessObject = dataAccessObject;
  }

  public int getMaxResults() {
    return maxResults;
  }

  public void setMaxResults(int maxResults) {
    this.maxResults = maxResults;
  }
}
