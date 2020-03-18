package com.revolsys.swing.events;

import java.awt.event.KeyEvent;

public class KeyEvents {

  public static boolean altKey(final KeyEvent event, final int keyCode) {
    final int keyCodeEvent = event.getKeyCode();
    return event.isAltDown() && keyCode == keyCodeEvent;
  }

  public static boolean controlKey(final KeyEvent event, final int keyCode) {
    final int keyCodeEvent = event.getKeyCode();
    return event.isControlDown() && keyCode == keyCodeEvent;
  }
}
