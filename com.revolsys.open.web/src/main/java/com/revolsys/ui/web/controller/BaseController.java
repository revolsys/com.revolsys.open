package com.revolsys.ui.web.controller;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.mvc.AbstractController;

import com.revolsys.ui.html.view.MenuElement;
import com.revolsys.ui.model.Menu;

public abstract class BaseController extends AbstractController {
  private Menu actionMenu = new Menu();

  @PreDestroy
  public void destroy() {
    setApplicationContext(null);
    this.actionMenu = null;
  }

  public Menu getActionMenu() {
    return this.actionMenu;
  }

  public Menu getActionMenu(final HttpServletRequest request) {
    final Menu requestMenu = (Menu)request.getAttribute("actionMenu");
    if (requestMenu == null) {
      return this.actionMenu;
    } else {
      final Menu newMenu = this.actionMenu.clone();
      newMenu.addAllMenuItems(requestMenu);
      return newMenu;
    }
  }

  public MenuElement getActionMenuElement(final HttpServletRequest request) {
    final Menu menu = getActionMenu(request);
    final MenuElement menuElement = new MenuElement(menu, "actionMenu");
    menuElement.initialize(request);
    return menuElement;
  }

  public void setActionMenu(final Menu actionMenu) {
    this.actionMenu = actionMenu;
  }
}
