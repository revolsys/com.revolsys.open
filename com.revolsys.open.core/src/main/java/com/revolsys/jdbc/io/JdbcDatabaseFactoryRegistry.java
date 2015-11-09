package com.revolsys.jdbc.io;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.WeakHashMap;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.record.io.RecordStoreFactoryRegistry;

public class JdbcDatabaseFactoryRegistry {
  private static Map<ApplicationContext, WeakReference<JdbcDatabaseFactoryRegistry>> factoriesByApplicationContext = new WeakHashMap<ApplicationContext, WeakReference<JdbcDatabaseFactoryRegistry>>();

  private static JdbcDatabaseFactoryRegistry instance;

  private static final Logger LOG = LoggerFactory.getLogger(JdbcDatabaseFactoryRegistry.class);

  public static void clearInstance() {
    instance = null;
  }

  public static List<JdbcDatabaseFactory> databaseFactories() {
    final JdbcDatabaseFactoryRegistry factory = databaseFactoryRegistry();
    return new ArrayList<>(factory.databaseFactoriesByProductName.values());
  }

  public static JdbcDatabaseFactory databaseFactory(final DataSource dataSource) {
    final JdbcDatabaseFactoryRegistry jdbcDatabaseFactoryRegistry = JdbcDatabaseFactoryRegistry
      .databaseFactoryRegistry();
    return jdbcDatabaseFactoryRegistry.getDatabaseFactory(dataSource);
  }

  public static JdbcDatabaseFactory databaseFactory(final Map<String, ? extends Object> config) {
    final JdbcDatabaseFactoryRegistry jdbcDatabaseFactoryRegistry = JdbcDatabaseFactoryRegistry
      .databaseFactoryRegistry();
    return jdbcDatabaseFactoryRegistry.getDatabaseFactory(config);
  }

  public static JdbcDatabaseFactory databaseFactory(final String productName) {
    final JdbcDatabaseFactoryRegistry jdbcDatabaseFactoryRegistry = JdbcDatabaseFactoryRegistry
      .databaseFactoryRegistry();
    return jdbcDatabaseFactoryRegistry.getDatabaseFactory(productName);
  }

  public synchronized static JdbcDatabaseFactoryRegistry databaseFactoryRegistry() {
    synchronized (JdbcDatabaseFactoryRegistry.class) {
      if (instance == null) {
        instance = new JdbcDatabaseFactoryRegistry();
        instance.loadServices();
      }
      return instance;
    }
  }

  public static JdbcDatabaseFactoryRegistry databaseFactoryRegistry(
    final ApplicationContext applicationContext) {
    if (applicationContext == null) {
      return databaseFactoryRegistry();
    } else {
      synchronized (factoriesByApplicationContext) {
        final WeakReference<JdbcDatabaseFactoryRegistry> reference = factoriesByApplicationContext
          .get(applicationContext);
        JdbcDatabaseFactoryRegistry jdbcDatabaseFactoryRegistry;
        if (reference == null) {
          jdbcDatabaseFactoryRegistry = null;
        } else {
          jdbcDatabaseFactoryRegistry = reference.get();
        }
        if (jdbcDatabaseFactoryRegistry == null) {
          jdbcDatabaseFactoryRegistry = new JdbcDatabaseFactoryRegistry();
          final ClassLoader classLoader = applicationContext.getClassLoader();
          jdbcDatabaseFactoryRegistry.loadFactories(classLoader);
          factoriesByApplicationContext.put(applicationContext,
            new WeakReference<JdbcDatabaseFactoryRegistry>(jdbcDatabaseFactoryRegistry));
        }
        return jdbcDatabaseFactoryRegistry;
      }
    }
  }

  private Map<String, JdbcDatabaseFactory> databaseFactoriesByProductName = new HashMap<String, JdbcDatabaseFactory>();

  private JdbcDatabaseFactoryRegistry() {
  }

  private void addFactory(final JdbcDatabaseFactory factory) {
    final String productName = factory.getProductName();
    this.databaseFactoriesByProductName.put(productName, factory);
  }

  @Override
  protected void finalize() throws Throwable {
    this.databaseFactoriesByProductName = null;
  }

  public JdbcDatabaseFactory getDatabaseFactory(final DataSource dataSource) {
    final String productName = JdbcUtils.getProductName(dataSource);
    return getDatabaseFactory(productName);
  }

  public JdbcDatabaseFactory getDatabaseFactory(final Map<String, ? extends Object> config) {
    final String url = (String)config.get("url");
    if (url == null) {
      throw new IllegalArgumentException("The url parameter must be specified");
    } else {
      for (final JdbcDatabaseFactory databaseFactory : databaseFactories()) {
        if (databaseFactory.canOpenUrl(url)) {
          return databaseFactory;
        }
      }
      throw new IllegalArgumentException("Data Source Factory not found for " + url);
    }
  }

  public JdbcDatabaseFactory getDatabaseFactory(final String productName) {
    final JdbcDatabaseFactory databaseFactory = this.databaseFactoriesByProductName
      .get(productName);
    if (databaseFactory == null) {
      throw new IllegalArgumentException("Record Store not found for " + productName);
    } else {
      return databaseFactory;
    }
  }

  private void loadFactories(final ClassLoader classLoader) {
    try {
      final ServiceLoader<JdbcDatabaseFactory> serviceLoader = ServiceLoader
        .load(JdbcDatabaseFactory.class, classLoader);
      for (final JdbcDatabaseFactory factory : serviceLoader) {
        addFactory(factory);
      }
    } catch (final Throwable e) {
      LOG.error("Unable to initialize JdbcDatabaseFactory", e);
    }

  }

  private void loadServices() {
    try {
      final ServiceLoader<JdbcDatabaseFactory> serviceLoader = ServiceLoader
        .load(JdbcDatabaseFactory.class);
      for (final JdbcDatabaseFactory factory : serviceLoader) {
        addFactory(factory);
        RecordStoreFactoryRegistry.register(factory);
      }
    } catch (final Throwable e) {
      LOG.error("Unable to initialize JdbcDatabaseFactory", e);
    }
  }
}
