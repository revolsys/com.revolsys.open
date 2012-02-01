package com.revolsys.ui.web.controller;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.util.UrlPathHelper;

import com.revolsys.io.FileUtil;

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
    final HttpServletResponse response) throws Exception {
    String path = urlPathHelper.getOriginatingRequestUri(request);

    String contextPath = urlPathHelper.getOriginatingContextPath(request);
    path = path.substring(contextPath.length());
    String pathNoExt = FileUtil.getBaseName(path);

    String viewName = this.viewName;
    viewName = viewName.replaceAll("\\[PATH\\]", path);
    viewName = viewName.replaceAll("\\[PATH-NO-EXT\\]", pathNoExt);

    final ModelAndView view = new ModelAndView(viewName);
    for (Entry<String, Object> attribute : attributes.entrySet()) {
      String attributeName = attribute.getKey();
      Object attributeValue = attribute.getValue();
      if (attributeValue instanceof String) {
        attributeValue = ((String)attributeValue).replaceAll("\\[PATH\\]", path);
        attributeValue = ((String)attributeValue).replaceAll(
          "\\[PATH-NO-EXT\\]", pathNoExt);
      }
      view.addObject(attributeName, attributeValue);
    }
    if (!viewName.startsWith("redirect:")) {
      view.addObject("wrapHtml", Boolean.FALSE);
    }
    return view;
  }

  public void setAttributes(final Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  public void setViewName(final String viewName) {
    this.viewName = viewName;
  }

}
