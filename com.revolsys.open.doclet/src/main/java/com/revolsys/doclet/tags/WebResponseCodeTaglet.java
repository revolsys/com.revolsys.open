package com.revolsys.doclet.tags;

import java.util.Map;

import com.sun.tools.doclets.Taglet;

public class WebResponseCodeTaglet implements Taglet {

  private static final String NAME = "web.response.code";

  public static void register(final Map tagletMap) {
    final WebResponseCodeTaglet tag = new WebResponseCodeTaglet();
    final String name = tag.getName();
    final Taglet t = (Taglet)tagletMap.get(name);
    if (t != null) {
      tagletMap.remove(name);
    }
    tagletMap.put(name, tag);
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean inConstructor() {
    return false;
  }

  @Override
  public boolean inField() {
    return false;
  }

  @Override
  public boolean inMethod() {
    return true;
  }

  @Override
  public boolean inOverview() {
    return false;
  }

  @Override
  public boolean inPackage() {
    return false;
  }

  @Override
  public boolean inType() {
    return false;
  }

  @Override
  public boolean isInlineTag() {
    return false;
  }

  @Override
  public String toString(final Tag tag) {
    return null;
  }

  @Override
  public String toString(final Tag[] tags) {
    return null;
  }
}
