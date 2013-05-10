package com.revolsys.swing.map.layer.dataobject.style.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.revolsys.i18n.I18n;
import com.revolsys.swing.component.ValuePanel;
import com.revolsys.swing.layout.SpringLayoutUtil;
import com.revolsys.swing.listener.InvokeMethodActionListener;
import com.revolsys.swing.listener.InvokeMethodChangeListener;

public class ColorChooserPanel extends ValuePanel<Color> {
  /**
   * 
   */
  private static final long serialVersionUID = 2046767970167738062L;

  private Color color;

  private final BufferedImage colorImage;

  public ColorChooserPanel(final Color color) {
    super(new SpringLayout());
    colorImage = new BufferedImage(60, 30, BufferedImage.TYPE_INT_RGB);
    final JButton colorButton = new JButton(new ImageIcon(colorImage));
    colorButton.setBackground(Color.WHITE);
    colorButton.setMargin(new Insets(0, 0, 0, 0));
    add(colorButton);
    colorButton.addActionListener(new InvokeMethodActionListener(this,
      "changeColor"));
    setColor(color);

    SpringLayoutUtil.makeColumns(this, 2, 0, 0, 5, 0);
  }

  public void changeColor() {
    final JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
      "Select Color", ModalityType.APPLICATION_MODAL);

    final Container contentPane = dialog.getContentPane();

    final JPanel chooserPanel = new JPanel(new BorderLayout());
    contentPane.add(chooserPanel, BorderLayout.CENTER);

    final JColorChooser colorChooser = new JColorChooser(this.color);
    chooserPanel.add(colorChooser, BorderLayout.CENTER);

    final JSlider alphaSlider = new JSlider(0, 255, color.getAlpha());
    alphaSlider.setMajorTickSpacing(32);
    alphaSlider.setMinorTickSpacing(8);
    alphaSlider.setPaintTicks(true);
    alphaSlider.setToolTipText(I18n.getString(getClass(), "transparency"));
    final InvokeMethodChangeListener colorChangeListener = new InvokeMethodChangeListener(
      this, "updateColor", colorChooser, alphaSlider);
    alphaSlider.addChangeListener(colorChangeListener);
    chooserPanel.add(alphaSlider, BorderLayout.SOUTH);

    final JPanel buttonPane = new JPanel(new FlowLayout());
    contentPane.add(buttonPane, BorderLayout.SOUTH);

    final JButton okButton = new JButton(
      UIManager.getString("ColorChooser.okText"));
    dialog.getRootPane().setDefaultButton(okButton);
    okButton.setActionCommand("OK");

    final InvokeMethodActionListener hideAction = new InvokeMethodActionListener(
      dialog, "hide");
    okButton.addActionListener(hideAction);
    okButton.addActionListener(new InvokeMethodActionListener(this,
      "updateColor", colorChooser, alphaSlider));
    colorChooser.getSelectionModel().addChangeListener(colorChangeListener);
    buttonPane.add(okButton);

    final JButton cancelButton = new JButton(
      UIManager.getString("ColorChooser.cancelText"));

    // The following few lines are used to register esc to close the dialog
    // Action cancelKeyAction = new AbstractAction() {
    // public void actionPerformed(ActionEvent e) {
    // ((AbstractButton)e.getSource()).fireActionPerformed(e);
    // }
    // };
    final KeyStroke cancelKeyStroke = KeyStroke.getKeyStroke(
      (char)KeyEvent.VK_ESCAPE, false);
    final InputMap inputMap = cancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    final ActionMap actionMap = cancelButton.getActionMap();
    if (inputMap != null && actionMap != null) {
      inputMap.put(cancelKeyStroke, "cancel");
      // actionMap.put("cancel", cancelKeyAction);
    }
    // end esc handling

    cancelButton.setActionCommand("cancel");
    cancelButton.addActionListener(hideAction);
    buttonPane.add(cancelButton);

    final JButton resetButton = new JButton(
      UIManager.getString("ColorChooser.resetText"));
    resetButton.addActionListener(new InvokeMethodActionListener(colorChooser,
      "setColor", color));
    // int mnemonic = SwingUtilities2.getUIDefaultsInt(
    // "ColorChooser.resetMnemonic", -1);
    // if (mnemonic != -1) {
    // resetButton.setMnemonic(mnemonic);
    // }
    buttonPane.add(resetButton);
    contentPane.add(buttonPane, BorderLayout.SOUTH);
    dialog.pack();
    dialog.setVisible(true);
  }

  public Color getColor() {
    return color;
  }

  public Number getOpacity() {
    return getColor().getAlpha() / 255F;
  }

  @Override
  public void save() {
    setValue(getColor());
  }

  public void setColor(final Color color) {
    final Object oldValue = this.color;
    this.color = color;
    final Graphics graphics = colorImage.getGraphics();
    graphics.setColor(Color.WHITE);
    graphics.fillRect(0, 0, 60, 30);
    graphics.setColor(color);
    graphics.fillRect(5, 5, 50, 20);
    firePropertyChange("color", oldValue, color);
    repaint();
  }

  public void updateColor(final JColorChooser colorChooser,
    final JSlider transparencyPanel) {
    final Color color = colorChooser.getColor();
    final Color newColor = new Color(color.getRed(), color.getGreen(),
      color.getBlue(), transparencyPanel.getValue());
    colorChooser.setColor(newColor);
    setColor(newColor);
  }

}
