package com.revolsys.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.spi.NamingManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

public class JndiObjectFactory extends AbstractFactoryBean<Object> {
  private static final Logger LOG = LoggerFactory.getLogger(JndiObjectFactory.class);

  private String beanName;

  private String jndiUrl;

  @Override
  protected Object createInstance() throws Exception {
    final Hashtable<Object, Object> initialEnvironment = new Hashtable<Object, Object>();
    final String initialFactory = System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
    final BeanFactory beanFactory = getBeanFactory();
    if (initialFactory != null) {
      initialEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, initialFactory);
    }
    Object savedObject;
    if (initialFactory == null
      && !NamingManager.hasInitialContextFactoryBuilder()) {
      final SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
      synchronized (beanName.intern()) {
        savedObject = beanFactory.getBean(beanName);
        builder.bind(jndiUrl, savedObject);
      }
    } else {
      Context context;
      if (NamingManager.hasInitialContextFactoryBuilder()) {
        context = NamingManager.getInitialContext(initialEnvironment);

      } else {
        context = new InitialContext();
      }
      try {
        savedObject = context.lookup(jndiUrl);
      } catch (final NameNotFoundException e) {
        synchronized (beanName.intern()) {
          savedObject = beanFactory.getBean(beanName);
          try {
            context.bind(jndiUrl, savedObject);
          } catch (final NameNotFoundException e2) {
          }
        }
      }
      if (savedObject == null) {
        synchronized (beanName.intern()) {
          LOG.error("JNDI data source was null " + jndiUrl);
          savedObject = beanFactory.getBean(beanName);
        }
      }
    }
    return null;
  }

  public String getBeanName() {
    return beanName;
  }

  public String getJndiUrl() {
    return jndiUrl;
  }

  @Override
  public Class<?> getObjectType() {
    return null;
  }

  public void setBeanName(final String beanName) {
    this.beanName = beanName;
  }

  public void setJndiUrl(final String jndiUrl) {
    this.jndiUrl = jndiUrl;
  }

}
