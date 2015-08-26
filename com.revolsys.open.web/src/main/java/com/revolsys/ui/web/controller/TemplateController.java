package com.revolsys.ui.web.controller;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.util.UrlPathHelper;

import com.revolsys.io.FileUtil;

public class TemplateController extends AbstractController {
  private Map<String, Object> attributes = Collections.emptyMap();

  private UrlPathHelper urlPathHelper = new UrlPathHelper();

  private String viewName;

  public TemplateController() {
    this.urlPathHelper.setAlwaysUseFullPath(true);
  }

  @PreDestroy
  public void destroy() {
    setApplicationContext(null);
    this.attributes = Collections.emptyMap();
    this.viewName = null;
    this.urlPathHelper = null;
  }

  public Map<String, Object> getFields() {
    return this.attributes;
  }

  public String getViewName() {
    return this.viewName;
  }

  @Override
  public ModelAndView handleRequestInternal(final HttpServletRequest request,
    final HttpServletResponse response) throws Exception {
    String path = this.urlPathHelper.getOriginatingRequestUri(request);

    final String contextPath = this.urlPathHelper.getOriginatingContextPath(request);
    path = path.substring(contextPath.length());
    final String pathNoExt = FileUtil.getBaseName(path);

    String viewName = this.viewName;
    viewName = viewName.replaceAll("\\[PATH\\]", path);
    viewName = viewName.replaceAll("\\[PATH-NO-EXT\\]", pathNoExt);

    final ModelAndView view = new ModelAndView(viewName);
    for (final Entry<String, Object> attribute : this.attributes.entrySet()) {
      final String attributeName = attribute.getKey();
      Object attributeValue = attribute.getValue();
      if (attributeValue instanceof String) {
        attributeValue = ((String)attributeValue).replaceAll("\\[PATH\\]", path);
        attributeValue = ((String)attributeValue).replaceAll("\\[PATH-NO-EXT\\]", pathNoExt);
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
