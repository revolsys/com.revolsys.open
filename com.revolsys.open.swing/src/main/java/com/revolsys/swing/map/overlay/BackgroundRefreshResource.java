package com.revolsys.swing.map.overlay;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.SwingWorker;

import com.revolsys.beans.PropertyChangeSupport;
import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.parallel.SupplierConsumerMaxThreadsSwingWorker;
import com.revolsys.util.Cancellable;

public class BackgroundRefreshResource<T> implements PropertyChangeSupportProxy {
  private final String description;

  private final String key;

  private final Function<Cancellable, T> newResourceFactory;

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  private long refreshIndexCurrent = Long.MIN_VALUE;

  private long refreshIndexLast = Long.MIN_VALUE;

  private final AtomicLong refreshIndexNext = new AtomicLong(Long.MIN_VALUE);

  private T resource;

  private SwingWorker<T, Void> worker;

  private final Object refreshSync = new Object();

  public BackgroundRefreshResource(final String description,
    final Function<Cancellable, T> newResourceFactory) {
    this.description = description;
    this.key = description;
    this.newResourceFactory = newResourceFactory;
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  public T getResource() {
    return this.resource;
  }

  public boolean isNew() {
    synchronized (this.refreshIndexNext) {
      if (this.resource == null) {
        return this.refreshIndexNext.get() == Long.MIN_VALUE;
      } else {
        return false;
      }
    }
  }

  public void refresh() {
    synchronized (this.refreshSync) {

      SwingWorker<T, Void> worker;
      long refreshIndex;
      synchronized (this.refreshIndexNext) {
        worker = this.worker;
        if (worker != null && !worker.isDone()
          && this.refreshIndexNext.get() > this.refreshIndexCurrent) {
          return;
        } else {
          refreshIndex = this.refreshIndexNext.incrementAndGet();
        }
      }
      if (worker != null) {
        worker.cancel(true);
      }
      final Supplier<T> backgroundTask = () -> refreshBackground(refreshIndex);
      final Consumer<T> doneTask = resource -> refreshUi(refreshIndex, resource);
      worker = new SupplierConsumerMaxThreadsSwingWorker<>(this.key, 1,
        "Refresh: " + this.description, backgroundTask, doneTask);
      Invoke.worker(worker);
      synchronized (this.refreshIndexNext) {
        this.worker = worker;
      }
    }

  }

  private T refreshBackground(final long refreshIndex) {
    synchronized (this.refreshIndexNext) {
      if (refreshIndex == this.refreshIndexNext.get()) {
        this.refreshIndexCurrent = refreshIndex;
      } else {
        return null;
      }
    }
    final Cancellable cancellable = () -> this.refreshIndexNext.get() > refreshIndex;
    return this.newResourceFactory.apply(cancellable);
  }

  private void refreshUi(final long refreshIndex, final T resource) {
    if (resource != null) {
      final T oldValue = this.resource;
      boolean changed = false;
      synchronized (this.refreshIndexNext) {
        if (refreshIndex > this.refreshIndexLast) {
          this.refreshIndexLast = refreshIndex;
          this.resource = resource;
        }
        if (refreshIndex == this.refreshIndexNext.get()) {
          this.worker = null;
          changed = true;
        }
      }
      if (changed) {
        this.propertyChangeSupport.firePropertyChange("resource", oldValue, resource);
      }
    }
  }
}
