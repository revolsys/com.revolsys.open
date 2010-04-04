package com.revolsys.ui.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.mvc.AbstractController;

import com.revolsys.ui.html.view.MenuElement;
import com.revolsys.ui.model.Menu;

public abstract class BaseController extends AbstractController {
  private Menu actionMenu = new Menu();

  public Menu getActionMenu(final HttpServletRequest request) {
    Menu requestMenu = (Menu)request.getAttribute("actionMenu");
    if (requestMenu == null) {
      return actionMenu;
    } else {
      Menu newMenu = actionMenu.clone();
      newMenu.addAllMenuItems(requestMenu);
      return newMenu;
    }
  }

  public MenuElement getActionMenuElement(final HttpServletRequest request) {
    Menu menu = getActionMenu(request);
    MenuElement menuElement = new MenuElement(menu, "actionMenu");
    menuElement.initialize(request);
    return menuElement;
  }

  public Menu getActionMenu() {
    return actionMenu;
  }

  public void setActionMenu(final Menu actionMenu) {
    this.actionMenu = actionMenu;
  }
}
