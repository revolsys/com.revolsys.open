package com.revolsys.ui.html.taglib;

import com.revolsys.ui.web.taglib.AbstractElementTag;

public class ScriptsTag extends AbstractElementTag {
  public ScriptsTag() {
    super("${rsWebController.scripts}");
  }
}
