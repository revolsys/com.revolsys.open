package com.revolsys.ui.web.servlet.listener;

import java.beans.Introspector;
import java.lang.management.ManagementFactory;
import java.util.Enumeration;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.log4j.Logger;
import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.beans.ClearCachedIntrospectionResults;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.web.util.Log4jWebConfigurer;

import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Property;

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
    GeometryFactory.clear();
    EpsgCoordinateSystems.clear();
    cleanupAttributes(event.getServletContext());

    BeanUtilsBean.setInstance(null);
    CachedIntrospectionResults.clearClassLoader(contextClassLoader);
    CachedIntrospectionResults.clearClassLoader(CachedIntrospectionResults.class.getClassLoader());
    CachedIntrospectionResults.clearClassLoader(ClassLoader.getSystemClassLoader());
    Introspector.flushCaches();
    ClearCachedIntrospectionResults.clearCache();
    Property.clearCache();
    Logger.getRootLogger().removeAllAppenders();
    Log4jWebConfigurer.shutdownLogging(event.getServletContext());
  }

  @Override
  public void contextInitialized(final ServletContextEvent event) {
    Log4jWebConfigurer.initLogging(event.getServletContext());
    CachedIntrospectionResults.acceptClassLoader(Thread.currentThread().getContextClassLoader());
  }

  protected boolean isWebAppClassLoaderOrChild(ClassLoader classLoader) {
    final ClassLoader webAppClassLoader = getClass().getClassLoader();

    while (classLoader != null) {
      if (classLoader == webAppClassLoader) {
        return true;
      }

      classLoader = classLoader.getParent();
    }

    return false;
  }

  /** Unregister MBeans loaded by the web application class loader */
  protected void unregisterMBeans() {
    try {
      final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
      final Set<ObjectName> allMBeanNames = mBeanServer.queryNames(new ObjectName("*:*"), null);

      for (final ObjectName mbeanName : allMBeanNames) {
        try {

          final ClassLoader mBeanClassLoader = mBeanServer.getClassLoaderFor(mbeanName);
          if (isWebAppClassLoaderOrChild(mBeanClassLoader)) {
            mBeanServer.unregisterMBean(mbeanName);
          }
        } catch (final Throwable e) {
          Exceptions.error(this, "Unable to deregister MBean" + mbeanName, e);
        }
      }
    } catch (final Throwable e) {
      Exceptions.error(this, "Unable to deregister MBeans", e);
    }
  }
}
