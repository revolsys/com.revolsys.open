package com.revolsys.doclet11.option;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import jdk.javadoc.doclet.Doclet.Option;

public class ConsumerOption extends AbstractOption implements Option {

  private final Consumer<String> action;

  public ConsumerOption(final List<String> names, final Kind kind, final String description,
    final String parameters, final Consumer<String> action) {
    super(names, kind, description, parameters);
    this.action = action;
  }

  public ConsumerOption(final List<String> names, final String description, final String parameters,
    final Consumer<String> action) {
    this(names, Kind.STANDARD, description, parameters, action);
  }

  public ConsumerOption(final String name, final String description, final String parameters,
    final Consumer<String> action) {
    this(Arrays.asList(name), description, parameters, action);
  }

  @Override
  public boolean process(final String option, final List<String> arguments) {
    this.action.accept(arguments.get(0));
    return true;
  }

}
