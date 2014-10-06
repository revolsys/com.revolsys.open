package com.revolsys.ui.html.builder;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.ui.html.fields.CheckBoxField;
import com.revolsys.ui.html.fields.TextAreaField;
import com.revolsys.ui.html.fields.TextField;
import com.revolsys.ui.html.view.Element;

public class AbstractTypeUiBuilder extends HtmlUiBuilder {
  public AbstractTypeUiBuilder(final String typePath, final String typeLabel) {
    super(typePath, typeLabel);
  }

  @Override
  public Element getAttribute(final HttpServletRequest request, final String key) {
    if (key.equals("active")) {
      return new CheckBoxField(key, true);
    } else if (key.equals("name")) {
      return new TextField(key, 40, true);
    } else if (key.equals("description")) {
      return new TextAreaField(key, 40, 4, false);
    }
    return super.getAttribute(request, key);
  }
}
