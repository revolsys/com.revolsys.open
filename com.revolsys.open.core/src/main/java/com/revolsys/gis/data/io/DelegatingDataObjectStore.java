package com.revolsys.gis.data.io;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class DelegatingDataObjectStore implements InvocationHandler {
  public static <T extends DataObjectStore> T create(final String label,
    final Class<T> interfaceClass, final T dataStore) {
    final ClassLoader classLoader = dataStore.getClass().getClassLoader();
    final Class<?>[] interfaces = new Class<?>[] {
      interfaceClass
    };
    final DelegatingDataObjectStore handler = new DelegatingDataObjectStore(
      label, dataStore);
    final T proxyStore = (T)Proxy.newProxyInstance(classLoader, interfaces,
      handler);
    return proxyStore;

  }

  public static <T extends DataObjectStore> T create(final String label,
    final Map<String, Object> config) {
    final ClassLoader classLoader = Thread.currentThread()
      .getContextClassLoader();
    final Class<?>[] interfaces = new Class<?>[] {
      DataObjectStoreFactoryRegistry.getDataObjectStoreInterfaceClass(config)
    };
    final DelegatingDataObjectStore handler = new DelegatingDataObjectStore(
      label, config);
    final T proxyStore = (T)Proxy.newProxyInstance(classLoader, interfaces,
      handler);
    proxyStore.initialize();
    return proxyStore;
  }

  private Map<String, Object> config;

  private DataObjectStore dataStore;

  private String label;

  public DelegatingDataObjectStore() {
  }

  public DelegatingDataObjectStore(final String label,
    final DataObjectStore dataStore) {
    this.label = label;
    this.dataStore = dataStore;
  }

  public DelegatingDataObjectStore(final String label,
    final Map<String, Object> config) {
    this.label = label;
    this.config = config;
  }

  protected DataObjectStore createDataStore() {
    if (config != null) {
      DataObjectStore dataStore = DataObjectStoreFactoryRegistry.createDataObjectStore(config);
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

  public Object invoke(final Object proxy, final Method method,
    final Object[] args) throws Throwable {
    int numArgs;
    if (args == null) {
      numArgs =0;
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
