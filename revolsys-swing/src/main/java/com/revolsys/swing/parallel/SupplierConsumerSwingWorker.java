package com.revolsys.swing.parallel;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.revolsys.util.Property;

public class SupplierConsumerSwingWorker<B> extends AbstractSwingWorker<B, Void> {
  private final Supplier<B> backgroundTask;

  private final Consumer<B> doneTask;

  private final String description;

  public SupplierConsumerSwingWorker(final String description, final Runnable backgroundTask) {
    this(description, () -> {
      backgroundTask.run();
      return null;
    }, null);
  }

  public SupplierConsumerSwingWorker(final String description, final Supplier<B> backgroundTask) {
    this(description, backgroundTask, null);
  }

  public SupplierConsumerSwingWorker(final String description, final Supplier<B> backgroundTask,
    final Consumer<B> doneTask) {
    if (Property.isEmpty(description)) {
      this.description = backgroundTask.toString();
    } else {
      this.description = description;
    }
    this.backgroundTask = backgroundTask;
    this.doneTask = doneTask;
  }

  public SupplierConsumerSwingWorker(final Supplier<B> backgroundTask, final Consumer<B> doneTask) {
    this(null, backgroundTask, doneTask);
  }

  public String getDescription() {
    return this.description;
  }

  @Override
  protected B handleBackground() {
    if (this.backgroundTask != null) {
      return this.backgroundTask.get();
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
