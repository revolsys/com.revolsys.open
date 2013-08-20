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
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;

import org.springframework.transaction.PlatformTransactionManager;

import com.revolsys.beans.MethodInvoker;
import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.revolsys.transaction.TransactionUtils;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.ExceptionUtil;

public class SwingWorkerManager {
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
    SwingWorkerManager.class);

  private static final List<WeakReference<SwingWorker<?, ?>>> WORKERS = new ArrayList<WeakReference<SwingWorker<?, ?>>>();

  private static final List<WeakReference<SwingWorker<?, ?>>> RUNNING_WORKERS = new ArrayList<WeakReference<SwingWorker<?, ?>>>();

  public static void execute(final Runnable backgroundTask) {
    execute(new RunnableSwingWorker(backgroundTask));
  }

  public static void execute(final String description, final Object object,
    final Method method, final Object... parameters) {
    final MethodInvoker backgroundTask = new MethodInvoker(method, object,
      parameters);
    execute(description, backgroundTask);
  }

  public static SwingWorker<?, ?> execute(final String description,
    final Object object, final String backgroundMethodName) {
    final SwingWorker<?, ?> worker = new InvokeMethodSwingWorker<Object, Object>(
      description, object, backgroundMethodName);
    execute(worker);
    return worker;
  }

  public static SwingWorker<?, ?> execute(final String description,
    final Object object, final String backgroundMethodName,
    final Collection<? extends Object> backgrounMethodParameters,
    final String doneMethodName,
    final Collection<? extends Object> doneMethodParameters) {
    final SwingWorker<?, ?> worker = new InvokeMethodSwingWorker<Object, Object>(
      description, object, backgroundMethodName, backgrounMethodParameters,
      doneMethodName, doneMethodParameters);
    execute(worker);
    return worker;
  }

  public static SwingWorker<?, ?> execute(final String description,
    final Object object, final String backgroundMethodName,
    final Object... parameters) {
    final SwingWorker<?, ?> worker = new InvokeMethodSwingWorker<Object, Object>(
      description, object, backgroundMethodName, Arrays.asList(parameters));
    execute(worker);
    return worker;
  }

  public static void execute(final String description,
    final Runnable backgroundTask) {
    execute(new RunnableSwingWorker(description, backgroundTask));
  }

  public static void execute(
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

  public static void executeUi(final String description, final Object object,
    final String doneMethodName, final Object... doneParameters) {
    final SwingWorker<?, ?> worker = new InvokeMethodSwingWorker<Object, Object>(
      description, object, null, Collections.emptyList(), doneMethodName,
      Arrays.asList(doneParameters));
    execute(worker);
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

  public static void invokeAndWait(final Object object,
    final String methodName, final Object... parameters) {
    final InvokeMethodRunnable runnable = new InvokeMethodRunnable(object,
      methodName, parameters);
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    } else {
      try {
        SwingUtilities.invokeAndWait(runnable);
      } catch (final InterruptedException e) {
        ExceptionUtil.throwUncheckedException(e);
      } catch (final InvocationTargetException e) {
        ExceptionUtil.throwCauseException(e);
      }
    }
  }

  public static void invokeLater(final Object object, final String methodName,
    final Object... parameters) {
    final InvokeMethodRunnable runnable = new InvokeMethodRunnable(object,
      methodName, parameters);
    SwingUtilities.invokeLater(runnable);
  }

  public static void invokeLater(final Runnable runnable) {
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    } else {
      SwingUtilities.invokeLater(runnable);
    }
  }

  public static boolean isWorkerRunning(final SwingWorker<?, ?> worker) {
    return CollectionUtil.containsReference(RUNNING_WORKERS, worker);
  }

  public static void transactionExecute(
    final PlatformTransactionManager transactionManager,
    final int propagationBehavior, final Runnable runnable) {
    execute(TransactionUtils.createRunnable(transactionManager,
      propagationBehavior, runnable));
  }

  private SwingWorkerManager() {
  }
}
