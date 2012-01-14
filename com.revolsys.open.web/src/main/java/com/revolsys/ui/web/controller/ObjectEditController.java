package com.revolsys.ui.web.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.revolsys.gis.data.io.DataAccessObject;
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.form.Form;
import com.revolsys.ui.html.view.MenuElement;
import com.revolsys.ui.model.Menu;

public class ObjectEditController extends BaseController {

  private DataAccessObject<?> dataAccessObject;

  private HtmlUiBuilder htmlUiBuilder;

  @Override
  protected void initApplicationContext() throws BeansException {
    Menu actionMenu = getActionMenu();
    int index = 0;
    actionMenu.addMenuItem(index++, new Menu("Save",
      "javascript:document.forms['editObject'].submit()"));
    actionMenu.addMenuItem(index++, new Menu("Cancel",
      htmlUiBuilder.getPageUrl("view")));
  }

  @SuppressWarnings("unchecked")
  protected ModelAndView handleRequestInternal(HttpServletRequest request,
    HttpServletResponse response) throws Exception {
    String path = request.getPathInfo();
    int endIndex = path.lastIndexOf('/');
    if (endIndex != -1) {
      int startIndex = path.lastIndexOf('/', endIndex - 1);
      if (startIndex != -1) {
        String idString = path.substring(startIndex + 1, endIndex);
        try {
          long id = Long.parseLong(idString);
          Object object = dataAccessObject.load(id);
          if (object != null) {
            Form form = htmlUiBuilder.createForm(object, "editObject", "edit",
              request.getLocale());
            form.initialize(request);

            if (form.isPosted() && form.isMainFormTask()) {
              if (form.isValid()) {
                Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put("id", id);
                String url = htmlUiBuilder.getPageUrl("view", parameters);
                return new ModelAndView(new RedirectView(url));
              }
            }

            Map<String, Object> model = new HashMap<String, Object>();
            model.put("title", "Edit " + htmlUiBuilder.getTitle() + " #"
              + idString);
            model.put("uiBuilder", htmlUiBuilder);
            MenuElement actionMenu = getActionMenuElement(request);
            model.put("view", Arrays.asList(form, actionMenu));
            model.put("id", id);
            return new ModelAndView("view", model);
          }
        } catch (NumberFormatException e) {
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND,
          htmlUiBuilder.getTitle() + " #" + idString + " does not exist");
        return null;
      }
    }
    response.sendError(HttpServletResponse.SC_NOT_FOUND,
      htmlUiBuilder.getTitle() + " does not exist");
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
