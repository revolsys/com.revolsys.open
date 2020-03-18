package com.revolsys.swing.listener;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public class WeakFocusListener implements FocusListener {

  private final Reference<FocusListener> reference;

  public WeakFocusListener(final FocusListener listener) {
    this.reference = new WeakReference<>(listener);
  }

  @Override
  public void focusGained(final FocusEvent e) {
    final FocusListener listener = this.reference.get();
    if (listener != null) {
      listener.focusGained(e);
    }
  }

  @Override
  public void focusLost(final FocusEvent e) {
    final FocusListener listener = this.reference.get();
    if (listener != null) {
      listener.focusLost(e);
    }
  }
}
