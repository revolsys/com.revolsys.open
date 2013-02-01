package com.revolsys.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;

import com.revolsys.util.CollectionUtil;

public class SwingWorkerManager {
  private static PropertyChangeListener PROPERTY_CHANGE_LISTENER = new PropertyChangeListener() {
    @Override
    public synchronized void propertyChange(final PropertyChangeEvent event) {
      final SwingWorker<?, ?> worker = (SwingWorker<?, ?>)event.getSource();
      if (event.getPropertyName().equals("state")) {
        if (event.getNewValue().equals(StateValue.STARTED)) {
          List<SwingWorker<?, ?>> oldRunningWorkers = getRunningWorkers();
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
          List<SwingWorker<?, ?>> oldRunningWorkers = getRunningWorkers();
          CollectionUtil.removeReference(RUNNING_WORKERS, worker);
          PROPERTY_CHANGE_SUPPORT.firePropertyChange("runningWorkers",
            oldRunningWorkers, RUNNING_WORKERS);
        } finally {
          try {
            List<SwingWorker<?, ?>> oldWorkers = getWorkers();
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

  public static SwingWorker<?, ?> execute(final String description,
    final Object object, final String backgroundMethodName,
    final Object... parameters) {
    final SwingWorker<?, ?> worker = new InvokeMethodSwingWorker<Object, Object>(
      description, object, backgroundMethodName, Arrays.asList(parameters));
    execute(worker);
    return worker;
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

  public static void execute(final SwingWorker<? extends Object, ? extends Object> worker) {
    synchronized (WORKERS) {
      List<SwingWorker<?, ?>> oldWorkers = getWorkers();
      if (!CollectionUtil.containsReference(WORKERS, worker)) {
        WORKERS.add(new WeakReference<SwingWorker<?, ?>>(worker));
      }
      PROPERTY_CHANGE_SUPPORT.firePropertyChange("workers", oldWorkers, WORKERS);
    }
    worker.addPropertyChangeListener(PROPERTY_CHANGE_LISTENER);
    worker.execute();
  }

  public static int getWorkerCount() {
    return getWorkers().size();
  }

  public static List<SwingWorker<?, ?>> getWorkers() {
    return CollectionUtil.getReferences(WORKERS);
  }

  public static SwingWorker<?, ?> getWorker(int i) {
    List<SwingWorker<?, ?>> workers = getWorkers();
    if (i < workers.size()) {
      return workers.get(i);
    } else {
      return null;
    }
  }

  public static PropertyChangeSupport getPropertyChangeSupport() {
    return PROPERTY_CHANGE_SUPPORT;
  }

  public static List<SwingWorker<?, ?>> getRunningWorkers() {
    return CollectionUtil.getReferences(WORKERS);
  }

  public static boolean isWorkerRunning(final SwingWorker<?, ?> worker) {
    return CollectionUtil.containsReference(RUNNING_WORKERS, worker);
  }

  private SwingWorkerManager() {
  }

  public static SwingWorker<?, ?> executeUi(final String description,
    final Object object, final String doneMethodName,
    final Collection<? extends Object> doneMethodParameters) {
    final SwingWorker<?, ?> worker = new InvokeMethodSwingWorker<Object, Object>(
      description, object, (String)null, (Collection<?>)null, doneMethodName, doneMethodParameters);
    execute(worker);
    return worker;
  }

  public static SwingWorker<?, ?> executeUi(final String description,
    final Object object, 
    final String doneMethodName,
    final Object... doneMethodParameters) {
     return executeUi(description, object, doneMethodName, Arrays.asList(doneMethodParameters));
  }
}
