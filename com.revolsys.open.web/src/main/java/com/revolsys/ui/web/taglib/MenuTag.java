package com.revolsys.ui.web.taglib;

import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Logger;

import com.revolsys.ui.html.view.MenuElement;
import com.revolsys.ui.model.Menu;

public class MenuTag extends AbstractMapElementTag {
  private MenuElement element = new MenuElement();

  public MenuTag() {
    super("${requestScope.rsWebController.menus}");
  }

  protected void serializeObject(final Writer out, final Object object)
    throws IOException {
    Menu menu = (Menu)object;
    Logger.getLogger(MenuTag.class).debug(menu);
    element.setMenu(menu);
    element.serialize(out);
  }

  /**
   * @return Returns the cssClass.
   */
  public String getCssClass() {
    return element.getCssClass();
  }

  /**
   * @param cssClass The cssClass to set.
   */
  public void setCssClass(String cssClass) {
    element.setCssClass(cssClass);
  }

  /**
   * @return Returns the numLevels.
   */
  public int getNumLevels() {
    return element.getNumLevels();
  }

  /**
   * @param numLevels The numLevels to set.
   */
  public void setNumLevels(int numLevels) {
    element.setNumLevels(numLevels);
  }

  /**
   * @return Returns the showRoot.
   */
  public boolean isShowRoot() {
    return element.isShowRoot();
  }

  /**
   * @param showRoot The showRoot to set.
   */
  public void setShowRoot(boolean showRoot) {
    element.setShowRoot(showRoot);
  }
}
