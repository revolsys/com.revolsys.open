package com.revolsys.swing.parallel;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class RunnableSwingWorkerManager {

  private String description;

  private RunnableSwingWorkerProcess process;

  private final Queue<Runnable> tasks = new LinkedList<>();

  public RunnableSwingWorkerManager(final String description) {
    this.description = description;
  }

  public void addTask(final Runnable task) {
    synchronized (this.tasks) {
      this.tasks.add(task);
      if (this.process == null) {
        this.process = new RunnableSwingWorkerProcess(this);
        this.process.execute();
      }
    }
  }

  public void addTasks(final Collection<Runnable> tasks) {
    synchronized (this.tasks) {
      this.tasks.addAll(tasks);
      if (this.process == null) {
        this.process = new RunnableSwingWorkerProcess(this);
        this.process.execute();
      }
    }
  }

  public Runnable getNextTask() {
    synchronized (this.tasks) {
      final Runnable task = this.tasks.poll();
      if (task == null) {
        this.process = null;
      }
      return task;
    }
  }

  public void removeTask(final Runnable task) {
    synchronized (this.tasks) {
      this.tasks.remove(task);
    }
  }

  public void removeTasks(final Collection<Runnable> tasks) {
    synchronized (this.tasks) {
      this.tasks.removeAll(tasks);
    }
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return this.description;
  }
}
