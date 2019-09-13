package com.revolsys.swing.table.highlighter;

import java.awt.Component;

import org.jdesktop.swingx.decorator.ComponentAdapter;

@FunctionalInterface
public interface TableModelHighlighter {
  Component highlight(Component component, ComponentAdapter adapter, int rowIndex, int columnIndex);
}
