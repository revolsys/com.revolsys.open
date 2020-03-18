package com.revolsys.swing.map.layer.record.style.panel;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.record.query.Condition;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.QueryWhereConditionField;
import com.revolsys.swing.field.TextArea;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.util.Property;

public class QueryFilterField extends ValueField implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private final JButton clearButton;

  private final AbstractRecordLayer layer;

  private final TextArea queryField;

  public QueryFilterField(final AbstractRecordLayer layer, final String fieldName,
    final String query) {
    super(new VerticalLayout());
    this.layer = layer;
    this.queryField = new TextArea(fieldName, query, 5, 30);
    Property.addListener(this.queryField, fieldName, this);
    final ToolBar toolBar = new ToolBar();
    toolBar.setOpaque(false);

    toolBar.addButtonTitleIcon("search", "Advanced Search", "filter_edits",
      this::showAdvancedFilter);

    this.clearButton = toolBar.addButtonTitleIcon("search", "Clear Filter", "filter:delete",
      () -> this.queryField.setFieldValue(""));
    this.clearButton.setEnabled(Property.hasValue(this.queryField.getText()));

    add(toolBar);
    add(new JScrollPane(this.queryField));

  }

  @Override
  public void addPropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    super.addPropertyChangeListener(propertyName, listener);
    Property.addListener(this.queryField, propertyName, listener);
  }

  @Override
  public String getFieldValidationMessage() {
    return this.queryField.getFieldValidationMessage();
  }

  @Override
  public <T> T getFieldValue() {
    return this.queryField.getFieldValue();
  }

  @Override
  public boolean isFieldValid() {
    return this.queryField.isFieldValid();
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (event.getPropertyName().equals("filter")) {
      final Condition filter = (Condition)event.getNewValue();
      if (filter == null) {
        this.queryField.setFieldValue(null);
      } else {
        this.queryField.setFieldValue(filter.toFormattedString());
      }
    }
    this.clearButton.setEnabled(Property.hasValue(this.queryField.getText()));
  }

  @Override
  public void setFieldBackgroundColor(final Color color) {
    this.queryField.setFieldBackgroundColor(color);
  }

  @Override
  public void setFieldForegroundColor(final Color color) {
    this.queryField.setFieldForegroundColor(color);
  }

  @Override
  public void setFieldInvalid(final String message, final Color foregroundColor,
    final Color backgroundColor) {
    this.queryField.setFieldInvalid(message, foregroundColor, backgroundColor);
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    this.queryField.setFieldToolTip(toolTip);
  }

  @Override
  public void setFieldValid() {
    this.queryField.setFieldValid();
  }

  @Override
  public boolean setFieldValue(final Object value) {
    return this.queryField.setFieldValue(value);
  }

  @Override
  public void setToolTipText(final String text) {
    this.queryField.setToolTipText(text);
  }

  public void showAdvancedFilter() {
    final QueryWhereConditionField advancedFilter = new QueryWhereConditionField(this.layer, this,
      this.queryField.getText());
    advancedFilter.showDialog(this);
  }

  @Override
  public void updateFieldValue() {
    this.queryField.updateFieldValue();
  }

}
