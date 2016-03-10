package com.revolsys.ui.html.view;

import com.revolsys.ui.model.Menu;

public class ErrorWithBackButton extends ElementContainer {
  public ErrorWithBackButton(final String message) {
    add(new DivElement("alert alert-danger", message).setRole("alert"));
    final Menu actionMenu = new Menu();
    actionMenu.addMenuItem("Back", "javascript:history.go(-1)");
    final ButtonsToolbarElement buttonsToolbar = new ButtonsToolbarElement(actionMenu);
    add(buttonsToolbar);
  }
}
