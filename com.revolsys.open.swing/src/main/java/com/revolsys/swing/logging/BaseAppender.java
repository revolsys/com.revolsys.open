package com.revolsys.swing.logging;

import java.io.Serializable;

import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.Layout;

public abstract class BaseAppender extends AbstractLifeCycle implements Appender {

  private String name;

  private Layout<? extends Serializable> layout;

  private ErrorHandler handler;

  private boolean ignoreExceptions;

  public BaseAppender() {
  }

  @Override
  public ErrorHandler getHandler() {
    return this.handler;
  }

  @Override
  public Layout<? extends Serializable> getLayout() {
    return this.layout;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public boolean ignoreExceptions() {
    return this.ignoreExceptions;
  }

  @Override
  public void initialize() {
  }

  @Override
  public void setHandler(final ErrorHandler handler) {
    this.handler = handler;
  }

  public void setName(final String name) {
    this.name = name;
  }

}
