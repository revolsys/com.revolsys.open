package com.revolsys.ui.html.taglib;

import com.revolsys.ui.web.taglib.AbstractElementTag;

public class StylesTag extends AbstractElementTag {
  public StylesTag() {
    super("${requestScope.rsWebController.styles}");
  }
}
