package com.revolsys.jdbc.io;

import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class JdbcWriterSynchronization extends
  TransactionSynchronizationAdapter {

  private final JdbcWriterResourceHolder writerHolder;

  private final Object key;

  private boolean holderActive = true;

  private final AbstractJdbcDataObjectStore dataStore;

  public JdbcWriterSynchronization(final AbstractJdbcDataObjectStore dataStore,
    final JdbcWriterResourceHolder writerHolder, final Object key) {
    this.dataStore = dataStore;
    this.writerHolder = writerHolder;
    this.key = key;
  }

  @Override
  public void afterCompletion(final int status) {
    if (holderActive) {
      TransactionSynchronizationManager.unbindResourceIfPossible(key);
      holderActive = false;
      if (writerHolder.hasWriter()) {
        final JdbcWriter writer = writerHolder.getWriter();
        dataStore.releaseWriter(writer);
        writerHolder.setWriter(null);
      }
    }
    writerHolder.reset();
  }

  @Override
  public void beforeCompletion() {
    if (!writerHolder.isOpen()) {
      TransactionSynchronizationManager.unbindResource(key);
      holderActive = false;
      if (writerHolder.hasWriter()) {
        final JdbcWriter writer = writerHolder.getWriter();
        dataStore.releaseWriter(writer);
      }
    }
  }

  @Override
  public int getOrder() {
    return 999;
  }

  @Override
  public void resume() {
    if (holderActive) {
      TransactionSynchronizationManager.bindResource(key, writerHolder);
    }
  }

  @Override
  public void suspend() {
    if (holderActive) {
      TransactionSynchronizationManager.unbindResource(key);
      if (writerHolder.hasWriter() && !writerHolder.isOpen()) {
        final JdbcWriter writer = writerHolder.getWriter();
        dataStore.releaseWriter(writer);
        writerHolder.setWriter(null);
      }
    }
  }
}
