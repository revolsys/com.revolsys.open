/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */
package com.revolsys.jump.ui.style;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import com.vividsolutions.jump.workbench.ui.ColorPanel;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;

public class BasicStyleComponent extends JPanel {
  private static final long serialVersionUID = -142311037392513807L;

  private ColorPanel colorPanel = new ColorPanel();

  public BasicStyleComponent(final Dimension dimension) {
    super(new GridBagLayout());
    add(colorPanel, new GridBagConstraints(0, 0, 1, 1, 1, 0,
      GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2),
      0, 0));
    setColorPanelSize(dimension);
  }

  public BasicStyleComponent(final BasicStyle basicStyle) {
    this(basicStyle, new Dimension(45, 8));
  }

  public BasicStyleComponent(final BasicStyle basicStyle,
    final Dimension dimension) {
    this(dimension);
    setBasicStyle(basicStyle);
  }

  protected void setColorPanelSize(final Dimension d) {
    colorPanel.setMinimumSize(d);
    colorPanel.setMaximumSize(d);
    colorPanel.setPreferredSize(d);
  }

  public void setBasicStyle(final BasicStyle basicStyle) {
    colorPanel.setStyle(basicStyle);
    if (basicStyle.isRenderingLine()) {
      colorPanel.setLineColor(GUIUtil.alphaColor(
        ((BasicStyle)basicStyle).getLineColor(), basicStyle.getAlpha()));
    } else {
      colorPanel.setLineColor(GUIUtil.alphaColor(Color.WHITE, 0));
    }
    if (basicStyle.isRenderingFill()) {
      colorPanel.setFillColor(GUIUtil.alphaColor(basicStyle.getFillColor(),
        basicStyle.getAlpha()));
    } else {
      colorPanel.setFillColor(GUIUtil.alphaColor(Color.WHITE, 0));
    }
    colorPanel.setOpaque(false);
    setOpaque(false);
  }

}
