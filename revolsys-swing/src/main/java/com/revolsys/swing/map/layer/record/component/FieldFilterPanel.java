package com.revolsys.swing.map.layer.record.component;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.jdesktop.swingx.JXSearchField;
import org.jeometry.common.awt.WebColors;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.logging.Logs;

import com.revolsys.io.BaseCloseable;
import com.revolsys.record.Record;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.query.BinaryCondition;
import com.revolsys.record.query.Column;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.ILike;
import com.revolsys.record.query.IsNotNull;
import com.revolsys.record.query.IsNull;
import com.revolsys.record.query.Not;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.RightUnaryCondition;
import com.revolsys.record.query.Value;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.AbstractRecordQueryField;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.DateField;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.field.QueryWhereConditionField;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.RecordDefinitionSqlFilter;
import com.revolsys.swing.map.layer.record.renderer.AbstractRecordLayerRenderer;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.util.Property;
import com.revolsys.value.GlobalBooleanValue;

public class FieldFilterPanel extends JComponent implements PropertyChangeListener {

  private static final long serialVersionUID = 1L;

  private CodeTable codeTable;

  private final ComboBox<String> codeTableOperatorField = ComboBox.newComboBox("operator", "=",
    "<>", "IS NULL", "IS NOT NULL");

  private final ComboBox<String> dateOperatorField = ComboBox.newComboBox("operator", "=", "<>",
    "IS NULL", "IS NOT NULL", "<", "<=", ">", ">=");

  private FieldDefinition field;

  private final List<String> fieldNames;

  private final ComboBox<String> generalOperatorField = ComboBox.newComboBox("operator", "=", "<>",
    "Like", "IS NULL", "IS NOT NULL");

  private Object lastValue = null;

  private final AbstractRecordLayer layer;

  private ComboBox<String> nameField;

  private final ComboBox<String> numericOperatorField = ComboBox.newComboBox("operator", "=", "<>",
    "IS NULL", "IS NOT NULL", "<", "<=", ">", ">=");

  private ComboBox<String> operatorField;

  private final JPanel operatorFieldPanel = new JPanel();

  private String previousSearchFieldName;

  private final RecordDefinition recordDefinition;

  private JComponent searchField;

  private final JPanel searchFieldPanel = new JPanel();

  private final TextField searchTextField = new TextField(20);

  private final GlobalBooleanValue settingFilter = new GlobalBooleanValue(false);

  private RecordLayerTableModel tableModel;

  private final JLabel whereLabel = new JLabel();

  private final ItemListener itemListener = (e) -> {
    if (this.settingFilter.isFalse()) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        final Object source = e.getSource();
        if (source == this.nameField) {
          final String searchFieldName = getSearchFieldName();
          setSearchFieldName(searchFieldName);
        } else if (source == this.operatorField) {
          final String searchOperator = getSearchOperator();
          setSearchOperator(searchOperator);
        } else {
          updateCondition();
        }
      }
    }
  };

  private final DocumentListener documentListener = new DocumentListener() {

    @Override
    public void changedUpdate(final DocumentEvent event) {
      updateCondition();
    }

    @Override
    public void insertUpdate(final DocumentEvent event) {
      updateCondition();
    }

    @Override
    public void removeUpdate(final DocumentEvent event) {
      updateCondition();
    }
  };

  private final ActionListener actionListener = event -> {
    if (this.settingFilter.isFalse()) {
      try {
        final Object source = event.getSource();
        if (source == this.searchField) {
          updateCondition();
        } else if (source == this.nameField) {
          setSearchFieldName(getSearchFieldName());
        }
      } catch (final Throwable e) {
        Logs.error(this, "Unable to search", e);
      }
    }
  };

  public FieldFilterPanel(final TablePanel tablePanel, final RecordLayerTableModel tableModel) {
    this.tableModel = tableModel;
    this.layer = tableModel.getLayer();
    this.recordDefinition = this.layer.getRecordDefinition();
    this.fieldNames = new ArrayList<>(this.layer.getFieldNamesSet("All"));
    this.fieldNames.removeAll(this.recordDefinition.getGeometryFieldNames());
    if (this.fieldNames.isEmpty()) {
      setVisible(false);
    } else {
      this.whereLabel.setMaximumSize(new Dimension(100, 250));
      this.whereLabel.setFont(SwingUtil.FONT);
      this.whereLabel.setOpaque(true);
      this.whereLabel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(1, 2, 1, 2)));
      this.whereLabel.setBackground(WebColors.White);
      add(this.whereLabel);

      this.nameField = ComboBox.newComboBox("fieldNames", this.fieldNames,
        (final Object fieldName) -> {
          return this.layer.getFieldTitle((String)fieldName);
        });
      this.nameField.addActionListener(this.actionListener);
      add(this.nameField);

      add(this.operatorFieldPanel);

      this.searchField = this.searchTextField;
      this.searchTextField.addActionListener(this.actionListener);
      this.searchTextField.setPreferredSize(new Dimension(200, 22));
      add(this.searchFieldPanel);
      GroupLayouts.makeColumns(this, 4, false);
      clear();
    }
  }

  public FieldFilterPanel(final TablePanel tablePanel, final RecordLayerTableModel tableModel,
    final Map<String, Object> config) {
    this(tablePanel, tableModel);

    if (!this.fieldNames.isEmpty()) {
      String searchField = this.layer.getProperty("searchField");
      searchField = (String)config.getOrDefault("searchField", searchField);
      setSearchFieldName(searchField);
      final Predicate<Record> filter = AbstractRecordLayerRenderer.getFilter(this.layer, config);
      if (filter instanceof RecordDefinitionSqlFilter) {
        final RecordDefinitionSqlFilter sqlFilter = (RecordDefinitionSqlFilter)filter;
        final Condition condition = sqlFilter.getCondition();
        setFilter(condition);
      }
    }
  }

  private void addListeners(final JComponent component) {
    if (component instanceof AbstractRecordQueryField) {
      final AbstractRecordQueryField queryField = (AbstractRecordQueryField)component;
      queryField.addPropertyChangeListener("selectedRecord", this);
    } else if (component instanceof JXSearchField) {
      final JXSearchField searchTextField = (JXSearchField)component;
      searchTextField.addActionListener(this.actionListener);
    } else if (component instanceof JTextComponent) {
      final JTextComponent searchTextField = (JTextComponent)component;
      searchTextField.getDocument().addDocumentListener(this.documentListener);
    } else if (component instanceof DateField) {
      final DateField dateField = (DateField)component;
      dateField.addActionListener(this.actionListener);
    }
    if (component instanceof Field) {
      final Field field = (Field)component;
      final String fieldName = field.getFieldName();
      Property.addListener(field, fieldName, this);
    }
  }

  public void clear() {
    if (Invoke.swingThread(this::clear)) {
      try (
        BaseCloseable settingFilter = this.settingFilter.closeable(true)) {
        this.lastValue = null;
        String searchField = this.previousSearchFieldName;
        if (!Property.hasValue(searchField)) {
          final List<String> fieldNames = this.recordDefinition.getFieldNames();
          if (!fieldNames.isEmpty()) {
            searchField = fieldNames.get(0);
          }
        }
        setSearchFieldName(searchField);
        setFilter(Condition.ALL);
      }
    }
  }

  public void close() {
    this.tableModel = null;
  }

  public void fireSearchChanged(final String propertyName, final Object oldValue,
    final Object newValue) {
    if (!DataType.equal(oldValue, newValue)) {
      Invoke.background("Change search", () -> fireSearchChanged(propertyName, oldValue, newValue));
    }
  }

  public List<String> getFieldNames() {
    return this.fieldNames;
  }

  public Condition getFilter() {
    return this.tableModel.getFilter();
  }

  public AbstractLayer getLayer() {
    return this.layer;
  }

  public String getSearchFieldName() {
    if (this.nameField != null) {
      final String searchFieldName = this.nameField.getSelectedItem();
      if (Property.hasValue(searchFieldName)) {
        return searchFieldName;
      }
    }
    return this.previousSearchFieldName;
  }

  public final String getSearchOperator() {
    if (this.operatorField == null) {
      return "=";
    } else {
      return this.operatorField.getSelectedItem();
    }
  }

  public Object getSearchValue() {
    if (this.searchField instanceof JTextComponent) {
      final JTextComponent textComponent = (JTextComponent)this.searchField;
      if (textComponent.isEditable()) {
        return textComponent.getText();
      } else {
        return null;
      }
    } else {
      final Object value = SwingUtil.getValue(this.searchField);
      return value;
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final String propertyName = event.getPropertyName();
    if (propertyName.equals("filter")) {
      final Condition filter = (Condition)event.getNewValue();
      setFilter(filter);
    } else if (event.getSource() == this.searchField) {
      updateCondition();
    }
  }

  @SuppressWarnings("rawtypes")
  private void removeListeners(final JComponent component) {
    if (component instanceof AbstractRecordQueryField) {
      final AbstractRecordQueryField queryField = (AbstractRecordQueryField)component;
      queryField.removePropertyChangeListener("selectedRecord", this);
    } else if (component instanceof JXSearchField) {
      final JXSearchField searchTextField = (JXSearchField)component;
      searchTextField.removeActionListener(this.actionListener);
    } else if (component instanceof JComboBox) {
      final JComboBox comboField = (JComboBox)component;
      comboField.removeActionListener(this.actionListener);
    } else if (component instanceof JTextComponent) {
      final JTextComponent searchTextField = (JTextComponent)component;
      searchTextField.getDocument().removeDocumentListener(this.documentListener);
    } else if (component instanceof DateField) {
      final DateField dateField = (DateField)component;
      dateField.removeActionListener(this.actionListener);
    }
    if (component instanceof Field) {
      final Field field = (Field)component;
      final String fieldName = field.getFieldName();
      Property.removeListener(field, fieldName, this);
    }
  }

  public void setFilter(final Condition filter) {
    if (!this.fieldNames.isEmpty()) {
      if (Invoke.swingThread(this::setFilter, filter)) {
        try (
          BaseCloseable settingFilter = this.settingFilter.closeable(true)) {
          setSearchFilter(filter);
          boolean simple = false;
          if (Property.isEmpty(filter)) {
            final Field searchField = (Field)this.searchField;
            if (searchField != null) {
              searchField.setFieldValue(null);
            }
            setSearchOperator("=");
            simple = true;
          } else if (filter instanceof Not) {
            final Not not = (Not)filter;
            final QueryValue condition = not.getValue();
            if (condition instanceof IsNull) {
              final IsNull isNull = (IsNull)condition;
              final IsNotNull isNotNull = new IsNotNull(isNull.getValue());
              setFilter(isNotNull);
              return;
            }
          } else if (filter instanceof ILike) {
            final ILike equal = (ILike)filter;
            final QueryValue leftCondition = equal.getLeft();
            final QueryValue rightCondition = equal.getRight();

            if (leftCondition instanceof Column && rightCondition instanceof Value) {
              final Column column = (Column)leftCondition;
              final String searchFieldName = column.getName();
              setSearchFieldName(searchFieldName);

              if (setSearchOperator("Like")) {
                final Value value = (Value)rightCondition;
                final Object searchValue = value.getValue();
                String searchText = DataTypes.toString(searchValue);
                if (Property.hasValue(searchText)) {
                  setSearchField(this.searchTextField);
                  searchText = searchText.replaceAll("%", "");
                  final String previousSearchText = this.searchTextField.getText();

                  if (!DataType.equal(searchText, previousSearchText)) {
                    this.searchTextField.setFieldValue(searchText);
                  }
                  simple = true;
                } else {
                  setSearchFilter(null);
                }
              }
            }
          } else if (filter instanceof BinaryCondition) {
            final BinaryCondition condition = (BinaryCondition)filter;
            final QueryValue leftCondition = condition.getLeft();
            final QueryValue rightCondition = condition.getRight();

            if (leftCondition instanceof Column && rightCondition instanceof Value) {
              final Column column = (Column)leftCondition;
              final String searchFieldName = column.getName();
              setSearchFieldName(searchFieldName);

              final String searchOperator = condition.getOperator();
              if (setSearchOperator(searchOperator)) {
                final Value value = (Value)rightCondition;
                final Object searchValue = value.getValue();

                final String searchText = DataTypes.toString(searchValue);

                final Field searchField = (Field)this.searchField;
                final Object oldValue = searchField.getFieldValue();
                if (!searchText.equalsIgnoreCase(DataTypes.toString(oldValue))) {
                  searchField.setFieldValue(searchText);
                }
                simple = true;
              }
            }
          } else if (filter instanceof RightUnaryCondition) {
            final RightUnaryCondition condition = (RightUnaryCondition)filter;
            final String operator = condition.getOperator();
            if (filter instanceof IsNull || filter instanceof IsNotNull) {
              final QueryValue leftValue = condition.getValue();
              if (leftValue instanceof Column) {
                final Column column = (Column)leftValue;
                final String searchFieldName = column.getName();
                setSearchFieldName(searchFieldName);

                if (setSearchOperator(operator)) {
                  simple = true;
                }
              }
            }
          }
          if (simple) {
            this.whereLabel.setVisible(false);
            if (this.nameField != null) {
              this.nameField.setVisible(true);
            }
            if (this.operatorField != null) {
              this.operatorField.setVisible(true);
            }
            this.searchFieldPanel.setVisible(true);
          } else {
            String filterText = filter.toString();
            if (filterText.length() > 40) {
              filterText = filterText.substring(0, 40) + "...";
            }
            this.whereLabel.setText(filterText);
            this.whereLabel.setToolTipText(filterText);
            this.whereLabel.setVisible(true);
            if (this.nameField != null) {
              this.nameField.setVisible(false);
            }
            this.operatorField.setVisible(false);
            this.searchFieldPanel.setVisible(false);
          }
        }
      }
    }
  }

  private void setOperatorField(final ComboBox<String> field) {
    if (field != this.operatorField) {
      final String operator = getSearchOperator();
      if (this.operatorField != null) {
        this.operatorField.removeItemListener(this.itemListener);
      }
      this.operatorFieldPanel.removeAll();
      this.operatorField = field;
      if (field != null) {
        this.operatorField.setSelectedIndex(0);
        if (operator != null) {
          this.operatorField.setSelectedItem(operator);
        }
        this.operatorField.addItemListener(this.itemListener);
        this.operatorFieldPanel.add(field);
        GroupLayouts.makeColumns(this.operatorFieldPanel, 1, false);
      }
    }
  }

  private void setSearchField(final JComponent searchField) {
    this.searchFieldPanel.removeAll();
    removeListeners(this.searchField);
    this.searchField = searchField;
    if (searchField == null) {
      this.searchFieldPanel.setVisible(false);
    } else {
      this.searchFieldPanel.setVisible(true);
      addListeners(searchField);
      if (searchField instanceof Field) {
        final Field field = (Field)searchField;
        field.setFieldValue(this.lastValue);
      }
      final Dimension size = new Dimension(200, 22);
      searchField.setMinimumSize(size);
      searchField.setPreferredSize(size);
      searchField.setMaximumSize(size);
      this.searchFieldPanel.add(this.searchField);
      GroupLayouts.makeColumns(this.searchFieldPanel, 1, false);
    }
  }

  private void setSearchFieldName(final String searchFieldName) {
    if (Property.hasValue(searchFieldName)
      && !DataType.equal(searchFieldName, this.previousSearchFieldName)
      && this.fieldNames.contains(searchFieldName)) {
      this.lastValue = null;
      this.previousSearchFieldName = searchFieldName;
      final RecordDefinition recordDefinition = this.tableModel.getRecordDefinition();
      this.field = recordDefinition.getField(searchFieldName);
      final Class<?> fieldClass = this.field.getTypeClass();
      if (!DataType.equal(searchFieldName, this.nameField.getSelectedItem())) {
        this.nameField.setFieldValue(searchFieldName);
      }
      if (searchFieldName.equals(recordDefinition.getIdFieldName())) {
        this.codeTable = null;
      } else {
        this.codeTable = this.recordDefinition.getCodeTableByFieldName(searchFieldName);
      }
      ComboBox<String> operatorField;
      if (this.codeTable != null) {
        operatorField = this.codeTableOperatorField;
      } else if (Number.class.isAssignableFrom(fieldClass)) {
        operatorField = this.numericOperatorField;
      } else if (Date.class.isAssignableFrom(fieldClass)) {
        operatorField = this.dateOperatorField;
      } else {
        operatorField = this.generalOperatorField;
      }
      setOperatorField(operatorField);

      setSearchOperator("=");
      if (this.settingFilter.isFalse()) {
        setSearchFilter(null);
      }
    }
  }

  public void setSearchFilter(final Condition filter) {
    if (this.tableModel != null) {
      this.tableModel.setFilter(filter);
    }
  }

  private boolean setSearchOperator(final String searchOperator) {
    if (this.operatorField == null) {
      return false;
    } else {
      final Object currentSearchOperator = this.operatorField.getSelectedItem();
      if (!DataType.equal(searchOperator, currentSearchOperator)) {
        this.operatorField.setSelectedItem(searchOperator);
      }
      if (this.operatorField.getSelectedIndex() < 0) {
        return false;
      } else {
        JComponent searchField = null;
        if ("Like".equals(searchOperator)) {
          this.codeTable = null;
          searchField = this.searchTextField;
        } else if ("IS NULL".equals(searchOperator)) {
          if (this.settingFilter.isFalse()) {
            setFilter(Q.isNull(this.field));
          }
          this.codeTable = null;
        } else if ("IS NOT NULL".equals(searchOperator)) {
          if (this.settingFilter.isFalse()) {
            setFilter(Q.isNotNull(this.field));
          }
          this.codeTable = null;
        } else {
          searchField = this.layer.newSearchField(this.field, this.codeTable);
        }

        setSearchField(searchField);
        return true;
      }
    }
  }

  public void showAdvancedFilter() {
    final Condition filter = getFilter();
    final QueryWhereConditionField advancedFilter = new QueryWhereConditionField(this.layer, this,
      filter);
    advancedFilter.showDialog(this);
  }

  private void updateCondition() {
    if (this.settingFilter.isFalse()) {
      if (!(this.searchField instanceof Field) || ((Field)this.searchField).isFieldValid()) {
        final Object searchValue = getSearchValue();
        this.lastValue = searchValue;
        Condition condition = null;
        final String searchOperator = getSearchOperator();
        if ("IS NULL".equalsIgnoreCase(searchOperator)) {
          condition = Q.isNull(this.field);
        } else if ("IS NOT NULL".equalsIgnoreCase(searchOperator)) {
          condition = Q.isNotNull(this.field);
        } else if (this.field != null) {
          if (Property.hasValue(DataTypes.toString(searchValue))) {
            if ("Like".equalsIgnoreCase(searchOperator)) {
              final String searchText = DataTypes.toString(searchValue);
              if (Property.hasValue(searchText)) {
                condition = Q.iLike(this.field, "%" + searchText + "%");
              }
            } else {
              Object value = null;
              if (this.codeTable == null) {
                value = this.layer.getValidSearchValue(this.field, searchValue);
              } else {
                value = this.codeTable.getIdentifier(searchValue);
                if (value == null) {
                  return;
                }
              }
              if (value != null) {
                condition = Q.binary(this.field, searchOperator, value);
              }
            }
          }
        }
        setSearchFilter(condition);
      }
    }
  }
}
