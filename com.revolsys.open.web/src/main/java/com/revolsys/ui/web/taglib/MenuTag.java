package com.revolsys.ui.web.taglib;

import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Logger;

import com.revolsys.ui.html.view.MenuElement;
import com.revolsys.ui.model.Menu;

public class MenuTag extends AbstractMapElementTag {
  private final MenuElement element = new MenuElement();

  public MenuTag() {
    super("${requestScope.rsWebController.menus}");
  }

  /**
   * @return Returns the cssClass.
   */
  public String getCssClass() {
    return element.getCssClass();
  }

  /**
   * @return Returns the numLevels.
   */
  public int getNumLevels() {
    return element.getNumLevels();
  }

  /**
   * @return Returns the showRoot.
   */
  public boolean isShowRoot() {
    return element.isShowRoot();
  }

  @Override
  protected void serializeObject(final Writer out, final Object object)
    throws IOException {
    final Menu menu = (Menu)object;
    Logger.getLogger(MenuTag.class).debug(menu);
    element.setMenu(menu);
    element.serialize(out);
  }

  /**
   * @param cssClass The cssClass to set.
   */
  public void setCssClass(final String cssClass) {
    element.setCssClass(cssClass);
  }

  /**
   * @param numLevels The numLevels to set.
   */
  public void setNumLevels(final int numLevels) {
    element.setNumLevels(numLevels);
  }

  /**
   * @param showRoot The showRoot to set.
   */
  public void setShowRoot(final boolean showRoot) {
    element.setShowRoot(showRoot);
  }
}
