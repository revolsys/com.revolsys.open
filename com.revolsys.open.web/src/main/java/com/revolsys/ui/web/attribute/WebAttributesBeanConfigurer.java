package com.revolsys.ui.web.attribute;

import java.util.Enumeration;

import javax.servlet.ServletContext;

import org.springframework.web.context.ServletContextAware;

import com.revolsys.spring.config.AttributesBeanConfigurer;

public class WebAttributesBeanConfigurer extends AttributesBeanConfigurer
  implements ServletContextAware {

  @SuppressWarnings("unchecked")
  @Override
  public void setServletContext(final ServletContext servletContext) {
    final Enumeration<String> names = servletContext.getInitParameterNames();
    while (names.hasMoreElements()) {
      final String name = names.nextElement();
      final String value = servletContext.getInitParameter(name);
      setAttribute(name, value);
    }

  }
}
