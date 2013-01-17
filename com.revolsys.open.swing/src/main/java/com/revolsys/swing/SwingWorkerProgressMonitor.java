package com.revolsys.swing;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class SwingWorkerProgressMonitor {
  private PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(final PropertyChangeEvent event) {
      final SwingWorker<?, ?> task = (SwingWorker<?, ?>)event.getSource();
      if (task.isCancelled() || task.isDone()) {
        task.removePropertyChangeListener(this);
        int count = completedTasks.incrementAndGet();
        int total = getTotalTasks();
        int percent = (int)Math.floor(((double)count) / total * 100);
        progress.setProgress(percent);
        progress.setNote("Completed " + percent + "%");
        if (percent == 100 && doneTask != null) {
          doneTask.execute();
        }
      }
    }
  };

  private AtomicInteger completedTasks = new AtomicInteger();

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    SwingWorkerProgressMonitor.class);

  private final List<SwingWorker<?, ?>> tasks = new ArrayList<SwingWorker<?, ?>>();

  private SwingWorker<?, ?> doneTask;

  public SwingWorker<?, ?> add(final String description, final Object object,
    final String backgroundMethodName, final Object... parameters) {
    final SwingWorker<?, ?> task = new InvokeMethodSwingWorker<Object, Object>(
      description, object, backgroundMethodName, Arrays.asList(parameters));
    add(task);
    return task;
  }

  public SwingWorker<?, ?> add(final String description, final Object object,
    final String backgroundMethodName) {
    final SwingWorker<?, ?> tasks = new InvokeMethodSwingWorker<Object, Object>(
      description, object, backgroundMethodName);
    add(tasks);
    return tasks;
  }

  public SwingWorker<?, ?> add(final String description, final Object object,
    final String backgroundMethodName,
    final Collection<? extends Object> backgrounMethodParameters,
    final String doneMethodName,
    final Collection<? extends Object> doneMethodParameters) {
    final SwingWorker<?, ?> tasks = new InvokeMethodSwingWorker<Object, Object>(
      description, object, backgroundMethodName, backgrounMethodParameters,
      doneMethodName, doneMethodParameters);
    add(tasks);
    return tasks;
  }

  public void add(final SwingWorker<?, ?> task) {
    synchronized (tasks) {
      List<SwingWorker<?, ?>> oldTasks = getTasks();
      if (!tasks.contains(task)) {
        tasks.add(task);
      }
      propertyChangeSupport.firePropertyChange("tasks", oldTasks, tasks);
      task.addPropertyChangeListener(propertyChangeListener);
      int count = totalTasks.incrementAndGet();
      if (progress != null) {
        progress.setMaximum(count);
      }
    }
  }

  public SwingWorker<?, ?> setDoneTask(final String description,
    final Object object, final String backgroundMethodName,
    final Object... parameters) {
    final SwingWorker<?, ?> task = new InvokeMethodSwingWorker<Object, Object>(
      description, object, backgroundMethodName, Arrays.asList(parameters));
    setDoneTask(task);
    return task;
  }

  public SwingWorker<?, ?> setDoneTask(final String description,
    final Object object, final String backgroundMethodName) {
    final SwingWorker<?, ?> task = new InvokeMethodSwingWorker<Object, Object>(
      description, object, backgroundMethodName);
    setDoneTask(task);
    return task;
  }

  public SwingWorker<?, ?> setDoneTask(final String description,
    final Object object, final String backgroundMethodName,
    final Collection<? extends Object> backgrounMethodParameters,
    final String doneMethodName,
    final Collection<? extends Object> doneMethodParameters) {
    final SwingWorker<?, ?> task = new InvokeMethodSwingWorker<Object, Object>(
      description, object, backgroundMethodName, backgrounMethodParameters,
      doneMethodName, doneMethodParameters);
    setDoneTask(task);
    return task;
  }

  public void setDoneTask(final SwingWorker<?, ?> task) {
    this.doneTask = task;
  }

  public int getWorkerCount() {
    return getTasks().size();
  }

  public List<SwingWorker<?, ?>> getTasks() {
    return new ArrayList<SwingWorker<?, ?>>(tasks);
  }

  public SwingWorker<?, ?> getTasks(int i) {
    List<SwingWorker<?, ?>> tasks = getTasks();
    if (i < tasks.size()) {
      return tasks.get(i);
    } else {
      return null;
    }
  }

  public PropertyChangeSupport getPropertyChangeSupport() {
    return propertyChangeSupport;
  }

  private AtomicInteger totalTasks = new AtomicInteger();

  private ProgressMonitor progress;

  private String note;

  private Component component;

  public SwingWorkerProgressMonitor(Component component, String note) {
    this.component = component;
    this.note = note;
  }

  public int getTotalTasks() {
    return totalTasks.get();
  }

  public void execute() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (progress == null) {
          progress = new ProgressMonitor(component, note, "Completed 0%", 0,
            100);
          progress.setMillisToDecideToPopup(0);
          progress.setMillisToPopup(100);
          progress.setProgress(0);
          for (SwingWorker<?, ?> task : tasks) {
            SwingWorkerManager.execute(task);
          }
        }
      }
    });
  }
}
