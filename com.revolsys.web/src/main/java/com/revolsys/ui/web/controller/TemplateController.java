package com.revolsys.ui.web.controller;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class TemplateController extends AbstractController {
  private Map<String, Object> attributes = Collections.emptyMap();

  private String viewName;

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public String getViewName() {
    return viewName;
  }

  @Override
  public ModelAndView handleRequestInternal(
    final HttpServletRequest request,
    final HttpServletResponse response)
    throws Exception {  
    final ModelAndView view = new ModelAndView(viewName);
    for (Entry<String,Object> attribute : attributes.entrySet()) {
      String attributeName =attribute.getKey();
      Object attributeValue = attribute.getValue();
      if (attributeValue instanceof String) {
        attributeValue = ((String)attributeValue).replaceAll("\\[PATH\\]", request.getPathInfo());
      }
      view.addObject(attributeName, attributeValue);
    }
    view.addObject("wrapHtml", Boolean.FALSE);
    return view;
  }

  public void setAttributes(
    final Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  public void setViewName(
    final String viewName) {
    this.viewName = viewName;
  }

}
