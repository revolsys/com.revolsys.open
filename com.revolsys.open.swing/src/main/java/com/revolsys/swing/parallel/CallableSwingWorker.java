package com.revolsys.swing.parallel;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import com.revolsys.util.Property;

public class CallableSwingWorker<B> extends AbstractSwingWorker<B, Void> {
  private final Callable<B> backgroundTask;

  private final Consumer<B> doneTask;

  private final String description;

  public CallableSwingWorker(final Callable<B> backgroundTask, final Consumer<B> doneTask) {
    this(null, backgroundTask, doneTask);
  }

  public CallableSwingWorker(final String description, final Callable<B> backgroundTask) {
    this(description, backgroundTask, null);
  }

  public CallableSwingWorker(final String description, final Callable<B> backgroundTask,
    final Consumer<B> doneTask) {
    if (Property.isEmpty(description)) {
      this.description = backgroundTask.toString();
    } else {
      this.description = description;
    }
    this.backgroundTask = backgroundTask;
    this.doneTask = doneTask;
  }

  public CallableSwingWorker(final String description, final Runnable backgroundTask) {
    this(description, () -> {
      backgroundTask.run();
      return null;
    } , null);
  }

  public String getDescription() {
    return this.description;
  }

  @Override
  protected B handleBackground() throws Exception {
    if (this.backgroundTask != null) {
      return this.backgroundTask.call();
    }
    return null;
  }

  @Override
  protected void handleDone(final B result) {
    if (this.doneTask != null) {
      this.doneTask.accept(result);
    }
  }

  @Override
  public String toString() {
    return this.description;
  }
}
