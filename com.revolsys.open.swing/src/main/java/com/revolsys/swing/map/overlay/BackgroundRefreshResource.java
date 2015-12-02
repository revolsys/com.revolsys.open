package com.revolsys.swing.map.overlay;

import java.beans.PropertyChangeSupport;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import javax.swing.SwingWorker;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.parallel.SupplierConsumerMaxThreadsSwingWorker;

public class BackgroundRefreshResource<T> implements PropertyChangeSupportProxy {
  private T resource;

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  private final Supplier<T> newResourceFactory;

  private long refreshIndexLast = Long.MIN_VALUE;

  private final AtomicLong refreshIndexNext = new AtomicLong(Long.MIN_VALUE);

  private long refreshIndexCurrent = Long.MIN_VALUE;

  private SwingWorker<T, Void> worker;

  private final String key;

  private final String description;

  public BackgroundRefreshResource(final String description, final Supplier<T> newResourceFactory) {
    this.description = description;
    this.key = description;
    this.newResourceFactory = newResourceFactory;
  }

  protected boolean canRefreshFinish(final long refreshIndex) {
    if (refreshIndex >= this.refreshIndexLast) {
      this.refreshIndexLast = refreshIndex;
      return true;
    } else {
      return false;
    }
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  public T getResource() {
    return this.resource;
  }

  public void refresh() {
    long refreshIndex;
    synchronized (this.refreshIndexNext) {
      if (this.worker != null && this.refreshIndexNext.get() > this.refreshIndexCurrent) {
        return;
      } else {
        refreshIndex = this.refreshIndexNext.incrementAndGet();
      }
    }
    if (this.worker != null) {
      this.worker.cancel(true);
    }
    this.worker = new SupplierConsumerMaxThreadsSwingWorker<>(this.key, 1,
      "Refresh: " + this.description, () -> {
        return refreshBackground(refreshIndex);
      } , (resource) -> {
        refreshUi(refreshIndex, resource);
      });
    Invoke.worker(this.worker);
  }

  private T refreshBackground(final long refreshIndex) {
    synchronized (this.refreshIndexNext) {
      if (refreshIndex == this.refreshIndexNext.get()) {
        this.refreshIndexCurrent = refreshIndex;
      } else {
        return null;
      }
    }
    return this.newResourceFactory.get();
  }

  private void refreshUi(final long refreshIndex, final T resource) {
    if (resource != null) {
      final T oldValue = this.resource;
      boolean changed = false;
      synchronized (this.refreshIndexNext) {
        if (refreshIndex == this.refreshIndexNext.get()) {
          this.worker = null;
        }
        if (refreshIndex > this.refreshIndexLast) {
          this.refreshIndexLast = refreshIndex;
          this.resource = resource;
          changed = true;
        }
      }
      if (changed) {
        this.propertyChangeSupport.firePropertyChange("resource", oldValue, resource);
      }
    }
  }
}
