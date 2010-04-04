package com.revolsys.jump.ui.style;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.ScrollPaneConstants;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;

public class BasicStyleDialog extends JDialog {
  private static final long serialVersionUID = 5011193926152948608L;

  private BasicStylePanel basicStylePanel;

  private OKCancelPanel okCancelPanel;

  public BasicStyleDialog(final WorkbenchContext workbenchContext,
    final JDialog owner) {
    super(owner, I18N.get("ui.renderer.style.ColorThemingPanel.custom"), true);
    basicStylePanel = new BasicStylePanel(workbenchContext.getWorkbench()
      .getBlackboard(), ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

    okCancelPanel = new OKCancelPanel();
    Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(basicStylePanel, BorderLayout.CENTER);
    contentPane.add(okCancelPanel, BorderLayout.SOUTH);
    okCancelPanel.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        setVisible(false);
      }
    });
    pack();
  }

  public BasicStyle getBasicStyle() {
    return basicStylePanel.getBasicStyle();
  }

  public void setBasicStyle(final BasicStyle basicStyle) {
    basicStylePanel.setBasicStyle(basicStyle);
  }

  public boolean wasOKPressed() {
    return okCancelPanel.wasOKPressed();
  }

}
