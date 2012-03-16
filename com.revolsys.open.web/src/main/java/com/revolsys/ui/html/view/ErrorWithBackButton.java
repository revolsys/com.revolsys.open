package com.revolsys.ui.html.view;

import com.revolsys.ui.model.Menu;

public class ErrorWithBackButton extends ElementContainer {
  public ErrorWithBackButton(String message) {
    add(new DivElement("error", message));
    final Menu actionMenu = new Menu();
    actionMenu.addMenuItem("Back", "javascript:history.go(-1)");
    add(new MenuElement(actionMenu, "actionMenu"));
  }
}
