package com.revolsys.ui.web.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.revolsys.orm.core.DataAccessObject;
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.form.Form;
import com.revolsys.ui.html.view.MenuElement;
import com.revolsys.ui.model.Menu;

public class ObjectAddController extends BaseController {

  private DataAccessObject<Object> dataAccessObject;

  private HtmlUiBuilder htmlUiBuilder;

  @Override
  protected void initApplicationContext() throws BeansException {
    Menu actionMenu = getActionMenu();
    int index = 0;
    actionMenu.addMenuItem(index++, new Menu("Add",
      "javascript:document.forms['addObject'].submit()"));
    actionMenu.addMenuItem(index++, new Menu("Cancel",
      htmlUiBuilder.getPageUrl("list")));
  }

  @SuppressWarnings("unchecked")
  protected ModelAndView handleRequestInternal(HttpServletRequest request,
    HttpServletResponse response) throws Exception {
    Object object = dataAccessObject.createInstance();

    Form form = htmlUiBuilder.createForm(object, "addObject", "add",
      request.getLocale());
    form.initialize(request);

    if (form.isPosted() && form.isMainFormTask()) {
      if (form.isValid()) {
        dataAccessObject.persist(object);
        Object id = BeanUtils.getSimpleProperty(object, "id");
        Map<String, Object> parameters = Collections.singletonMap("id", id);
        RedirectView url = new RedirectView(htmlUiBuilder.getPageUrl("view",
          parameters));
        return new ModelAndView(url);
      }
    }

    Map<String, Object> model = new HashMap<String, Object>();
    model.put("title", "Add " + htmlUiBuilder.getTitle());
    model.put("uiBuilder", htmlUiBuilder);
    MenuElement actionMenu = getActionMenuElement(request);
    model.put("view", Arrays.asList(form, actionMenu));
    return new ModelAndView("view", model);
  }

  public DataAccessObject<?> getDataAccessObject() {
    return dataAccessObject;
  }

  public void setDataAccessObject(
    final DataAccessObject<Object> dataAccessObject) {
    this.dataAccessObject = dataAccessObject;
  }

  public HtmlUiBuilder getHtmlUiBuilder() {
    return htmlUiBuilder;
  }

  public void setHtmlUiBuilder(final HtmlUiBuilder htmlUiBuilder) {
    this.htmlUiBuilder = htmlUiBuilder;
  }

}
