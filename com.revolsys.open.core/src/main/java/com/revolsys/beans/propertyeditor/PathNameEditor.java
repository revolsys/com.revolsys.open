package com.revolsys.beans.propertyeditor;

import java.beans.PropertyEditorSupport;

import com.revolsys.io.PathName;

public class PathNameEditor extends PropertyEditorSupport {

  @Override
  public String getAsText() {
    final PathName value = (PathName)getValue();
    if (value == null) {
      return "";
    } else {
      return value.toString();
    }
  }

  @Override
  public void setAsText(final String text) throws IllegalArgumentException {
    final PathName pathName = PathName.create(text);
    setValue(pathName);
  }

}
