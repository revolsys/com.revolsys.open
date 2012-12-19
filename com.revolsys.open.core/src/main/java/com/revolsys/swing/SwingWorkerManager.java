package com.revolsys.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;

public class SwingWorkerManager implements PropertyChangeListener {
  private static SwingWorkerManager INSTANCE = new SwingWorkerManager();

  public static void execute(SwingWorker<?, ?> worker) {
    INSTANCE.doExecute(worker);
  }

  public static SwingWorker<?, ?> execute(final String description,
    final Object object, String backgroundMethodName,
    final String doneMethodName) {
    SwingWorker<?, ?> worker = new InvokeMethodSwingWorker<Object, Object>(
      description, object, backgroundMethodName, doneMethodName);
    execute(worker);
    return worker;
  }

  public static SwingWorker<?, ?> execute(final String description,
    final Object object, String backgroundMethodName) {
    SwingWorker<?, ?> worker = new InvokeMethodSwingWorker<Object, Object>(
      description, object, backgroundMethodName);
    execute(worker);
    return worker;
  }

  private SwingWorkerManager() {
  }

  private Set<SwingWorker<?, ?>> workers = new LinkedHashSet<SwingWorker<?, ?>>();

  private Set<SwingWorker<?, ?>> runningWorkers = new LinkedHashSet<SwingWorker<?, ?>>();

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    SwingWorker<?, ?> worker = (SwingWorker<?, ?>)event.getSource();
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

  private void doExecute(SwingWorker<?, ?> worker) {
    worker.addPropertyChangeListener(this);
    worker.execute();
  }

  public static SwingWorker<?, ?> execute(String description, Class<?> object,
    String backgroundMethodName, Object... parameters) {
    SwingWorker<?, ?> worker = new InvokeMethodSwingWorker<Object, Object>(
      description, object, backgroundMethodName, Arrays.asList(parameters));
    execute(worker);
    return worker;
  }
}
