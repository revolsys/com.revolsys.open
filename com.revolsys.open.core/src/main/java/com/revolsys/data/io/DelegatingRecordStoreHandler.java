package com.revolsys.data.io;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.revolsys.data.record.schema.RecordStore;

public class DelegatingRecordStoreHandler implements InvocationHandler {
  public static <T extends RecordStore> T create(final String label,
    final Class<T> interfaceClass, final T recordStore) {
    final ClassLoader classLoader = recordStore.getClass().getClassLoader();
    final Class<?>[] interfaces = new Class<?>[] {
      interfaceClass
    };
    final DelegatingRecordStoreHandler handler = new DelegatingRecordStoreHandler(
      label, recordStore);
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
        "Unable to initialize record store " + label, t);
    }
    return proxyStore;
  }

  private Map<String, Object> config;

  private RecordStore recordStore;

  private String label;

  public DelegatingRecordStoreHandler() {
  }

  public DelegatingRecordStoreHandler(final String label,
    final Map<String, ? extends Object> config) {
    this.label = label;
    this.config = new HashMap<String, Object>(config);
  }

  public DelegatingRecordStoreHandler(final String label,
    final RecordStore recordStore) {
    this.label = label;
    this.recordStore = recordStore;
  }

  protected RecordStore createRecordStore() {
    if (this.config != null) {
      final RecordStore recordStore = RecordStoreFactoryRegistry.createRecordStore(this.config);
      return recordStore;
    } else {
      throw new UnsupportedOperationException(
          "Record store must be set manually");
    }
  }

  public RecordStore getRecordStore() {
    if (this.recordStore == null) {
      this.recordStore = createRecordStore();
      this.recordStore.initialize();
    }
    return this.recordStore;
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
      return this.label;
    } else if (method.getName().equals("getLabel") && numArgs == 0) {
      return this.label;
    } else if (method.getName().equals("hashCode") && numArgs == 0) {
      return this.label.hashCode();
    } else if (method.getName().equals("equals") && numArgs == 1) {
      final boolean equal = args[0] == proxy;
      return equal;
    } else if (method.getName().equals("close") && numArgs == 0) {
      if (this.recordStore != null) {
        final RecordStore recordStore = getRecordStore();

        recordStore.close();
        this.recordStore = null;
      }
      return null;
    } else {
      final RecordStore recordStore = getRecordStore();
      return method.invoke(recordStore, args);
    }
  }
}
