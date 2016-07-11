package com.revolsys.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;

import org.springframework.util.ClassUtils;

import com.revolsys.logging.Logs;

public class SimpleNamingContextBuilder implements InitialContextFactoryBuilder {

  private static volatile SimpleNamingContextBuilder activated;

  private static final Object initializationLock = new Object();

  private static boolean initialized = false;

  public static SimpleNamingContextBuilder emptyActivatedContextBuilder() throws NamingException {
    if (activated != null) {
      activated.clear();
    } else {
      final SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
      builder.activate();
    }
    return activated;
  }

  public static SimpleNamingContextBuilder getCurrentContextBuilder() {
    return activated;
  }

  private final Hashtable<String, Object> boundObjects = new Hashtable<>();

  public SimpleNamingContextBuilder() {
  }

  public void activate() throws IllegalStateException, NamingException {
    Logs.info(this, "Activating simple JNDI environment");
    synchronized (initializationLock) {
      if (!initialized) {
        if (NamingManager.hasInitialContextFactoryBuilder()) {
          throw new IllegalStateException(
            "Cannot activate SimpleNamingContextBuilder: there is already a JNDI provider registered. Note that JNDI is a JVM-wide service, shared at the JVM system class loader level, with no reset option. As a consequence, a JNDI provider must only be registered once per JVM.");
        }
        NamingManager.setInitialContextFactoryBuilder(this);
        initialized = true;
      }
    }
    activated = this;
  }

  public void bind(final String name, final Object obj) {
    Logs.info(this, new StringBuilder("Static JNDI binding: [").append(name)
      .append("] = [")
      .append(obj)
      .append("]")
      .toString());
    this.boundObjects.put(name, obj);
  }

  public void clear() {
    this.boundObjects.clear();
  }

  @Override
  public InitialContextFactory createInitialContextFactory(final Hashtable environment) {
    if (activated == null && environment != null) {
      final Object icf = environment.get("java.naming.factory.initial");
      if (icf != null) {
        Class<?> icfClass = null;
        if (icf instanceof Class<?>) {
          icfClass = (Class<?>)icf;
        } else if (icf instanceof String) {
          icfClass = ClassUtils.resolveClassName((String)icf, getClass().getClassLoader());
        } else {
          throw new IllegalArgumentException(new StringBuilder(
            "Invalid value type for environment key [java.naming.factory.initial]: ")
              .append(icf.getClass().getName()).toString());
        }
        if (!InitialContextFactory.class.isAssignableFrom(icfClass)) {
          throw new IllegalArgumentException(
            new StringBuilder("Specified class does not implement [")
              .append(InitialContextFactory.class.getName()).append("]: ").append(icf).toString());
        }
        try {
          return (InitialContextFactory)icfClass.newInstance();
        } catch (final Throwable ex) {
          final IllegalStateException ise = new IllegalStateException(
            new StringBuilder("Cannot instantiate specified InitialContextFactory: ").append(icf)
              .toString());
          ise.initCause(ex);
          throw ise;
        }
      }
    }
    return new InitialContextFactory() {

      @Override
      public Context getInitialContext(final Hashtable environment) {
        return new SimpleNamingContext("", SimpleNamingContextBuilder.this.boundObjects,
          environment);
      }
    };
  }

  public void deactivate() {
    Logs.info(this, "Deactivating simple JNDI environment");
    activated = null;
  }

}

// Messages from Jad:
// Overlapped try statements detected. Not all exception handlers will be
// resolved in the method activate
//
