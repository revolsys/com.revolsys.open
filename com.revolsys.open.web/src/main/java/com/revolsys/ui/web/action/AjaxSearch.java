package com.revolsys.ui.web.action;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import com.revolsys.gis.data.io.DataAccessObject;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.orm.core.SpringDaoFactory;
import com.revolsys.ui.web.config.ActionConfig;
import com.revolsys.ui.web.exception.ActionException;
import com.revolsys.ui.web.exception.ActionInitException;
import com.revolsys.util.JavaBeanUtil;

public class AjaxSearch extends SpringFrameworkAction {
  private static final Logger log = Logger.getLogger(AjaxSearch.class);

  private static final QName UL = new QName("ul");

  private static final QName LI = new QName("li");

  private DataAccessObject dao;

  private Method searchMethod;

  private List parameters;

  @Override
  public void init(final ActionConfig actionConfig) throws ActionInitException {
    super.init(actionConfig);
    final String className = actionConfig.getStringParameter("objectClassName");
    String queryName = actionConfig.getStringParameter("queryName");
    if (queryName == null) {
      queryName = "LikeName";
    }
    final String queryParams = actionConfig.getStringParameter("queryParams");
    if (queryParams != null) {
      parameters = Arrays.asList(queryParams.split(","));
    } else {
      parameters = Collections.singletonList("name");
    }
    try {
      final Class domainClass = Class.forName(className);
      dao = SpringDaoFactory.get(getApplicationContext(), domainClass);
      final Method[] methods = dao.getClass().getMethods();
      for (int i = 0; i < methods.length; i++) {
        final Method method = methods[i];
        if (method.getName().equals("iterate" + queryName)) {
          searchMethod = method;
        }
      }
      if (searchMethod == null) {
        throw new ActionInitException(dao + " does not have method iterate "
          + queryName);
      }
    } catch (final ClassNotFoundException e) {
      throw new ActionInitException("Domain Class " + className
        + " does not exist", e);
    }
  }

  @Override
  public void process(
    final HttpServletRequest request,
    final HttpServletResponse response) throws ActionException, IOException {
    try {
      final Object[] params = new Object[parameters.size()];
      for (int i = 0; i < params.length; i++) {
        final String parameterName = (String)parameters.get(i);
        Object value = request.getAttribute(parameterName);
        if (value == null) {
          value = request.getParameter(parameterName);
        }
        if (value == null) {
          value = "%";
        } else if (value instanceof String) {

          final String stringValue = (String)value;
          value = '%' + stringValue + "%";
        }
        params[i] = value;
      }

      final Iterator results = (Iterator)searchMethod.invoke(dao, params);
      response.setContentType("text/html");
      final XmlWriter out = new XmlWriter(response.getOutputStream());
      out.startDocument();
      out.startTag(UL);
      while (results.hasNext()) {
        final Object object = results.next();
        out.element(UL, JavaBeanUtil.getProperty(object, "name"));
      }
      out.endTag(UL);
      out.endDocument();
      out.flush();
    } catch (final Throwable t) {
      log.error(t.getMessage(), t);
    }
  }

}
