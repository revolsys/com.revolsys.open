package com.revolsys.swing.parallel;

import java.beans.PropertyChangeListener;
import java.util.function.Consumer;

import com.revolsys.beans.PropertyChangeSupport;
import com.revolsys.util.Property;

class BackgroundTaskManager {

  private static final PropertyChangeSupport PROPERTY_CHANGE_SUPPORT = new PropertyChangeSupport(
    Invoke.class);

  static void addTask(final BackgroundTask task) {
    PROPERTY_CHANGE_SUPPORT.firePropertyChange("task", null, task);
  }

  static PropertyChangeListener addTaskListener(final Consumer<BackgroundTask> listener) {
    return Property.addListenerNewValue(PROPERTY_CHANGE_SUPPORT, "task", listener);
  }

  public static PropertyChangeListener addTaskStatusChangedListener(final Runnable listener) {
    return Property.addListenerRunnable(PROPERTY_CHANGE_SUPPORT, "taskStatusChanged", listener);
  }

  static void removeListener(final PropertyChangeListener listener) {
    PROPERTY_CHANGE_SUPPORT.removePropertyChangeListener("task", listener);
  }

  public static void taskStatusChanged() {
    PROPERTY_CHANGE_SUPPORT.firePropertyChange("taskStatusChanged", false, true);
  }
}
