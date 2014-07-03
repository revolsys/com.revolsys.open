package com.revolsys.data.io;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

public class DelegatingDataObjectStoreHandler implements InvocationHandler {
  public static <T extends DataObjectStore> T create(final String label,
    final Class<T> interfaceClass, final T dataStore) {
    final ClassLoader classLoader = dataStore.getClass().getClassLoader();
    final Class<?>[] interfaces = new Class<?>[] {
      interfaceClass
    };
    final DelegatingDataObjectStoreHandler handler = new DelegatingDataObjectStoreHandler(
      label, dataStore);
    final T proxyStore = (T)Proxy.newProxyInstance(classLoader, interfaces,
      handler);
    return proxyStore;

  }

  @SuppressWarnings("unchecked")
  public static <T extends DataObjectStore> T create(final String label,
    final Map<String, ? extends Object> config) {
    final ClassLoader classLoader = Thread.currentThread()
      .getContextClassLoader();
    final Class<?>[] interfaces = new Class<?>[] {
      DataObjectStoreFactoryRegistry.getDataObjectStoreInterfaceClass(config)
    };
    final DelegatingDataObjectStoreHandler handler = new DelegatingDataObjectStoreHandler(
      label, config);
    final T proxyStore = (T)Proxy.newProxyInstance(classLoader, interfaces,
      handler);
    try {
      proxyStore.initialize();
    } catch (final Throwable t) {
      LoggerFactory.getLogger(DelegatingDataObjectStoreHandler.class).error(
        "Unable to initialize data store " + label, t);
    }
    return proxyStore;
  }

  private Map<String, Object> config;

  private DataObjectStore dataStore;

  private String label;

  public DelegatingDataObjectStoreHandler() {
  }

  public DelegatingDataObjectStoreHandler(final String label,
    final DataObjectStore dataStore) {
    this.label = label;
    this.dataStore = dataStore;
  }

  public DelegatingDataObjectStoreHandler(final String label,
    final Map<String, ? extends Object> config) {
    this.label = label;
    this.config = new HashMap<String, Object>(config);
  }

  protected DataObjectStore createDataStore() {
    if (config != null) {
      final DataObjectStore dataStore = DataObjectStoreFactoryRegistry.createDataObjectStore(config);
      return dataStore;
    } else {
      throw new UnsupportedOperationException("Data store must be set manually");
    }
  }

  public DataObjectStore getDataStore() {
    if (dataStore == null) {
      dataStore = createDataStore();
      dataStore.initialize();
    }
    return dataStore;
  }

  @Override
  public Object invoke(final Object proxy, final Method method,
    final Object[] args) throws Throwable {
    int numArgs;
    if (args == null) {
      numArgs = 0;
    } else {
      numArgs = args.length;
    }
    if (method.getName().equals("toString") && numArgs == 0) {
      return label;
    } else if (method.getName().equals("getLabel") && numArgs == 0) {
      return label;
    } else if (method.getName().equals("hashCode") && numArgs == 0) {
      return label.hashCode();
    } else if (method.getName().equals("equals") && numArgs == 1) {
      final boolean equal = args[0] == proxy;
      return equal;
    } else if (method.getName().equals("close") && numArgs == 0) {
      if (dataStore != null) {
        final DataObjectStore dataStore = getDataStore();

        dataStore.close();
        this.dataStore = null;
      }
      return null;
    } else {
      final DataObjectStore dataStore = getDataStore();
      return method.invoke(dataStore, args);
    }
  }
}
