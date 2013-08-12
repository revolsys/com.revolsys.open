package com.revolsys.swing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.beans.InvokeMethodCallable;
import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.revolsys.util.ExceptionUtil;

public class InvokeMethodSwingWorker<T, V> extends SwingWorker<T, V> {
  private Callable<T> backgroundTask;

  private Object doneObject;

  private String doneMethodName;

  private String description;

  private Collection<? extends Object> doneMethodParameters;

  public InvokeMethodSwingWorker(final String description, final Object object,
    final String backgroundMethodName) {
    this(description, object, backgroundMethodName, Collections.emptyList(),
      null, Collections.emptyList());
  }

  public InvokeMethodSwingWorker(final String description, final Object object,
    final String backgroundMethodName,
    final Collection<? extends Object> backgroundMethodParameters,
    final String doneMethodName,
    final Collection<? extends Object> doneMethodParameters) {
    this.description = description;
    this.doneObject = object;
    if (StringUtils.hasText(backgroundMethodName)) {
      this.backgroundTask = new InvokeMethodCallable<T>(object,
        backgroundMethodName, backgroundMethodParameters.toArray());
    }
    if (StringUtils.hasText(doneMethodName)) {
      this.doneMethodName = doneMethodName;
      this.doneMethodParameters = new ArrayList<Object>(doneMethodParameters);
      if (object == null) {
        throw new IllegalArgumentException("doneObject cannot be null");
      }
    }
  }

  public InvokeMethodSwingWorker(final String description, final Object object,
    final String backgroundMethodName, final Collection<Object> parameters) {
    this(description, object, backgroundMethodName, parameters, null,
      Collections.emptyList());
  }

  public InvokeMethodSwingWorker(final String description, final Object object,
    final String backgroundMethodName, final String doneMethodName) {
    this(description, object, backgroundMethodName, Collections.emptyList(),
      doneMethodName, Collections.emptyList());
  }

  @Override
  protected T doInBackground() throws Exception {
    if (backgroundTask != null) {
      return backgroundTask.call();
    } else {
      return null;
    }
  }

  @Override
  protected void done() {
    T result = null;
    try {
      if (!isCancelled()) {
        result = get();
      }
    } catch (final InterruptedException e) {
      return;
    } catch (final ExecutionException e) {
      final Throwable cause = e.getCause();
      ExceptionUtil.log(getClass(), "Error running " + description + " using "
        + backgroundTask, cause);
      return;
    }
    if (doneMethodName != null) {
      final List<Object> parameters = new ArrayList<Object>(
        doneMethodParameters);
      if (result != null) {
        parameters.add(result);
      }
      try {
        InvokeMethodRunnable.run(doneObject, doneMethodName, parameters);
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error(
          "Error running "
            + description
            + " using "
            + InvokeMethodRunnable.toString(doneObject, doneMethodName,
              parameters), e);
      }
    }
  }

  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    return description;
  }
}
