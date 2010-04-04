package com.revolsys.parallel.tools;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.MethodInvocationException;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.PropertyBatchUpdateException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;

import com.revolsys.logging.log4j.ThreadLocalFileAppender;
import com.revolsys.parallel.process.ProcessNetwork;
import com.revolsys.parallel.process.ResourceEditorRegistrar;

public class ScriptExecutorRunnable implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(ScriptExecutorRunnable.class);

  private static Throwable getBeanExceptionCause(
    BeanCreationException e) {
    Throwable cause = e.getCause();
    while (cause instanceof BeanCreationException
      || cause instanceof MethodInvocationException
      || cause instanceof PropertyAccessException
      || cause instanceof PropertyBatchUpdateException
      || cause instanceof InvalidPropertyException) {
      Throwable newCause;
      if (cause instanceof PropertyBatchUpdateException) {
        PropertyBatchUpdateException batchEx = (PropertyBatchUpdateException)cause;
        newCause = batchEx.getPropertyAccessExceptions()[0];
      } else {
        newCause = cause.getCause();
      }
      if (newCause != null) {
        cause = newCause;
      } else {
        return cause;
      }
    }
    return cause;
  }

  private Map<String, Object> attributes;

  private String script;

  public ScriptExecutorRunnable(
    String script,
    Map<String, Object> attributes) {
    this.script = script;
    this.attributes = attributes;
  }

  public void run() {
    long startTime = System.currentTimeMillis();
    try {
      StringBuffer message = new StringBuffer("Processing ");
      message.append(" -s ");
      message.append(script);
      String logFileName = (String)attributes.get("logFile");
      if (logFileName != null && logFileName.trim().length() > 0) {
        File logFile = new File(logFileName);
        File parentFile = logFile.getParentFile();
        if (parentFile != null) {
          parentFile.mkdirs();
        }
        ThreadLocalFileAppender.getAppender().setLocalFile(
          logFile.getAbsolutePath());
        message.append(" -l ");
        message.append(logFile.getAbsolutePath());
      }
      for (Entry<String, Object> parameter : attributes.entrySet()) {
        message.append(" ");
        message.append(parameter.getKey());
        message.append("=");
        message.append(parameter.getValue());
      }
      LOG.info(message.toString());
      ThreadSharedAttributes.setAttributes(attributes);

      GenericApplicationContext beans = new GenericApplicationContext();
      beans.getBeanFactory().addPropertyEditorRegistrar(
        new ResourceEditorRegistrar());

      if (new File(script).exists()) {
        new XmlBeanDefinitionReader(beans).loadBeanDefinitions("file:" + script);
      } else {
        new XmlBeanDefinitionReader(beans).loadBeanDefinitions("classpath:"
          + script);
      }
      beans.refresh();
      try {
        Object bean = beans.getBean("com.revolsys.parallel.process.ProcessNetwork");
        ProcessNetwork pipeline = (ProcessNetwork)bean;
        pipeline.startAndWait();
      } finally {
        beans.close();
      }
    } catch (BeanCreationException e) {
      Throwable cause = getBeanExceptionCause(e);
      LOG.error(cause.getMessage(), cause);
      System.err.println(cause.getMessage());
      System.err.flush();
    } catch (Throwable t) {
      LOG.error(t.getMessage(), t);
    }
    long endTime = System.currentTimeMillis();
    long time = endTime - startTime;
    long seconds = time / 1000;
    long minutes = seconds / 60;
    seconds = seconds % 60;
    LOG.info(minutes + " minutes " + seconds + " seconds");
  }
}
