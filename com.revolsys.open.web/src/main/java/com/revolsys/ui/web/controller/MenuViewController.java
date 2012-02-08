package com.revolsys.ui.web.controller;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.revolsys.ui.html.view.MenuElement;
import com.revolsys.ui.model.Menu;

@Controller
public class MenuViewController {

  @RequestMapping("/view/menu/{menuName}")
  public void getMenu(
    HttpServletRequest request,
    HttpServletResponse response,
    @PathVariable String menuName) throws IOException {
    Menu menu = (Menu)request.getAttribute(menuName);
    if (menu != null) {
      MenuElement menuElement = new MenuElement(menu, menuName);
      OutputStream out = response.getOutputStream();
      menuElement.serialize(out);
    }
  }
}
