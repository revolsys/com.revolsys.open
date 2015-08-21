package com.revolsys.ui.web.servlet.listener;

import java.beans.Introspector;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.log4j.Logger;
import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.beans.ClearCachedIntrospectionResults;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.web.util.Log4jWebConfigurer;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.jdbc.io.JdbcFactoryRegistry;
import com.revolsys.util.JavaBeanUtil;

public class ContextCleanupListener implements ServletContextListener {

  static void cleanupAttributes(final ServletContext servletContext) {
    @SuppressWarnings("rawtypes")
    final Enumeration attrNames = servletContext.getAttributeNames();
    while (attrNames.hasMoreElements()) {
      final String attrName = (String)attrNames.nextElement();
      if (attrName.startsWith("org.springframework.")) {
        final Object attrValue = servletContext.getAttribute(attrName);
        if (attrValue instanceof DisposableBean) {
          try {
            ((DisposableBean)attrValue).destroy();
          } catch (final Throwable e) {
            System.err
              .println("Couldn't invoke destroy method of attribute with name '" + attrName + "'");
          }
        } else {
          servletContext.removeAttribute(attrName);
        }
      }
    }
  }

  @Override
  public void contextDestroyed(final ServletContextEvent event) {
    final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    IoFactoryRegistry.clearInstance();
    JdbcFactoryRegistry.clearInstance();
    StringConverterRegistry.clearInstance();
    GeometryFactory.clear();
    EpsgCoordinateSystems.clear();
    cleanupAttributes(event.getServletContext());

    JavaBeanUtil.clearCache();
    BeanUtilsBean.setInstance(null);
    CachedIntrospectionResults.clearClassLoader(contextClassLoader);
    CachedIntrospectionResults.clearClassLoader(CachedIntrospectionResults.class.getClassLoader());
    CachedIntrospectionResults.clearClassLoader(ClassLoader.getSystemClassLoader());
    ClearCachedIntrospectionResults.clearCache();
    Introspector.flushCaches();
    Logger.getRootLogger().removeAllAppenders();
    Log4jWebConfigurer.shutdownLogging(event.getServletContext());
  }

  @Override
  public void contextInitialized(final ServletContextEvent event) {
    Log4jWebConfigurer.initLogging(event.getServletContext());
    CachedIntrospectionResults.acceptClassLoader(Thread.currentThread().getContextClassLoader());
  }
}
