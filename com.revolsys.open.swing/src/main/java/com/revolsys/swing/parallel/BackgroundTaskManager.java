package com.revolsys.swing.parallel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.function.Consumer;

import com.revolsys.util.Property;

class BackgroundTaskManager {

  private static final PropertyChangeSupport PROPERTY_CHANGE_SUPPORT = new PropertyChangeSupport(
    Invoke.class);

  static PropertyChangeListener addListener(final Consumer<BackgroundTask> listener) {
    return Property.addListenerNewValue(PROPERTY_CHANGE_SUPPORT, "task", listener);
  }

  static void addTask(final BackgroundTask task) {
    PROPERTY_CHANGE_SUPPORT.firePropertyChange("task", null, task);
  }

  static void removeListener(final PropertyChangeListener listener) {
    PROPERTY_CHANGE_SUPPORT.removePropertyChangeListener("task", listener);
  }
}
