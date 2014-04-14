package com.revolsys.jtstest.testbuilder;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jtstest.testbuilder.controller.JTSTestBuilderController;

public class GeometryPopupMenu extends JPopupMenu {
  Coordinates clickCoord;

  public GeometryPopupMenu() {
    initUI();
  }

  private void initUI() {
    final JMenuItem extractComponentItem = new JMenuItem("Extract Component");
    extractComponentItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        JTSTestBuilderController.extractComponentsToTestCase(clickCoord);
      }
    });
    add(extractComponentItem);

    final JMenuItem copyComponentItem = new JMenuItem("Copy Component");
    copyComponentItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        JTSTestBuilderController.copyComponentToClipboard(clickCoord);
      }
    });
    add(copyComponentItem);

    final JMenuItem infoItem = new JMenuItem("Info");
    infoItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        JTSTestBuilderFrame.instance().displayInfo(clickCoord);
      }
    });
    add(infoItem);

  }

  /**
   * Record model Coordinates of click point for use in menu operations
   */
  @Override
  public void show(final Component invoker, final int x, final int y) {
    final GeometryEditPanel editPanel = (GeometryEditPanel)invoker;
    clickCoord = editPanel.getViewport().toModelCoordinate(
      new java.awt.Point(x, y));
    super.show(invoker, x, y);
  }

}
