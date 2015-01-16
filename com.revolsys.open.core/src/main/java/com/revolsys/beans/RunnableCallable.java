package com.revolsys.beans;

import java.util.concurrent.Callable;

import javax.swing.SwingUtilities;

import com.revolsys.parallel.AbstractRunnable;
import com.revolsys.util.ExceptionUtil;

/**
 * <p>A {@link Runnable} wrapper for a {@link Callable}. The result of the {@link Callable} can
 * be obtained using the {@link #getResult()} method.</p>
 *
 * <p>NOTE: This class is designed to be used for a single invocation only.</p>
 *
 * @param <T> The type of the result.
 */
public class RunnableCallable<T> extends AbstractRunnable {

  public static <V> V invokeAndWait(final Callable<V> callable) {
    final RunnableCallable<V> runnable = new RunnableCallable<V>(callable);
    try {
      SwingUtilities.invokeAndWait(runnable);
    } catch (final Throwable e) {
      ExceptionUtil.throwUncheckedException(e);
    }
    return runnable.getResult();
  }

  /** The callable to invoke. */
  private final Callable<T> callable;

  /** The result value returned by the callable. */
  private T result;

  public RunnableCallable(final Callable<T> callable) {
    this.callable = callable;
  }

  @Override
  public void doRun() {
    try {
      this.result = this.callable.call();
    } catch (final Exception e) {
      ExceptionUtil.throwUncheckedException(e);
    }
  }

  /**
   * Get the result value returned by the callable.
   *
   * @return The result value returned by the callable.
   */
  public T getResult() {
    return this.result;
  }
}
