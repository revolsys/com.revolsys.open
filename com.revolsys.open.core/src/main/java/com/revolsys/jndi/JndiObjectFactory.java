package com.revolsys.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.spi.NamingManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;

public class JndiObjectFactory implements BeanFactoryAware, FactoryBean {
  private static final Logger LOG = LoggerFactory.getLogger(JndiObjectFactory.class);

  private BeanFactory beanFactory;

  private String beanName;

  private String jndiUrl;

  private Object savedObject;

  public String getBeanName() {
    return beanName;
  }

  public String getJndiUrl() {
    return jndiUrl;
  }

  public Object getObject() throws Exception {
    if (savedObject == null) {
      final Hashtable<Object, Object> initialEnvironment = new Hashtable<Object, Object>();
      final String initialFactory = System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
      if (initialFactory != null) {
        initialEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, initialFactory);
      }
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
    }
    return savedObject;
  }

  public Class getObjectType() {
    return null;
  }

  public boolean isSingleton() {
    return true;
  }

  public void setBeanFactory(final BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  public void setBeanName(final String beanName) {
    this.beanName = beanName;
  }

  public void setJndiUrl(final String jndiUrl) {
    this.jndiUrl = jndiUrl;
  }

}
