package com.revolsys.jump.ui.style;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;

import com.revolsys.jump.ui.swing.EditPanel;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;

public class BasicStyleEditPanel extends EditPanel<BasicStyle> {

  private static final long serialVersionUID = -6597629687665967177L;

  private BasicStylePanel stylePanel;

  public BasicStyleEditPanel(final WorkbenchContext context) {
    super(new BorderLayout());
    stylePanel = new BasicStylePanel(context.getBlackboard(),
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    add(stylePanel, BorderLayout.CENTER);

  }

  public BasicStyle getValue() {
    return stylePanel.getBasicStyle();
  }

  public void setValue(final BasicStyle value) {
    stylePanel.setBasicStyle(value);
  }
}
