package com.revolsys.swing.parallel;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class RunnableSwingWorkerManager {

  private class RunnableSwingWorkerProcess extends AbstractSwingWorker<Void, Void> {

    @Override
    protected Void handleBackground() {
      final Queue<Runnable> tasks = RunnableSwingWorkerManager.this.tasks;
      do {
        final Runnable task;
        synchronized (tasks) {
          task = tasks.poll();
          if (task == null) {
            RunnableSwingWorkerManager.this.process = null;
            return null;
          }
        }

        task.run();
      } while (true);
    }

    @Override
    public String toString() {
      return RunnableSwingWorkerManager.this.description;
    }
  }

  private String description;

  private RunnableSwingWorkerProcess process;

  private final Queue<Runnable> tasks = new LinkedList<>();

  public RunnableSwingWorkerManager(final String description) {
    this.description = description;
  }

  public void addTask(final Runnable task) {
    synchronized (this.tasks) {
      this.tasks.add(task);
      executeTasks();
    }
  }

  public void addTasks(final Collection<Runnable> tasks) {
    synchronized (this.tasks) {
      this.tasks.addAll(tasks);
      executeTasks();
    }
  }

  private void executeTasks() {
    if (this.process == null) {
      this.process = new RunnableSwingWorkerProcess();
      Invoke.worker(this.process);
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
