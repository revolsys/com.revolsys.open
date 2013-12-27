package com.revolsys.swing.parallel;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class RunnableSwingWorkerManager {

  private final String description;

  private RunnableSwingWorkerProcess process;

  private final Queue<Runnable> tasks = new LinkedList<Runnable>();

  public RunnableSwingWorkerManager(final String description) {
    this.description = description;
  }

  public void addTask(final Runnable task) {
    synchronized (this.tasks) {
      this.tasks.add(task);
      if (process == null) {
        process = new RunnableSwingWorkerProcess(this);
        process.execute();
      }
    }
  }

  public void addTasks(final Collection<Runnable> tasks) {
    synchronized (this.tasks) {
      this.tasks.addAll(tasks);
      if (process == null) {
        process = new RunnableSwingWorkerProcess(this);
        process.execute();
      }
    }
  }

  public Runnable getNextTask() {
    synchronized (tasks) {
      final Runnable task = tasks.poll();
      if (task == null) {
        process = null;
      }
      return task;
    }
  }

  public void removeTask(final Runnable task) {
    synchronized (tasks) {
      tasks.remove(task);
    }
  }

  public void removeTasks(final Collection<Runnable> tasks) {
    synchronized (this.tasks) {
      this.tasks.removeAll(tasks);
    }
  }

  @Override
  public String toString() {
    return description;
  }
}
