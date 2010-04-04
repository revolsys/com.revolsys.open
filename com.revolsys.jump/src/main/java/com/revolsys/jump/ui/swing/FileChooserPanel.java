package com.revolsys.jump.ui.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.coordsys.CoordinateSystem;
import com.vividsolutions.jump.coordsys.CoordinateSystemRegistry;
import com.vividsolutions.jump.util.Blackboard;

public class FileChooserPanel extends JPanel {
  /**
   * 
   */
  private static final long serialVersionUID = -8802431348575901979L;

  private JFileChooser chooser;

  private JComboBox coordinateSystemComboBox = new JComboBox();

  private JLabel coordinateSystemLabel = new JLabel(
    I18N.get("datasource.FileDataSourceQueryChooser.coordinate-system-of-file")
      + " ");

  private JPanel southComponent1Container = new JPanel(new BorderLayout());

  private JPanel southComponent2Container = new JPanel(new BorderLayout());

  @SuppressWarnings("unchecked")
  public FileChooserPanel(final JFileChooser chooser,
    final Blackboard blackboard) {
    setLayout(new BorderLayout());
    coordinateSystemLabel.setDisplayedMnemonic('r');
    coordinateSystemLabel.setLabelFor(coordinateSystemComboBox);
    List<CoordinateSystem> sortedSystems = new ArrayList<CoordinateSystem>(
      CoordinateSystemRegistry.instance(blackboard).getCoordinateSystems());
    Collections.sort(sortedSystems);
    coordinateSystemComboBox.setModel(new DefaultComboBoxModel(
      new Vector<CoordinateSystem>(sortedSystems)));
    this.chooser = chooser;

    JPanel southPanel = new JPanel(new GridBagLayout());
    southPanel.add(coordinateSystemLabel, new GridBagConstraints(0, 0, 1, 1, 0,
      0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0,
        0), 0, 0));
    southPanel.add(coordinateSystemComboBox, new GridBagConstraints(1, 0, 1, 1,
      0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0,
        0, 0), 0, 0));
    southPanel.add(southComponent1Container, new GridBagConstraints(2, 0, 1, 1,
      1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(
        0, 4, 0, 0), 0, 0));
    southPanel.add(southComponent2Container, new GridBagConstraints(0, 1, 3, 1,
      1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(
        0, 0, 0, 0), 0, 0));
    add(chooser, BorderLayout.CENTER);
    add(southPanel, BorderLayout.SOUTH);
    coordinateSystemComboBox.setVisible(false);
    coordinateSystemLabel.setVisible(false);
  }

  public JFileChooser getChooser() {
    return chooser;
  }

  public void setCoordinateSystemComboBoxVisible(final boolean visible) {
    coordinateSystemComboBox.setVisible(visible);
    coordinateSystemLabel.setVisible(visible);
  }

  public CoordinateSystem getSelectedCoordinateSystem() {
    if (coordinateSystemComboBox.isVisible()) {
      return (CoordinateSystem)coordinateSystemComboBox.getSelectedItem();
    } else {
      return CoordinateSystem.UNSPECIFIED;
    }
  }

  public void setSelectedCoordinateSystem(final String name) {
    coordinateSystemComboBox.setSelectedItem(coordinateSystem(name));
  }

  private CoordinateSystem coordinateSystem(final String name) {
    for (int i = 0; i < coordinateSystemComboBox.getItemCount(); i++) {
      if (((CoordinateSystem)coordinateSystemComboBox.getItemAt(i)).getName()
        .equals(name)) {
        return (CoordinateSystem)coordinateSystemComboBox.getItemAt(i);
      }
    }
    return null;
  }
}
