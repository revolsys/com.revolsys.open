package com.revolsys.parallel.tools;

import java.io.File;
import java.io.IOException;

import javax.annotation.PreDestroy;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.revolsys.logging.log4j.ThreadLocalFileAppender;

public class ThreadLocalAppenderBean implements BeanFactoryPostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(ThreadLocalAppenderBean.class);

  private File logFile;

  @PreDestroy
  public void close() {
    ThreadLocalFileAppender.getAppender().setLocalFile(null);
  }

  public File getLogFile() {
    return this.logFile;
  }

  @Override
  public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory)
    throws BeansException {
    final ThreadLocalFileAppender localAppender = ThreadLocalFileAppender.getAppender();
    final String logFilePath = this.logFile.getAbsolutePath();
    if (localAppender != null && this.logFile != null) {
      final File parentFile = this.logFile.getParentFile();
      if (parentFile != null) {
        parentFile.mkdirs();
      }
      localAppender.setLocalFile(logFilePath);
    } else if (this.logFile != null) {

      final org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
      try {
        final Layout layout = new PatternLayout("%d\t%p\t%m%n");
        final Appender appender = new ThreadLocalFileAppender(layout, logFilePath, false);
        appender.setName("ThreadLocalLog");
        rootLogger.addAppender(appender);
      } catch (final IOException e) {
        final Layout layout = new PatternLayout("%p\t%m%n");
        final Appender appender = new ConsoleAppender(layout);
        rootLogger.addAppender(appender);
        LOG.error("Cannot find log file " + this.logFile, e);
      }
    }
  }

  public void setLogFile(final File logFile) {
    this.logFile = logFile;
  }
}
