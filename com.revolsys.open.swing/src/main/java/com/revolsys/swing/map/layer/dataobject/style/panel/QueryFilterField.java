package com.revolsys.swing.map.layer.dataobject.style.panel;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.VerticalLayout;
import org.springframework.util.StringUtils;

import com.revolsys.gis.data.query.Condition;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.QueryWhereConditionField;
import com.revolsys.swing.field.TextArea;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.toolbar.ToolBar;

public class QueryFilterField extends ValueField implements
  PropertyChangeListener {

  private final TextArea queryField;

  private final JButton clearButton;

  private final AbstractDataObjectLayer layer;

  public QueryFilterField(final AbstractDataObjectLayer layer,
    final String fieldName, final String query) {
    super(new VerticalLayout());
    this.layer = layer;
    queryField = new TextArea(fieldName, query, 5, 30);
    queryField.addPropertyChangeListener(fieldName, this);
    final ToolBar toolBar = new ToolBar();

    toolBar.addButtonTitleIcon("search", "Advanced Search", "filter_edits",
      this, "showAdvancedFilter");

    clearButton = toolBar.addButtonTitleIcon("search", "Clear Filter",
      "filter_delete", queryField, "setFieldValue", "");
    clearButton.setEnabled(StringUtils.hasText(queryField.getText()));

    add(toolBar);
    add(new JScrollPane(queryField));

  }

  @Override
  public void addPropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    super.addPropertyChangeListener(propertyName, listener);
    queryField.addPropertyChangeListener(propertyName, listener);
  }

  @Override
  public String getFieldValidationMessage() {
    return queryField.getFieldValidationMessage();
  }

  @Override
  public <T> T getFieldValue() {
    return queryField.getFieldValue();
  }

  @Override
  public boolean isFieldValid() {
    return queryField.isFieldValid();
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (event.getPropertyName().equals("filter")) {
      final Condition filter = (Condition)event.getNewValue();
      if (filter == null) {
        queryField.setFieldValue(null);
      } else {
        queryField.setFieldValue(filter.toFormattedString());
      }
    }
    clearButton.setEnabled(StringUtils.hasText(queryField.getText()));
  }

  @Override
  public void setFieldBackgroundColor(final Color color) {
    queryField.setFieldBackgroundColor(color);
  }

  @Override
  public void setFieldForegroundColor(final Color color) {
    queryField.setFieldForegroundColor(color);
  }

  @Override
  public void setFieldInvalid(final String message,
    final Color foregroundColor, final Color backgroundColor) {
    queryField.setFieldInvalid(message, foregroundColor, backgroundColor);
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    queryField.setFieldToolTip(toolTip);
  }

  @Override
  public void setFieldValid() {
    queryField.setFieldValid();
  }

  @Override
  public void setFieldValue(final Object value) {
    queryField.setFieldValue(value);
  }

  @Override
  public void setToolTipText(final String text) {
    queryField.setToolTipText(text);
  }

  public void showAdvancedFilter() {
    final QueryWhereConditionField advancedFilter = new QueryWhereConditionField(
      layer, this, queryField.getText());
    advancedFilter.showDialog(this);
  }

  @Override
  public void updateFieldValue() {
    queryField.updateFieldValue();
  }

}
