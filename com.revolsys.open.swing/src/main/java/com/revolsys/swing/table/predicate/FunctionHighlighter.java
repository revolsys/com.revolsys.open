package com.revolsys.swing.table.predicate;

import java.awt.Color;
import java.awt.Component;
import java.util.function.BiFunction;

import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jeometry.common.logging.Logs;

import com.revolsys.swing.table.highlighter.OutsideBorderHighlighter;

public class FunctionHighlighter implements HighlightPredicate {

  public static Highlighter color(final BiFunction<Component, ComponentAdapter, Boolean> function,
    final Color cellBackground, final Color cellForeground, final Color selectedBackground,
    final Color selectedForeground) {
    final HighlightPredicate predicate = new FunctionHighlighter(function);
    return new ColorHighlighter(predicate, cellBackground, cellForeground, selectedBackground,
      selectedForeground);
  }

  public static Highlighter outsideBorder(
    final BiFunction<Component, ComponentAdapter, Boolean> function, final Color color,
    final int thickness) {
    final HighlightPredicate predicate = new FunctionHighlighter(function);
    return new OutsideBorderHighlighter(predicate, color, thickness, true, false);
  }

  private final BiFunction<Component, ComponentAdapter, Boolean> function;

  public FunctionHighlighter(final BiFunction<Component, ComponentAdapter, Boolean> function) {
    this.function = function;
  }

  @Override
  public boolean isHighlighted(final Component renderer, final ComponentAdapter adapter) {
    try {
      return this.function.apply(renderer, adapter);
    } catch (final Throwable e) {
      Logs.debug(this, "Error in highlighter", e);
      return false;
    }
  }
}
