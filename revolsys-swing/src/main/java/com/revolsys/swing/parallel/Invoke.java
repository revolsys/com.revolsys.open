package com.revolsys.swing.parallel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.beans.PropertyChangeSupport;
import com.revolsys.collection.list.Lists;
import com.revolsys.collection.map.Maps;
import com.revolsys.parallel.ThreadInterruptedException;
import com.revolsys.util.Property;

public class Invoke {
  private static final PropertyChangeListener PROPERTY_CHANGE_LISTENER = new PropertyChangeListener() {
    @Override
    public synchronized void propertyChange(final PropertyChangeEvent event) {
      final SwingWorker<?, ?> worker = (SwingWorker<?, ?>)event.getSource();

      if (worker.isCancelled() || worker.isDone()) {
        try {
          final List<SwingWorker<?, ?>> oldWorkers;
          List<SwingWorker<?, ?>> newWorkers;
          synchronized (WORKERS) {
            oldWorkers = Lists.toArray(WORKERS);
            WORKERS.remove(worker);
            if (worker instanceof MaxThreadsSwingWorker) {
              final MaxThreadsSwingWorker maxThreadsWorker = (MaxThreadsSwingWorker)worker;
              final String workerKey = maxThreadsWorker.getWorkerKey();
              final int maxThreads = maxThreadsWorker.getMaxThreads();
              int threads = Maps.decrementCount(WORKER_COUNTS, workerKey);
              final List<SwingWorker<?, ?>> waitingWorkers = WAITING_WORKERS.get(workerKey);
              while (Property.hasValue(waitingWorkers) && threads < maxThreads) {
                final SwingWorker<?, ?> nextWorker = waitingWorkers.remove(0);
                Maps.addCount(WORKER_COUNTS, workerKey);
                nextWorker.execute();
                threads++;
              }
            }
            for (final Iterator<SwingWorker<?, ?>> iterator = WORKERS.iterator(); iterator
              .hasNext();) {
              final SwingWorker<?, ?> swingWorker = iterator.next();
              if (swingWorker.isDone()) {
                iterator.remove();
              }
            }
            newWorkers = getWorkers();
          }
          PROPERTY_CHANGE_SUPPORT.firePropertyChange("workers", oldWorkers, newWorkers);
        } finally {
          worker.removePropertyChangeListener(this);
        }
      }
    }
  };

  private static final PropertyChangeSupport PROPERTY_CHANGE_SUPPORT = new PropertyChangeSupport(
    Invoke.class);

  private static final List<SwingWorker<?, ?>> WORKERS = new LinkedList<>();

  private static final Map<String, List<SwingWorker<?, ?>>> WAITING_WORKERS = new HashMap<>();

  private static final Map<String, Integer> WORKER_COUNTS = new HashMap<>();

  public static <V> V andWait(final Callable<V> callable) {
    try {
      if (SwingUtilities.isEventDispatchThread()) {
        return callable.call();
      } else {
        final RunnableCallable<V> runnable = new RunnableCallable<>(callable);
        SwingUtilities.invokeAndWait(runnable);
        return runnable.getResult();
      }
    } catch (final Exception e) {
      return Exceptions.throwUncheckedException(e);
    }
  }

  public static void andWait(final Runnable runnable) {
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    } else {
      try {
        SwingUtilities.invokeAndWait(runnable);
      } catch (final InterruptedException e) {
        throw new ThreadInterruptedException(e);
      } catch (final InvocationTargetException e) {
        Exceptions.throwCauseException(e);
      }
    }
  }

  public static <V> SwingWorker<V, Void> background(final String key, final int maxThreads,
    final String description, final Supplier<V> backgroundTask, final Consumer<V> doneTask) {
    if (backgroundTask != null) {
      if (SwingUtilities.isEventDispatchThread()) {
        final SwingWorker<V, Void> worker = new SupplierConsumerMaxThreadsSwingWorker<>(key,
          maxThreads, description, backgroundTask, doneTask);
        worker(worker);
        return worker;
      } else {
        try {
          final V result = backgroundTask.get();
          later(() -> doneTask.accept(result));
        } catch (final Exception e) {
          Exceptions.throwUncheckedException(e);
        }
      }
    }
    return null;
  }

  public static SwingWorker<?, ?> background(final String description,
    final Runnable backgroundTask) {
    if (backgroundTask != null) {
      if (SwingUtilities.isEventDispatchThread()) {
        final SwingWorker<?, ?> worker = new SupplierConsumerSwingWorker<>(description, () -> {
          backgroundTask.run();
          return null;
        });
        worker(worker);
        return worker;
      } else {
        backgroundTask.run();
      }
    }
    return null;
  }

  public static SwingWorker<?, ?> background(final String description,
    final Supplier<?> backgroundTask) {
    if (backgroundTask != null) {
      if (SwingUtilities.isEventDispatchThread()) {
        final SwingWorker<?, ?> worker = new SupplierConsumerSwingWorker<>(description,
          backgroundTask);
        worker(worker);
        return worker;
      } else {
        backgroundTask.get();
      }
    }
    return null;
  }

  public static <V> SwingWorker<V, Void> background(final String description,
    final Supplier<V> backgroundTask, final Consumer<V> doneTask) {
    if (backgroundTask != null) {
      if (SwingUtilities.isEventDispatchThread()) {
        final SwingWorker<V, Void> worker = new SupplierConsumerSwingWorker<>(description,
          backgroundTask, doneTask);
        worker(worker);
        return worker;
      } else {
        final V result = backgroundTask.get();
        later(() -> doneTask.accept(result));
      }
    }
    return null;
  }

  public static PropertyChangeSupport getPropertyChangeSupport() {
    return PROPERTY_CHANGE_SUPPORT;
  }

  public static List<SwingWorker<?, ?>> getWorkers() {
    synchronized (WORKERS) {
      final List<SwingWorker<?, ?>> workers = new ArrayList<>();
      for (final SwingWorker<?, ?> worker : WORKERS) {
        if (!worker.isDone()) {
          workers.add(worker);
        }
      }
      return workers;
    }
  }

  public static boolean hasWorker() {
    return !getWorkers().isEmpty();
  }

  public static void later(final Runnable runnable) {
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    } else {
      SwingUtilities.invokeLater(runnable);
    }
  }

  public static void laterQueue(final Runnable runnable) {
    SwingUtilities.invokeLater(runnable);
  }

  public static <V> boolean swingThread(final Consumer<V> action, final V arg) {
    if (SwingUtilities.isEventDispatchThread()) {
      return true;
    } else {
      SwingUtilities.invokeLater(() -> action.accept(arg));
      return false;
    }
  }

  public static boolean swingThread(final Runnable action) {
    if (SwingUtilities.isEventDispatchThread()) {
      return true;
    } else {
      SwingUtilities.invokeLater(action);
      return false;
    }
  }

  public static void worker(final SwingWorker<? extends Object, ? extends Object> worker) {
    boolean execute = true;
    final List<SwingWorker<?, ?>> oldWorkers;
    final List<SwingWorker<?, ?>> newWorkers;
    synchronized (WORKERS) {
      if (WORKERS.contains(worker)) {
        return;
      }
      oldWorkers = Lists.toArray(WORKERS);
      WORKERS.add(worker);
      if (worker instanceof MaxThreadsSwingWorker) {
        final MaxThreadsSwingWorker maxThreadsWorker = (MaxThreadsSwingWorker)worker;
        final String workerKey = maxThreadsWorker.getWorkerKey();
        final int maxThreads = maxThreadsWorker.getMaxThreads();
        final int threads = Maps.getCount(WORKER_COUNTS, workerKey);
        if (threads >= maxThreads) {
          execute = false;
          Maps.addToList(WAITING_WORKERS, workerKey, worker);
        } else {
          Maps.addCount(WORKER_COUNTS, workerKey);
        }
      }
      newWorkers = getWorkers();
    }
    worker.addPropertyChangeListener(PROPERTY_CHANGE_LISTENER);
    PROPERTY_CHANGE_SUPPORT.firePropertyChange("workers", oldWorkers, newWorkers);
    if (execute) {
      worker.execute();
    }
  }

  /**
   * Use a swing worker to make sure the task is done later in the UI thread.
   *
   * @param description
   * @param doneTask
   */
  public static void workerDone(final String description, final Runnable doneTask) {
    if (doneTask != null) {
      final SwingWorker<Void, Void> worker = new SupplierConsumerSwingWorker<>(description, null,
        result -> doneTask.run());
      worker(worker);
    }
  }

  private Invoke() {
  }
}
