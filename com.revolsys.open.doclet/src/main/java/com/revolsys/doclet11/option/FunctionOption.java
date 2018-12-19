package com.revolsys.doclet11.option;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import jdk.javadoc.doclet.Doclet.Option;

public class FunctionOption extends AbstractOption implements Option {

  private final Function<String, Boolean> action;

  public FunctionOption(final List<String> names, final Kind kind, final String description,
    final String parameters, final Function<String, Boolean> action) {
    super(names, kind, description, parameters);
    this.action = action;
  }

  public FunctionOption(final List<String> names, final String description, final String parameters,
    final Function<String, Boolean> action) {
    this(names, Kind.STANDARD, description, parameters, action);
  }

  public FunctionOption(final String name, final String description, final String parameters,
    final Function<String, Boolean> action) {
    this(Arrays.asList(name), description, parameters, action);
  }

  @Override
  public boolean process(final String option, final List<String> arguments) {
    return this.action.apply(arguments.get(0));
  }
}
