package com.revolsys.ui.html.builder;

import org.apache.commons.jexl3.JexlContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import com.revolsys.ui.model.Menu;
import com.revolsys.ui.web.config.Page;
import com.revolsys.util.Property;

public class HtmlUiBuilderMenu extends Menu implements BeanFactoryAware {
  private BeanFactory beanFactory;

  private String pageName;

  private String typePath;

  @Override
  public String getLink(final JexlContext context) {
    final HtmlUiBuilder<Object> htmlUiBuilder = HtmlUiBuilderFactory.get(this.beanFactory,
      this.typePath);
    if (htmlUiBuilder == null) {
      return null;
    } else {
      final String link = htmlUiBuilder.getPageUrl(this.pageName, getParameters());
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
  public String getLinkTitle(final JexlContext context) {
    final String title = getTitle();
    if (Property.hasValue(title)) {
      return title;
    } else {
      final HtmlUiBuilder<Object> htmlUiBuilder = HtmlUiBuilderFactory.get(this.beanFactory,
        this.typePath);
      if (htmlUiBuilder == null) {
        return null;
      } else {
        Page page = htmlUiBuilder.getPage(this.pageName);
        if (page == null) {
          page = new Page(null, htmlUiBuilder.getPluralTitle(), this.pageName, false);
        }
        return page.getExpandedTitle();
      }
    }
  }

  public String getPageName() {
    return this.pageName;
  }

  public String getTypeName() {
    return this.typePath;
  }

  @Override
  public boolean isVisible() {
    final HtmlUiBuilder<Object> htmlUiBuilder = HtmlUiBuilderFactory.get(this.beanFactory,
      this.typePath);
    if (htmlUiBuilder == null) {
      return false;
    } else {
      return htmlUiBuilder.getPageUrl(this.pageName, getParameters()) != null;
    }
  }

  @Override
  public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }

  public void setPageName(final String pageName) {
    this.pageName = pageName;
  }

  public void setTypeName(final String typePath) {
    this.typePath = typePath;
  }
}
