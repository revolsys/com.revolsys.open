package com.revolsys.collection;

import java.util.List;

import com.revolsys.util.IconNameProxy;

public interface Parent<C> extends IconNameProxy {
  List<C> getChildren();

  @Override
  default String getIconName() {
    return "folder";
  }

  default void refresh() {
  }
}
