package com.revolsys.gis.parallel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.context.HashMapContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import com.revolsys.collection.ThreadSharedAttributes;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMap;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;
import com.revolsys.parallel.process.BaseInProcess;
import com.revolsys.parallel.tools.ScriptExecutorRunnable;
import com.revolsys.util.JexlUtil;

public class ScriptExecutorProcess extends BaseInProcess<DataObject> implements
  BeanFactoryAware {
  private static final Logger LOG = LoggerFactory.getLogger(ScriptExecutorProcess.class);

  private final Map<String, Object> attributes = new HashMap<String, Object>();

  private ExecutorService executor;

  private final Map<String, Expression> expressions = new HashMap<String, Expression>();

  private int maxConcurrentScripts = 1;

  private Map<String, String> parameters = new HashMap<String, String>();

  private String script;

  private final Set<Future<?>> tasks = new LinkedHashSet<Future<?>>();

  @Override
  protected void destroy() {
    try {
      while (!tasks.isEmpty()) {
        try {
          synchronized (this) {
            wait(1000);
            for (final Iterator<Future<?>> taskIter = tasks.iterator(); taskIter.hasNext();) {
              final Future<?> task = taskIter.next();
              if (task.isDone()) {
                taskIter.remove();
              }
            }
          }
        } catch (final InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    } finally {
      executor.shutdown();
    }
  }

  private void executeScript(final DataObject object) {
    try {
      final JexlContext context = new HashMapContext();
      final Map<String, Object> vars = new HashMap<String, Object>(attributes);
      vars.putAll(new DataObjectMap(object));
      context.setVars(vars);
      final Map<String, Object> scriptParams = new HashMap<String, Object>();
      scriptParams.putAll(attributes);
      for (final Entry<String, Expression> param : expressions.entrySet()) {
        final String key = param.getKey();
        final Expression expression = param.getValue();
        final Object value = JexlUtil.evaluateExpression(context, expression);
        scriptParams.put(key, value);
      }
      final ScriptExecutorRunnable scriptRunner = new ScriptExecutorRunnable(
        script, scriptParams);
      if (executor == null) {
        scriptRunner.run();
      } else {
        while (tasks.size() >= maxConcurrentScripts) {
          try {
            synchronized (this) {
              wait(1000);
              for (final Iterator<Future<?>> taskIter = tasks.iterator(); taskIter.hasNext();) {
                final Future<?> task = taskIter.next();
                if (task.isDone()) {
                  taskIter.remove();
                }
              }
            }
          } catch (final InterruptedException e) {
            throw new ClosedException(e);
          }
        }
        final Future<?> future = executor.submit(scriptRunner);
        tasks.add(future);
      }
    } catch (final ThreadDeath e) {
      throw e;
    } catch (final Throwable t) {
      LOG.error(t.getMessage(), t);
    }
  }

  public ExecutorService getExecutor() {
    return executor;
  }

  public int getMaxConcurrentScripts() {
    return maxConcurrentScripts;
  }

  public Map<String, String> getParameters() {
    return parameters;
  }

  public String getScript() {
    return script;
  }

  @Override
  protected void postRun(final Channel<DataObject> in) {
    tasks.clear();
    if (executor != null) {
      executor.shutdownNow();
    }
  }

  @Override
  protected void process(final Channel<DataObject> in, final DataObject object) {
    executeScript(object);
  }

  @Override
  public void setBeanFactory(final BeanFactory beanFactory)
    throws BeansException {
    attributes.putAll(ThreadSharedAttributes.getAttributes());
  }

  public void setExecutor(final ExecutorService executor) {
    this.executor = executor;
  }

  public void setMaxConcurrentScripts(final int maxConcurrentScripts) {
    this.maxConcurrentScripts = maxConcurrentScripts;
    if (executor == null) {
      executor = new ThreadPoolExecutor(Math.min(maxConcurrentScripts, 10),
        maxConcurrentScripts, 10, TimeUnit.SECONDS,
        new LinkedBlockingQueue<Runnable>());
    }
  }

  public void setParameters(final Map<String, String> parameters) {
    this.parameters = parameters;
    for (final Entry<String, String> param : parameters.entrySet()) {
      final String key = param.getKey();
      final String value = param.getValue();
      try {
        final Expression expression = JexlUtil.createExpression(value,
          "#\\{([^\\}]+)\\}");
        expressions.put(key, expression);
      } catch (final Exception e) {
        throw new IllegalArgumentException("Expression not valid " + key + "="
          + value);
      }
    }
  }

  public void setScript(final String script) {
    this.script = script;
  }

}
