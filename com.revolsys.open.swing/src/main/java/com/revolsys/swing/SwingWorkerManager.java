package com.revolsys.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;

public class SwingWorkerManager implements PropertyChangeListener {
  private static SwingWorkerManager INSTANCE = new SwingWorkerManager();

  public static SwingWorker<?, ?> execute(final String description,
    final Class<?> object, final String backgroundMethodName,
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
    final String doneMethodName) {
    final SwingWorker<?, ?> worker = new InvokeMethodSwingWorker<Object, Object>(
      description, object, backgroundMethodName, doneMethodName);
    execute(worker);
    return worker;
  }

  public static SwingWorker<?, ?> execute(final String description,
    final Object object, final String backgroundMethodName,
    Collection<? extends Object> backgrounMethodParameters,
    final String doneMethodName,
    Collection<? extends Object> doneMethodParameters) {
    final SwingWorker<?, ?> worker = new InvokeMethodSwingWorker<Object, Object>(
      description, object, backgroundMethodName, backgrounMethodParameters,
      doneMethodName, doneMethodParameters);
    execute(worker);
    return worker;
  }

  public static void execute(final SwingWorker<?, ?> worker) {
    INSTANCE.doExecute(worker);
  }

  private final Set<SwingWorker<?, ?>> workers = new LinkedHashSet<SwingWorker<?, ?>>();

  private final Set<SwingWorker<?, ?>> runningWorkers = new LinkedHashSet<SwingWorker<?, ?>>();

  private SwingWorkerManager() {
  }

  private void doExecute(final SwingWorker<?, ?> worker) {
    worker.addPropertyChangeListener(this);
    worker.execute();
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final SwingWorker<?, ?> worker = (SwingWorker<?, ?>)event.getSource();
    if (event.getPropertyName().equals("state")) {
      if (event.getNewValue().equals(StateValue.STARTED)) {
        runningWorkers.add(worker);
      }
    } else if (worker.isCancelled() || worker.isDone()) {
      try {
        runningWorkers.remove(worker);
      } finally {
        try {
          workers.remove(worker);
        } finally {
          worker.removePropertyChangeListener(this);
        }
      }
    }
  }
}
