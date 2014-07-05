package com.revolsys.data.io;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

public class DelegatingRecordStoreHandler implements InvocationHandler {
  public static <T extends RecordStore> T create(final String label,
    final Class<T> interfaceClass, final T dataStore) {
    final ClassLoader classLoader = dataStore.getClass().getClassLoader();
    final Class<?>[] interfaces = new Class<?>[] {
      interfaceClass
    };
    final DelegatingRecordStoreHandler handler = new DelegatingRecordStoreHandler(
      label, dataStore);
    final T proxyStore = (T)Proxy.newProxyInstance(classLoader, interfaces,
      handler);
    return proxyStore;

  }

  @SuppressWarnings("unchecked")
  public static <T extends RecordStore> T create(final String label,
    final Map<String, ? extends Object> config) {
    final ClassLoader classLoader = Thread.currentThread()
      .getContextClassLoader();
    final Class<?>[] interfaces = new Class<?>[] {
      RecordStoreFactoryRegistry.getRecordStoreInterfaceClass(config)
    };
    final DelegatingRecordStoreHandler handler = new DelegatingRecordStoreHandler(
      label, config);
    final T proxyStore = (T)Proxy.newProxyInstance(classLoader, interfaces,
      handler);
    try {
      proxyStore.initialize();
    } catch (final Throwable t) {
      LoggerFactory.getLogger(DelegatingRecordStoreHandler.class).error(
        "Unable to initialize data store " + label, t);
    }
    return proxyStore;
  }

  private Map<String, Object> config;

  private RecordStore dataStore;

  private String label;

  public DelegatingRecordStoreHandler() {
  }

  public DelegatingRecordStoreHandler(final String label,
    final RecordStore dataStore) {
    this.label = label;
    this.dataStore = dataStore;
  }

  public DelegatingRecordStoreHandler(final String label,
    final Map<String, ? extends Object> config) {
    this.label = label;
    this.config = new HashMap<String, Object>(config);
  }

  protected RecordStore createDataStore() {
    if (config != null) {
      final RecordStore dataStore = RecordStoreFactoryRegistry.createRecordStore(config);
      return dataStore;
    } else {
      throw new UnsupportedOperationException("Data store must be set manually");
    }
  }

  public RecordStore getDataStore() {
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
        final RecordStore dataStore = getDataStore();

        dataStore.close();
        this.dataStore = null;
      }
      return null;
    } else {
      final RecordStore dataStore = getDataStore();
      return method.invoke(dataStore, args);
    }
  }
}
