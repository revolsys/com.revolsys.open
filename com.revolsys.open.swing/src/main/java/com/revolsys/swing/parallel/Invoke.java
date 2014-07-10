package com.revolsys.swing.parallel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;

import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;

import com.revolsys.beans.MethodInvoker;
import com.revolsys.parallel.ThreadInterruptedException;
import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.ExceptionUtil;

public class Invoke {
  public static void andWait(final InvokeMethodRunnable runnable) {
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    } else {
      try {
        SwingUtilities.invokeAndWait(runnable);
      } catch (final InterruptedException e) {
        throw new ThreadInterruptedException(e);
      } catch (final InvocationTargetException e) {
        ExceptionUtil.throwCauseException(e);
      }
    }
  }

  public static void andWait(final Object object, final String methodName,
    final Object... parameters) {
    final InvokeMethodRunnable runnable = new InvokeMethodRunnable(object,
      methodName, parameters);
    andWait(runnable);
  }

  public static void background(final Runnable backgroundTask) {
    worker(new RunnableSwingWorker(backgroundTask));
  }

  public static void background(final String description, final Object object,
    final Method method, final Object... parameters) {
    final MethodInvoker backgroundTask = new MethodInvoker(method, object,
      parameters);
    background(description, backgroundTask);
  }

  public static SwingWorker<?, ?> background(final String description,
    final Object object, final String backgroundMethodName,
    final List<Object> parameters) {
    final SwingWorker<?, ?> worker = new InvokeMethodSwingWorker<Object, Object>(
        description, object, backgroundMethodName, parameters);
    worker(worker);
    return worker;
  }

  public static SwingWorker<?, ?> background(final String description,
    final Object object, final String backgroundMethodName,
    final Object... parameters) {
    final SwingWorker<?, ?> worker = new InvokeMethodSwingWorker<Object, Object>(
        description, object, backgroundMethodName, Arrays.asList(parameters));
    worker(worker);
    return worker;
  }

  public static void background(final String description,
    final Runnable backgroundTask) {
    worker(new RunnableSwingWorker(description, backgroundTask));
  }

  public static void backgroundTransaction(final String description,
    final PlatformTransactionManager transactionManager,
    final Propagation propagation, final Runnable runnable) {
    background(description,
      Transaction.runnable(runnable, transactionManager, propagation));
  }

  public static PropertyChangeSupport getPropertyChangeSupport() {
    return PROPERTY_CHANGE_SUPPORT;
  }

  public static List<SwingWorker<?, ?>> getRunningWorkers() {
    return CollectionUtil.getReferences(WORKERS);
  }

  public static SwingWorker<?, ?> getWorker(final int i) {
    final List<SwingWorker<?, ?>> workers = getWorkers();
    if (i < workers.size()) {
      return workers.get(i);
    } else {
      return null;
    }
  }

  public static int getWorkerCount() {
    return getWorkers().size();
  }

  public static List<SwingWorker<?, ?>> getWorkers() {
    return CollectionUtil.getReferences(WORKERS);
  }

  public static boolean isWorkerRunning(final SwingWorker<?, ?> worker) {
    return CollectionUtil.containsReference(RUNNING_WORKERS, worker);
  }

  public static void later(final Object object, final Method method,
    final Object... parameters) {
    later(new Runnable() {

      @Override
      public void run() {
        try {
          method.invoke(object, parameters);
        } catch (final InvocationTargetException e) {
          LoggerFactory.getLogger(getClass()).error(
            "Error invoking method " + method + " "
                + Arrays.toString(parameters), e.getTargetException());
        } catch (final Throwable e) {
          LoggerFactory.getLogger(getClass()).error(
            "Error invoking method " + method + " "
                + Arrays.toString(parameters), e);
        }
      }
    });
  }

  public static void later(final Object object, final String methodName,
    final Object... parameters) {
    final InvokeMethodRunnable runnable = new InvokeMethodRunnable(object,
      methodName, parameters);
    later(runnable);
  }

  public static void later(final Runnable runnable) {
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    } else {
      SwingUtilities.invokeLater(runnable);
    }
  }

  public static SwingWorker<?, ?> worker(final String description,
    final Object object, final String backgroundMethodName,
    final Collection<? extends Object> backgrounMethodParameters,
    final String doneMethodName,
    final Collection<? extends Object> doneMethodParameters) {
    final SwingWorker<?, ?> worker = new InvokeMethodSwingWorker<Object, Object>(
        description, object, backgroundMethodName, backgrounMethodParameters,
        doneMethodName, doneMethodParameters);
    worker(worker);
    return worker;
  }

  public static void worker(
    final SwingWorker<? extends Object, ? extends Object> worker) {
    synchronized (WORKERS) {
      final List<SwingWorker<?, ?>> oldWorkers = getWorkers();
      if (!CollectionUtil.containsReference(WORKERS, worker)) {
        WORKERS.add(new WeakReference<SwingWorker<?, ?>>(worker));
      }
      PROPERTY_CHANGE_SUPPORT.firePropertyChange("workers", oldWorkers, WORKERS);
    }
    worker.addPropertyChangeListener(PROPERTY_CHANGE_LISTENER);
    worker.execute();
  }

  private static PropertyChangeListener PROPERTY_CHANGE_LISTENER = new PropertyChangeListener() {
    @Override
    public synchronized void propertyChange(final PropertyChangeEvent event) {
      final SwingWorker<?, ?> worker = (SwingWorker<?, ?>)event.getSource();
      if (event.getPropertyName().equals("state")) {
        if (event.getNewValue().equals(StateValue.STARTED)) {
          final List<SwingWorker<?, ?>> oldRunningWorkers = getRunningWorkers();
          if (!CollectionUtil.containsReference(RUNNING_WORKERS, worker)) {
            RUNNING_WORKERS.add(new WeakReference<SwingWorker<?, ?>>(worker));
          }
          PROPERTY_CHANGE_SUPPORT.firePropertyChange("runningWorkers",
            oldRunningWorkers, RUNNING_WORKERS);
          return;
        }
      }
      if (worker.isCancelled() || worker.isDone()) {
        try {
          final List<SwingWorker<?, ?>> oldRunningWorkers = getRunningWorkers();
          CollectionUtil.removeReference(RUNNING_WORKERS, worker);
          PROPERTY_CHANGE_SUPPORT.firePropertyChange("runningWorkers",
            oldRunningWorkers, RUNNING_WORKERS);
        } finally {
          try {
            final List<SwingWorker<?, ?>> oldWorkers = getWorkers();
            synchronized (WORKERS) {
              CollectionUtil.removeReference(WORKERS, worker);
            }
            PROPERTY_CHANGE_SUPPORT.firePropertyChange("workers", oldWorkers,
              WORKERS);
          } finally {
            worker.removePropertyChangeListener(this);
          }
        }
      }
    }
  };

  private static final PropertyChangeSupport PROPERTY_CHANGE_SUPPORT = new PropertyChangeSupport(
    Invoke.class);

  private static final List<WeakReference<SwingWorker<?, ?>>> WORKERS = new ArrayList<WeakReference<SwingWorker<?, ?>>>();

  private static final List<WeakReference<SwingWorker<?, ?>>> RUNNING_WORKERS = new ArrayList<WeakReference<SwingWorker<?, ?>>>();

  private Invoke() {
  }
}
