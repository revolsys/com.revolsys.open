package com.revolsys.jump.ui.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;

public class EditPanelDialog<T> extends JDialog {
  /**
   * 
   */
  private static final long serialVersionUID = 378939915683216271L;

  private OKCancelPanel okCancelPanel;

  private EditPanel<T> panel;

  public EditPanelDialog(final WorkbenchContext workbenchContext,
    final JDialog owner, final EditPanel<T> panel) {
    super(owner, I18N.get("ui.renderer.style.ColorThemingPanel.custom"), true);
    this.panel = panel;

    jbInit();
  }

  public EditPanelDialog(final WorkbenchContext workbenchContext,
    final Frame frame, final EditPanel<T> panel) {
    super(frame, panel.getTitle(), true);
    this.panel = panel;
    jbInit();
  }

  private void jbInit() {
    okCancelPanel = new OKCancelPanel();
    Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(panel, BorderLayout.CENTER);
    contentPane.add(okCancelPanel, BorderLayout.SOUTH);
    okCancelPanel.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        if (okCancelPanel.wasOKPressed()) {
          panel.save();
        }
        setVisible(false);
      }
    });
    pack();
  }

  public T getValue() {
    return panel.getValue();
  }

  public void setValue(final T value) {
    panel.setValue(value);
  }

  public boolean wasOKPressed() {
    return okCancelPanel.wasOKPressed();
  }

}
