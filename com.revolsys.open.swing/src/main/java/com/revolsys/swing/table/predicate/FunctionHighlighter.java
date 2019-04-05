package com.revolsys.swing.table.predicate;

import java.awt.Color;
import java.awt.Component;
import java.util.function.BiFunction;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import org.jdesktop.swingx.decorator.BorderHighlighter;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jeometry.common.logging.Logs;

import com.revolsys.swing.table.highlighter.OutsideBorderHighlighter;

public class FunctionHighlighter implements HighlightPredicate {

  public static Highlighter border(final BiFunction<Component, ComponentAdapter, Boolean> function,
    final Border border) {
    final HighlightPredicate predicate = new FunctionHighlighter(function);
    return new BorderHighlighter(predicate, border);
  }

  public static Highlighter border(final BiFunction<Component, ComponentAdapter, Boolean> function,
    final Color color) {
    return border(function, color, 1);
  }

  public static Highlighter border(final BiFunction<Component, ComponentAdapter, Boolean> function,
    final Color color, final int thickness) {
    final Border border = BorderFactory.createLineBorder(color, thickness);
    return border(function, border);
  }

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
