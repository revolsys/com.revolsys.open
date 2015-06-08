package com.revolsys.swing.table.renderer;

import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import com.revolsys.swing.SwingUtil;

public class JRendererTextPane extends JTextPane {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  static {
    UIManager.put("TextPane.font", SwingUtil.FONT);
  }

  public JRendererTextPane() {
    setContentType("text/html");
    final HTMLEditorKit kit = new HTMLEditorKit();
    setEditorKit(kit);
    final StyleSheet styleSheet = kit.getStyleSheet();
    styleSheet.addRule("body {font-family:sans-serif;font-size:9px;}");
    final Document doc = kit.createDefaultDocument();
    setDocument(doc);
  }

  @Override
  public void firePropertyChange(final String propertyName, final boolean oldValue,
    final boolean newValue) {
  }

  @Override
  protected void firePropertyChange(final String propertyName, final Object oldValue,
    final Object newValue) {
    if ("text".equals(propertyName) || "document".equals(propertyName)) {
      super.firePropertyChange(propertyName, oldValue, newValue);
    }
  }

  @Override
  public void invalidate() {
  }

  @Override
  protected void paintComponent(final Graphics g) {
    // TODO Auto-generated method stub
    super.paintComponent(g);
  }

  @Override
  public void repaint() {
  }

  @Override
  public void repaint(final long tm, final int x, final int y, final int width, final int height) {
  }

  @Override
  public void repaint(final Rectangle r) {
  }

  @Override
  public void revalidate() {
  }

  @Override
  public void setText(final String t) {
    super.setText(t);
  }

  @Override
  public void validate() {
  }

}
