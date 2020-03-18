package com.revolsys.swing.events;

import java.awt.event.MouseEvent;

public class MouseEvents {

  public static boolean altButton(final MouseEvent event, final int button) {
    final int eventButton = event.getButton();
    return eventButton == button && event.isAltDown();
  }

}
