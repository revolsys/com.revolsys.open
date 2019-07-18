package com.revolsys.ui.web.taglib;

import java.io.IOException;
import java.io.Writer;

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
    return this.element.getCssClass();
  }

  /**
   * @return Returns the numLevels.
   */
  public int getNumLevels() {
    return this.element.getNumLevels();
  }

  /**
   * @return Returns the showRoot.
   */
  public boolean isShowRoot() {
    return this.element.isShowRoot();
  }

  @Override
  protected void serializeObject(final Writer out, final Object object) throws IOException {
    final Menu menu = (Menu)object;
    this.element.setMenu(menu);
    this.element.serialize(out);
  }

  /**
   * @param cssClass The cssClass to set.
   */
  public void setCssClass(final String cssClass) {
    this.element.setCssClass(cssClass);
  }

  /**
   * @param numLevels The numLevels to set.
   */
  public void setNumLevels(final int numLevels) {
    this.element.setNumLevels(numLevels);
  }

  /**
   * @param showRoot The showRoot to set.
   */
  public void setShowRoot(final boolean showRoot) {
    this.element.setShowRoot(showRoot);
  }
}
