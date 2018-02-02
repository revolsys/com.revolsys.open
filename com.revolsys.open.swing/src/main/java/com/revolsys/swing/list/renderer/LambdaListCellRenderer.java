package com.revolsys.swing.list.renderer;

import java.awt.Component;
import java.util.function.Function;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class LambdaListCellRenderer<T, R> implements ListCellRenderer<T> {

  @SuppressWarnings("unchecked")
  public static <T, R> LambdaListCellRenderer<T, R> newRenderer(final Function<T, R> function) {
    if (function == null) {
      return new LambdaListCellRenderer<>(v -> (R)v);
    } else {
      return new LambdaListCellRenderer<>(function);
    }
  }

  private final DefaultListCellRenderer renderer = new DefaultListCellRenderer();

  private final Function<T, R> function;

  private LambdaListCellRenderer(final Function<T, R> function) {
    this.function = function;
  }

  @Override
  public Component getListCellRendererComponent(final JList<? extends T> list, final T value,
    final int index, final boolean isSelected, final boolean cellHasFocus) {
    final Object rendered = this.function.apply(value);
    return this.renderer.getListCellRendererComponent(list, rendered, index, isSelected,
      cellHasFocus);
  }

}
