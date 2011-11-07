package com.revolsys.ui.web.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.web.servlet.ModelAndView;

import com.revolsys.orm.core.DataAccessObject;
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.MenuElement;
import com.revolsys.ui.model.Menu;

public class ObjectViewController extends BaseController {

  private DataAccessObject<?> dataAccessObject;

  private HtmlUiBuilder htmlUiBuilder;

  @Override
  protected void initApplicationContext() throws BeansException {
    Menu actionMenu = getActionMenu();
    int index = 0;
    if (htmlUiBuilder.hasPageUrl("edit")) {
      actionMenu.addMenuItem(index++, new Menu("Edit",
        htmlUiBuilder.getPageUrl("edit")));
    }
  }

  @SuppressWarnings("unchecked")
  protected ModelAndView handleRequestInternal(HttpServletRequest request,
    HttpServletResponse response) throws Exception {
    String path = request.getPathInfo();
    int index = path.lastIndexOf('/');
    if (index != -1) {
      String idString = path.substring(index + 1);
      try {
        long id = Long.parseLong(idString);
        Object object = dataAccessObject.load(id);
        if (object != null) {
          Element listView = htmlUiBuilder.createDetailView(object,
            "objectView", "view", request.getLocale());
          Map<String, Object> model = new HashMap<String, Object>();
          model.put("title", htmlUiBuilder.getTitle() + " #" + idString);
          MenuElement actionMenu = getActionMenuElement(request);
          model.put("view", Arrays.asList(listView, actionMenu));
          model.put("id", id);
          return new ModelAndView("view", model);
        }
      } catch (NumberFormatException e) {
      }
      response.sendError(HttpServletResponse.SC_NOT_FOUND,
        htmlUiBuilder.getTitle() + " #" + idString + " does not exist");
    } else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND,
        htmlUiBuilder.getTitle() + " does not exist");
    }
    return null;
  }

  public DataAccessObject<?> getDataAccessObject() {
    return dataAccessObject;
  }

  public void setDataAccessObject(final DataAccessObject<?> dataAccessObject) {
    this.dataAccessObject = dataAccessObject;
  }

  public HtmlUiBuilder getHtmlUiBuilder() {
    return htmlUiBuilder;
  }

  public void setHtmlUiBuilder(final HtmlUiBuilder htmlUiBuilder) {
    this.htmlUiBuilder = htmlUiBuilder;
  }

}
