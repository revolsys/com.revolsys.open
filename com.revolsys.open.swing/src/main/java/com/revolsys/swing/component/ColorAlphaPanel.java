package com.revolsys.swing.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.color.ColorUtil;

import com.revolsys.swing.layout.GroupLayoutUtil;

public class ColorAlphaPanel extends AbstractColorChooserPanel implements
  ChangeListener {

  /**
   * 
   */
  private static final long serialVersionUID = 3533569797414988165L;

  private final JSlider alphaSlider;

  public ColorAlphaPanel() {
    setLayout(new BorderLayout());
    alphaSlider = new JSlider(0, 255, 255);
    alphaSlider.setMajorTickSpacing(64);
    alphaSlider.setMinorTickSpacing(16);
    alphaSlider.setPaintTicks(true);
    alphaSlider.setToolTipText("Alpha");
    alphaSlider.addChangeListener(this);
    alphaSlider.setPaintLabels(true);
    Hashtable<Integer, JComponent> labels = new Hashtable<Integer, JComponent>();
    labels.put(0, new JLabel("0"));
    labels.put(64, new JLabel("64"));
    labels.put(128, new JLabel("128"));
    labels.put(192, new JLabel("192"));
    labels.put(255, new JLabel("255"));
    alphaSlider.setLabelTable(labels);

    add(new JLabel("Alpha (Opacity)"));

    add(alphaSlider);
    GroupLayoutUtil.makeColumns(this, 2);
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    Color color = getColorFromModel();
    Color newColor = ColorUtil.setAlpha(color, alphaSlider.getValue());
    ColorSelectionModel colorSelectionModel = getColorSelectionModel();
    colorSelectionModel.setSelectedColor(newColor);
  }

  @Override
  public void updateChooser() {
    Color color = getColorFromModel();
    alphaSlider.setValue(color.getAlpha());
  }

  @Override
  protected void buildChooser() {
  }

  @Override
  public String getDisplayName() {
    return "Alpha";
  }

  @Override
  public Icon getSmallDisplayIcon() {
    return new ImageIcon();
  }

  @Override
  public Icon getLargeDisplayIcon() {
    return new ImageIcon();
  }
}
