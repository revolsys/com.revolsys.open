package com.revolsys.swing.map.layer.record;

import java.util.Set;

import com.revolsys.util.Locals;

public class BatchUpdate implements AutoCloseable {

  private static ThreadLocal<Set<LayerRecord>> RECORDS = new ThreadLocal<>();

  public static boolean isUpdating(final LayerRecord record) {
    return Locals.setContains(RECORDS, record);
  }

  private final boolean added;

  private final LayerRecord record;

  public BatchUpdate(final LayerRecord record) {
    this.record = record;
    this.added = Locals.setAdd(RECORDS, record);
  }

  @Override
  public void close() {
    if (this.added) {
      if (Locals.setRemove(RECORDS, this.record)) {
        this.record.validate();
      }
    }
  }

}
