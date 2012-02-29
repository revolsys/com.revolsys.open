package com.revolsys.ui.html.builder;

import org.apache.commons.jexl.JexlContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import com.revolsys.ui.model.Menu;
import com.revolsys.ui.web.config.Page;

public class HtmlUiBuilderMenu extends Menu implements BeanFactoryAware {
  private BeanFactory beanFactory;

  private String typeName;

  private String pageName;

  @Override
  public String getLink(final JexlContext context) {
    final HtmlUiBuilder<Object> htmlUiBuilder = HtmlUiBuilderFactory.get(
      beanFactory, typeName);
    if (htmlUiBuilder == null) {
      return null;
    } else {
      final String link = htmlUiBuilder.getPageUrl(pageName, getParameters());
      if (link == null) {
        return null;
      } else {
        final String anchor = getAnchor();
        if (anchor == null) {
          return link;
        } else {
          return link + "#" + anchor;
        }
      }
    }
  }

  @Override
  public boolean isVisible() {
    final HtmlUiBuilder<Object> htmlUiBuilder = HtmlUiBuilderFactory.get(
      beanFactory, typeName);
    if (htmlUiBuilder == null) {
      return false;
    } else {
      return htmlUiBuilder.getPageUrl(pageName, getParameters()) != null;
    }
  }

  @Override
  public String getLinkTitle(final JexlContext context) {
    final HtmlUiBuilder<Object> htmlUiBuilder = HtmlUiBuilderFactory.get(
      beanFactory, typeName);
    if (htmlUiBuilder == null) {
      return null;
    } else {
      Page page = htmlUiBuilder.getPage(pageName);
      if (page == null) {
        page = new Page(null, htmlUiBuilder.getPluralTitle(), pageName, false);
      }
      return page.getExpandedTitle();
    }
  }

  public String getPageName() {
    return pageName;
  }

  public String getTypeName() {
    return typeName;
  }

  public void setBeanFactory(final BeanFactory beanFactory)
    throws BeansException {
    this.beanFactory = beanFactory;
  }

  public void setPageName(final String pageName) {
    this.pageName = pageName;
  }

  public void setTypeName(final String typeName) {
    this.typeName = typeName;
  }
}
