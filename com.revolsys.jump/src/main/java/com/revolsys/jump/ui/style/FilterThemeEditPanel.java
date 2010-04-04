package com.revolsys.jump.ui.style;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import com.revolsys.jump.feature.filter.NameValueFeatureFilter;
import com.revolsys.jump.feature.filter.NameValueFilterPanel;
import com.revolsys.jump.ui.swing.EditPanel;
import com.revolsys.jump.ui.swing.SpringUtilities;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;

public class FilterThemeEditPanel extends EditPanel<FilterTheme> {
  /**
   * 
   */
  private static final long serialVersionUID = 3452694865363996756L;

  private WorkbenchContext workbenchContext;

  private JTextField labelField;

  private BasicStylePanel stylePanel;

  private JCheckBox visibleField;

  private NameValueFilterPanel filterPanel;

  public FilterThemeEditPanel(final WorkbenchContext workbenchContext) {
    super(new BorderLayout());
    this.workbenchContext = workbenchContext;
    jbInit();
  }

  private void jbInit() {
    JPanel labelPanel = new JPanel(new SpringLayout());
    add(labelPanel, BorderLayout.NORTH);

    labelPanel.add(new JLabel("Label:"));
    labelField = new JTextField(20);
    labelPanel.add(labelField);

    labelPanel.add(new JLabel("Visible:"));
    visibleField = new JCheckBox();
    labelPanel.add(visibleField);

    SpringUtilities.makeCompactGrid(labelPanel, 2, 2, 3, 3, 3, 3);

    JTabbedPane tabPane = new JTabbedPane();
    add(tabPane, BorderLayout.CENTER);

    stylePanel = new BasicStylePanel(workbenchContext.getBlackboard(),
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    filterPanel = new NameValueFilterPanel(workbenchContext);
    tabPane.addTab("Filter", filterPanel);

    tabPane.addTab("Style", stylePanel);

  }

  public FilterTheme getFilterTheme() {
    return (FilterTheme)getValue();
  }

  public FilterTheme getValue() {
    FilterTheme theme = super.getValue();
    theme.setLabel(labelField.getText());
    theme.setFilter(filterPanel.getValue());
    theme.setBasicStyle(stylePanel.getBasicStyle());
    theme.setVisible(visibleField.isSelected());
    return theme;
  }

  public void setValue(final FilterTheme theme) {
    labelField.setText(theme.getLabel());
    filterPanel.setValue((NameValueFeatureFilter)theme.getFilter());
    stylePanel.setBasicStyle((BasicStyle)theme.getBasicStyle());
    visibleField.setSelected(theme.isVisible());
    super.setValue(theme);
  }

}
