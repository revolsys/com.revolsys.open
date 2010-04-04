package com.revolsys.jump.ui.builder;

import com.vividsolutions.jump.workbench.ui.GUIUtil;

public class StringUiBuilder extends AbstractUiBuilder {

  public void appendHtml(final StringBuffer s, final Object object) {
    if (object != null) {
      s.append(GUIUtil.escapeHTML(object.toString(), false, false));
    }
  }
}
