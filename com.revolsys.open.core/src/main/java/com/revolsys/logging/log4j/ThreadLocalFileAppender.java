package com.revolsys.logging.log4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.LoggingEvent;

public class ThreadLocalFileAppender extends FileAppender {
  public static ThreadLocalFileAppender getAppender() {
    return getAppender("ThreadLocalLog");
  }

  public static ThreadLocalFileAppender getAppender(final String name) {
    final Logger rootLogger = Logger.getRootLogger();
    final ThreadLocalFileAppender appender = (ThreadLocalFileAppender)rootLogger.getAppender(name);
    return appender;
  }

  private final Map<String, FileAppender> appenders = new HashMap<String, FileAppender>();

  private final ThreadLocal<FileAppender> localAppender = new ThreadLocal<FileAppender>();

  private final ThreadLocal<String> localFile = new ThreadLocal<String>();

  public ThreadLocalFileAppender() {
    super();
  }

  public ThreadLocalFileAppender(final Layout layout, final String filename)
    throws IOException {
    setLayout(layout);
    setFile(filename);
  }

  public ThreadLocalFileAppender(final Layout layout, final String filename,
    final boolean append) throws IOException {
    setLayout(layout);
    setFile(filename);
    setAppend(append);
  }

  public ThreadLocalFileAppender(final Layout layout, final String filename,
    final boolean append, final boolean bufferedIO, final int bufferSize)
    throws IOException {
    super(layout, filename, append, bufferedIO, bufferSize);
  }

  @Override
  public void activateOptions() {
  }

  @Override
  public void append(final LoggingEvent event) {
    if (hasLocalAppender()) {
      final FileAppender appender = getLocalAppender();
      appender.append(event);
    }
  }

  @Override
  public void close() {
    if (hasLocalAppender()) {
      getLocalAppender().close();
    } else {
      super.close();
    }
  }

  private FileAppender createLocalAppender() {
    final String file = getLocalFile();
    final FileAppender fileAppender = getFileAppender(file);
    localAppender.set(fileAppender);
    return fileAppender;
  }

  @Override
  public boolean getAppend() {
    if (hasLocalAppender()) {
      return getLocalAppender().getAppend();
    } else {
      return super.getAppend();
    }
  }

  @Override
  public boolean getBufferedIO() {
    if (hasLocalAppender()) {
      return getLocalAppender().getBufferedIO();
    } else {
      return super.getBufferedIO();
    }
  }

  @Override
  public int getBufferSize() {
    if (hasLocalAppender()) {
      return getLocalAppender().getBufferSize();
    } else {
      return super.getBufferSize();
    }
  }

  @Override
  public String getEncoding() {
    if (hasLocalAppender()) {
      return getLocalAppender().getEncoding();
    } else {
      return super.getEncoding();
    }
  }

  @Override
  public ErrorHandler getErrorHandler() {
    if (hasLocalAppender()) {
      return getLocalAppender().getErrorHandler();
    } else {
      return super.getErrorHandler();
    }
  }

  @Override
  public String getFile() {
    if (hasLocalAppender()) {
      return getLocalAppender().getFile();
    } else {
      return super.getFile();
    }
  }

  private synchronized FileAppender getFileAppender(final String file) {
    FileAppender appender = appenders.get(file);
    if (appender == null) {
      appender = new FileAppender();
      appender.setAppend(super.getAppend());
      appender.setBufferedIO(super.getBufferedIO());
      appender.setBufferSize(super.getBufferSize());
      appender.setEncoding(super.getEncoding());
      appender.setErrorHandler(super.getErrorHandler());
      appender.setFile(file);
      appender.setImmediateFlush(super.getImmediateFlush());
      appender.setLayout(super.getLayout());
      appender.setName(super.getName());
      appender.setThreshold(super.getThreshold());
      appender.activateOptions();
      appenders.put(file, appender);
    }
    return appender;
  }

  @Override
  public boolean getImmediateFlush() {
    if (hasLocalAppender()) {
      return getLocalAppender().getImmediateFlush();
    } else {
      return super.getImmediateFlush();
    }
  }

  @Override
  public Layout getLayout() {
    if (hasLocalAppender()) {
      return getLocalAppender().getLayout();
    } else {
      return super.getLayout();
    }
  }

  private FileAppender getLocalAppender() {
    if (hasLocalAppender()) {
      return localAppender.get();
    } else {
      return null;
    }
  }

  public String getLocalFile() {
    return localFile.get();
  }

  @Override
  public Priority getThreshold() {
    if (hasLocalAppender()) {
      return getLocalAppender().getThreshold();
    } else {
      return super.getThreshold();
    }
  }

  private boolean hasLocalAppender() {
    return localAppender.get() != null;
  }

  public void removeLocalFile() {
    final String localFileName = localFile.get();
    if (localFileName != null) {
      localAppender.set(null);
      localFile.set(null);
      final FileAppender appender = appenders.get(localFileName);
      if (appender != null) {
        appender.close();
        appenders.remove(localFileName);
      }
    }
  }

  @Override
  public boolean requiresLayout() {
    return true;
  }

  @Override
  public void setAppend(final boolean append) {
    if (hasLocalAppender()) {
      getLocalAppender().setAppend(append);
    } else {
      super.setAppend(append);
    }
  }

  @Override
  public void setBufferedIO(final boolean bufferedIO) {
    if (hasLocalAppender()) {
      getLocalAppender().setBufferedIO(bufferedIO);
    } else {
      super.setBufferedIO(bufferedIO);
    }
  }

  @Override
  public void setBufferSize(final int bufferSize) {
    if (hasLocalAppender()) {
      getLocalAppender().setBufferSize(bufferSize);
    } else {
      super.setBufferSize(bufferSize);
    }
  }

  @Override
  public void setEncoding(final String encoding) {
    if (hasLocalAppender()) {
      getLocalAppender().setEncoding(encoding);
    } else {
      super.setEncoding(encoding);
    }
  }

  @Override
  public void setErrorHandler(final ErrorHandler errorHandler) {
    if (hasLocalAppender()) {
      getLocalAppender().setErrorHandler(errorHandler);
    } else {
      super.setErrorHandler(errorHandler);
    }
  }

  @Override
  public void setFile(final String file) {
    if (hasLocalAppender()) {
      getLocalAppender().setFile(file);
    } else {
      super.setFile(file);
    }
  }

  @Override
  public void setImmediateFlush(final boolean immediateFlush) {
    if (hasLocalAppender()) {
      getLocalAppender().setImmediateFlush(immediateFlush);
    } else {
      super.setImmediateFlush(immediateFlush);
    }
  }

  @Override
  public void setLayout(final Layout layout) {
    if (hasLocalAppender()) {
      getLocalAppender().setLayout(layout);
    } else {
      super.setLayout(layout);
    }
  }

  public void setLocalFile(final String file) {
    localFile.set(file);
    if (file != null) {
      createLocalAppender();
    }
  }

  @Override
  public void setThreshold(final Priority threshold) {
    if (hasLocalAppender()) {
      getLocalAppender().setThreshold(threshold);
    } else {
      super.setThreshold(threshold);
    }
  }
}
