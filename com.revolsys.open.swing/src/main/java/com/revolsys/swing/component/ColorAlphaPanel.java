package com.revolsys.swing.component;

import java.awt.BorderLayout;
import java.awt.Color;
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

import org.jeometry.common.awt.WebColors;

import com.revolsys.swing.layout.GroupLayouts;

public class ColorAlphaPanel extends AbstractColorChooserPanel implements ChangeListener {
  private static final long serialVersionUID = 3533569797414988165L;

  private final JSlider alphaSlider;

  public ColorAlphaPanel() {
    setLayout(new BorderLayout());
    this.alphaSlider = new JSlider(0, 255, 255);
    this.alphaSlider.setMajorTickSpacing(64);
    this.alphaSlider.setMinorTickSpacing(16);
    this.alphaSlider.setPaintTicks(true);
    this.alphaSlider.setToolTipText("Alpha");
    this.alphaSlider.addChangeListener(this);
    this.alphaSlider.setPaintLabels(true);
    final Hashtable<Integer, JComponent> labels = new Hashtable<>();
    labels.put(0, new JLabel("0"));
    labels.put(64, new JLabel("64"));
    labels.put(128, new JLabel("128"));
    labels.put(192, new JLabel("192"));
    labels.put(255, new JLabel("255"));
    this.alphaSlider.setLabelTable(labels);

    add(new JLabel("Alpha (Opacity)"));

    add(this.alphaSlider);
    GroupLayouts.makeColumns(this, 2, true);
  }

  @Override
  protected void buildChooser() {
  }

  @Override
  public String getDisplayName() {
    return "Alpha";
  }

  @Override
  public Icon getLargeDisplayIcon() {
    return new ImageIcon();
  }

  @Override
  public Icon getSmallDisplayIcon() {
    return new ImageIcon();
  }

  @Override
  public void stateChanged(final ChangeEvent e) {
    final Color color = getColorFromModel();
    final Color newColor = WebColors.newAlpha(color, this.alphaSlider.getValue());
    final ColorSelectionModel colorSelectionModel = getColorSelectionModel();
    colorSelectionModel.setSelectedColor(newColor);
  }

  @Override
  public void updateChooser() {
    final Color color = getColorFromModel();
    final int alpha = color.getAlpha();
    this.alphaSlider.setValue(alpha);
  }
}
