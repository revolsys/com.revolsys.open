package com.revolsys.ui.html.builder;

import org.apache.commons.jexl.JexlContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import com.revolsys.ui.model.Menu;
import com.revolsys.ui.web.config.Page;

public class HtmlUiBuilderMenu extends Menu implements BeanFactoryAware {
  private BeanFactory beanFactory;

  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }

  private String typeName;

  private String pageName;

  @Override
  public String getLink(JexlContext context) {
    final HtmlUiBuilder<Object> htmlUiBuilder = HtmlUiBuilderFactory.get(
      beanFactory, typeName);
    if (htmlUiBuilder == null) {
      return null;
    } else {
      String link = htmlUiBuilder.getPageUrl(pageName, getParameters());
      if (link == null) {
        return null;
      } else {
        String anchor = getAnchor();
        if (anchor == null) {
          return link;
        } else {
          return link + "#" + anchor;
        }
      }
    }
  }

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public String getPageName() {
    return pageName;
  }

  public void setPageName(String pageName) {
    this.pageName = pageName;
  }

  @Override
  public String getLinkTitle(JexlContext context) {
    final HtmlUiBuilder<Object> htmlUiBuilder = HtmlUiBuilderFactory.get(
      beanFactory, typeName);
    if (htmlUiBuilder == null) {
      return null;
    } else {
      Page page = htmlUiBuilder.getPage(pageName);
      return page.getExpandedTitle();
    }
  }
}
