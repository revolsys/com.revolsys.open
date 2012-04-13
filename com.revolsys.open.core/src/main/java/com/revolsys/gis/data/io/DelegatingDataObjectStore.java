package com.revolsys.gis.data.io;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class DelegatingDataObjectStore implements InvocationHandler {
  public static <T extends DataObjectStore> T create(
    final String label,
    final Class<T> interfaceClass,
    final T dataObjectStore) {
    final ClassLoader classLoader = dataObjectStore.getClass().getClassLoader();
    final Class<?>[] interfaces = new Class<?>[] {
      interfaceClass
    };
    final DelegatingDataObjectStore handler = new DelegatingDataObjectStore(
      label, dataObjectStore);
    final T proxyStore = (T)Proxy.newProxyInstance(classLoader, interfaces,
      handler);
    return proxyStore;

  }

  public static <T extends DataObjectStore> T create(
    final String label,
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

  private DataObjectStore dataObjectStore;

  private String label;

  public DelegatingDataObjectStore() {
  }

  public DelegatingDataObjectStore(final String label,
    final DataObjectStore dataObjectStore) {
    this.label = label;
    this.dataObjectStore = dataObjectStore;
  }

  public DelegatingDataObjectStore(final String label,
    final Map<String, Object> config) {
    this.label = label;
    this.config = config;
  }

  protected DataObjectStore createDataObjectStore() {
    if (config != null) {
      return DataObjectStoreFactoryRegistry.createDataObjectStore(config);
    } else {
      throw new UnsupportedOperationException("Data store must be set manually");
    }
  }

  public DataObjectStore getDataObjectStore() {
    if (dataObjectStore == null) {
      dataObjectStore = createDataObjectStore();
    }
    return dataObjectStore;
  }

  public Object invoke(
    final Object proxy,
    final Method method,
    final Object[] args) throws Throwable {
    if (method.getName().equals("toString")) {
      return label;
    } else if (method.getName().equals("hashCode")) {
      return label.hashCode();
    } else if (method.getName().equals("equals")) {
      final boolean equal = args[0] == proxy;
      return equal;
    } else {
      final DataObjectStore dataObjectStore = getDataObjectStore();
      return method.invoke(dataObjectStore, args);
    }
  }
}
