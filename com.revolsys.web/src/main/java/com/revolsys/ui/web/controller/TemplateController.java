package com.revolsys.ui.web.controller;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.util.UrlPathHelper;

public class TemplateController extends AbstractController {
  private Map<String, Object> attributes = Collections.emptyMap();

  private String viewName;

  private UrlPathHelper urlPathHelper = new UrlPathHelper();

  public TemplateController() {
    urlPathHelper.setAlwaysUseFullPath(true);
  }

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
    String path = urlPathHelper.getOriginatingRequestUri(request);
    String contextPath = urlPathHelper.getOriginatingContextPath(request);
    path = path.substring(contextPath.length());

    String viewName = this.viewName;
    viewName = viewName.replaceAll("\\[PATH\\]", path);
    
    final ModelAndView view = new ModelAndView(viewName);
    for (Entry<String, Object> attribute : attributes.entrySet()) {
      String attributeName = attribute.getKey();
      Object attributeValue = attribute.getValue();
      if (attributeValue instanceof String) {
         attributeValue = ((String)attributeValue).replaceAll("\\[PATH\\]", path);
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
