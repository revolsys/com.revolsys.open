package com.revolsys.swing.layout;

import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.LayoutStyle;

public class BaseLayoutStyle extends LayoutStyle {

  public static final BaseLayoutStyle INSTANCE = new BaseLayoutStyle();

  private int containerGap;

  @Override
  public int getContainerGap(final JComponent component, final int position,
    final Container parent) {
    return this.containerGap;
  }

  @Override
  public int getPreferredGap(final JComponent component1, final JComponent component2,
    final ComponentPlacement type, final int position, final Container parent) {
    return 1;
  }

}
