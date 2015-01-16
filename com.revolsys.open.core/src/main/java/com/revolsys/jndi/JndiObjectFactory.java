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
      synchronized (this.beanName.intern()) {
        savedObject = beanFactory.getBean(this.beanName);
        builder.bind(this.jndiUrl, savedObject);
      }
    } else {
      Context context;
      if (NamingManager.hasInitialContextFactoryBuilder()) {
        context = NamingManager.getInitialContext(initialEnvironment);

      } else {
        context = new InitialContext();
      }
      try {
        savedObject = context.lookup(this.jndiUrl);
      } catch (final NameNotFoundException e) {
        synchronized (this.beanName.intern()) {
          savedObject = beanFactory.getBean(this.beanName);
          try {
            context.bind(this.jndiUrl, savedObject);
          } catch (final NameNotFoundException e2) {
          }
        }
      }
      if (savedObject == null) {
        synchronized (this.beanName.intern()) {
          LOG.error("JNDI data source was null " + this.jndiUrl);
          savedObject = beanFactory.getBean(this.beanName);
        }
      }
    }
    return null;
  }

  public String getBeanName() {
    return this.beanName;
  }

  public String getJndiUrl() {
    return this.jndiUrl;
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
