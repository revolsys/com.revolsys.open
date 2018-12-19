package com.revolsys.doclet11.option;

import java.util.List;

import jdk.javadoc.doclet.Doclet.Option;

public abstract class AbstractOption implements Option {

  protected final List<String> names;

  protected final Kind kind;

  protected final String description;

  protected final String parameters;

  public AbstractOption(final List<String> names, final Kind kind, final String description,
    final String parameters) {
    this.names = names;
    this.kind = kind;
    this.description = description;
    this.parameters = parameters;
  }

  @Override
  public int getArgumentCount() {
    return 1;
  }

  @Override
  public String getDescription() {
    return this.description;
  }

  @Override
  public Kind getKind() {
    return this.kind;
  }

  @Override
  public List<String> getNames() {
    return this.names;
  }

  @Override
  public String getParameters() {
    return this.parameters;
  }

}
