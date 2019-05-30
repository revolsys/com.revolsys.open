package com.revolsys.gis.parallel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.MapContext;
import org.jeometry.common.logging.Logs;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import com.revolsys.collection.map.ThreadSharedProperties;
import com.revolsys.parallel.ThreadInterruptedException;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;
import com.revolsys.parallel.process.BaseInProcess;
import com.revolsys.parallel.tools.ScriptExecutorRunnable;
import com.revolsys.record.Record;
import com.revolsys.util.JexlUtil;

public class ScriptExecutorProcess extends BaseInProcess<Record> implements BeanFactoryAware {
  private final Map<String, Object> attributes = new HashMap<>();

  private ExecutorService executor;

  private final Map<String, Expression> expressions = new HashMap<>();

  private int maxConcurrentScripts = 1;

  private Map<String, String> parameters = new HashMap<>();

  private String script;

  private final Set<Future<?>> tasks = new LinkedHashSet<>();

  @Override
  protected void destroy() {
    try {
      while (!this.tasks.isEmpty()) {
        synchronized (this) {
          try {
            this.wait(1000);
          } catch (final InterruptedException e) {
            throw new ThreadInterruptedException(e);
          }
          for (final Iterator<Future<?>> taskIter = this.tasks.iterator(); taskIter.hasNext();) {
            final Future<?> task = taskIter.next();
            if (task.isDone()) {
              taskIter.remove();
            }
          }
        }
      }
    } finally {
      this.executor.shutdown();
    }
  }

  private void executeScript(final Record record) {
    try {
      final Map<String, Object> vars = new HashMap<>(this.attributes);
      vars.putAll(record);
      final MapContext context = new MapContext(vars);
      final Map<String, Object> scriptParams = new HashMap<>();
      scriptParams.putAll(this.attributes);
      for (final Entry<String, Expression> param : this.expressions.entrySet()) {
        final String key = param.getKey();
        final Expression expression = param.getValue();
        final Object value = JexlUtil.evaluateExpression(context, expression);
        scriptParams.put(key, value);
      }
      final ScriptExecutorRunnable scriptRunner = new ScriptExecutorRunnable(this.script,
        scriptParams);
      if (this.executor == null) {
        scriptRunner.run();
      } else {
        while (this.tasks.size() >= this.maxConcurrentScripts) {
          try {
            synchronized (this) {
              try {
                this.wait(1000);
              } catch (final InterruptedException e) {
                throw new ThreadInterruptedException(e);
              }
              for (final Iterator<Future<?>> taskIter = this.tasks.iterator(); taskIter
                .hasNext();) {
                final Future<?> task = taskIter.next();
                if (task.isDone()) {
                  taskIter.remove();
                }
              }
            }
          } catch (final ThreadInterruptedException e) {
            throw new ClosedException(e);
          }
        }
        final Future<?> future = this.executor.submit(scriptRunner);
        this.tasks.add(future);
      }
    } catch (final ThreadDeath e) {
      throw e;
    } catch (final Throwable t) {
      Logs.error(this, t);
    }
  }

  public ExecutorService getExecutor() {
    return this.executor;
  }

  public int getMaxConcurrentScripts() {
    return this.maxConcurrentScripts;
  }

  public Map<String, String> getParameters() {
    return this.parameters;
  }

  public String getScript() {
    return this.script;
  }

  @Override
  protected void postRun(final Channel<Record> in) {
    this.tasks.clear();
    if (this.executor != null) {
      this.executor.shutdownNow();
    }
  }

  @Override
  protected void process(final Channel<Record> in, final Record object) {
    executeScript(object);
  }

  @Override
  public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
    this.attributes.putAll(ThreadSharedProperties.getProperties());
  }

  public void setExecutor(final ExecutorService executor) {
    this.executor = executor;
  }

  public void setMaxConcurrentScripts(final int maxConcurrentScripts) {
    this.maxConcurrentScripts = maxConcurrentScripts;
    if (this.executor == null) {
      this.executor = new ThreadPoolExecutor(Math.min(maxConcurrentScripts, 10),
        maxConcurrentScripts, 10, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    }
  }

  public void setParameters(final Map<String, String> parameters) {
    this.parameters = parameters;
    for (final Entry<String, String> param : parameters.entrySet()) {
      final String key = param.getKey();
      final String value = param.getValue();
      try {
        final Expression expression = JexlUtil.newExpression(value, "#\\{([^\\}]+)\\}");
        this.expressions.put(key, expression);
      } catch (final Exception e) {
        throw new IllegalArgumentException("Expression not valid " + key + "=" + value);
      }
    }
  }

  public void setScript(final String script) {
    this.script = script;
  }

}
