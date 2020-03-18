package com.revolsys.swing.parallel;

import java.util.concurrent.Callable;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.parallel.AbstractRunnable;

/**
 * <p>A {@link Runnable} wrapper for a {@link Callable}. The result of the {@link Callable} can
 * be obtained using the {@link #getResult()} method.</p>
 *
 * <p>NOTE: This class is designed to be used for a single invocation only.</p>
 *
 * @param <T> The type of the result.
 */
public class RunnableCallable<T> extends AbstractRunnable {
  /** The callable to invoke. */
  private final Callable<T> callable;

  /** The result value returned by the callable. */
  private T result;

  public RunnableCallable(final Callable<T> callable) {
    this.callable = callable;
  }

  /**
   * Get the result value returned by the callable.
   *
   * @return The result value returned by the callable.
   */
  public T getResult() {
    return this.result;
  }

  @Override
  public void runDo() {
    try {
      this.result = this.callable.call();
    } catch (final Exception e) {
      Exceptions.throwUncheckedException(e);
    }
  }
}
