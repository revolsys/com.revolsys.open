package com.revolsys.swing.menu;

import java.awt.Window;
import java.awt.event.MouseEvent;

import com.revolsys.io.BaseCloseable;

public class MenuSourceHolder implements BaseCloseable {
  private final Object source;

  private Window window;

  private boolean menuVisible;

  private final MouseEvent event;

  public MenuSourceHolder(final Object source) {
    this(source, (MouseEvent)null);
  }

  public MenuSourceHolder(final Object source, final MouseEvent event) {
    this.source = source;
    this.event = event;
    MenuFactory.setMenuSourceHolder(this);
  }

  public MenuSourceHolder(final Object source, final Window window) {
    this(source, (MouseEvent)null);
    this.window = window;
  }

  @Override
  public void close() {
    if (!this.menuVisible) {
      closeDo();
    }
  }

  void closeDo() {
    MenuFactory.clearMenuSourceHolder(this);
  }

  public MouseEvent getEvent() {
    return this.event;
  }

  @SuppressWarnings("unchecked")
  public <S> S getSource() {
    return (S)this.source;
  }

  public Window getWindow() {
    return this.window;
  }

  void setMenuVisible(final boolean menuVisible) {
    this.menuVisible = menuVisible;
  }

  public void setWindow(final Window window) {
    this.window = window;
  }
}
