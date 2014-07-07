package com.revolsys.swing.map.layer.record.component;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.jdesktop.swingx.JXSearchField;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.awt.WebColors;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.query.BinaryCondition;
import com.revolsys.data.query.Column;
import com.revolsys.data.query.Condition;
import com.revolsys.data.query.ILike;
import com.revolsys.data.query.IsNotNull;
import com.revolsys.data.query.IsNull;
import com.revolsys.data.query.Not;
import com.revolsys.data.query.Q;
import com.revolsys.data.query.QueryValue;
import com.revolsys.data.query.RightUnaryCondition;
import com.revolsys.data.query.Value;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.RecordStoreQueryTextField;
import com.revolsys.swing.field.DateField;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.field.QueryWhereConditionField;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.table.RecordLayerTablePanel;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public class AttributeFilterPanel extends JComponent implements ActionListener,
ItemListener, DocumentListener, PropertyChangeListener {

  private static final long serialVersionUID = 1L;

  private String previousSearchFieldName;

  private final List<String> attributeNames;

  private JComponent searchField;

  private final JPanel searchFieldPanel = new JPanel();

  private final JPanel operatorFieldPanel = new JPanel();

  private final ComboBox nameField;

  private final TextField searchTextField;

  private final RecordDefinition recordDefinition;

  private final AbstractRecordLayer layer;

  private final ComboBox numericOperatorField = new ComboBox("=", "<>", "Like",
    "IS NULL", "IS NOT NULL", "<", "<=", ">", ">=");

  private final ComboBox dateOperatorField = new ComboBox("=", "<>", "IS NULL",
    "IS NOT NULL", "<", "<=", ">", ">=");

  private final ComboBox generalOperatorField = new ComboBox("=", "<>", "Like",
    "IS NULL", "IS NOT NULL");

  private final ComboBox codeTableOperatorField = new ComboBox("=", "<>",
    "IS NULL", "IS NOT NULL");

  private ComboBox operatorField;

  private final RecordLayerTableModel tableModel;

  private final boolean eventsEnabled = true;

  private final JLabel whereLabel;

  private CodeTable codeTable;

  private boolean settingFilter = false;

  private Attribute attribute;

  public AttributeFilterPanel(final RecordLayerTablePanel tablePanel) {
    this.tableModel = tablePanel.getTableModel();
    this.layer = tablePanel.getLayer();
    this.recordDefinition = this.layer.getRecordDefinition();

    this.whereLabel = new JLabel();
    this.whereLabel.setMaximumSize(new Dimension(100, 250));
    this.whereLabel.setFont(SwingUtil.FONT);
    this.whereLabel.setOpaque(true);
    this.whereLabel.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createLoweredBevelBorder(),
      BorderFactory.createEmptyBorder(1, 2, 1, 2)));
    this.whereLabel.setBackground(WebColors.White);
    add(this.whereLabel);

    this.attributeNames = new ArrayList<String>(tablePanel.getColumnNames());
    this.attributeNames.remove(this.recordDefinition.getGeometryAttributeName());
    final AttributeTitleStringConveter converter = new AttributeTitleStringConveter(
      this.layer);
    this.nameField = new ComboBox(converter, false,
      this.attributeNames.toArray());
    this.nameField.setRenderer(converter);
    this.nameField.addActionListener(this);
    add(this.nameField);

    add(this.operatorFieldPanel);

    this.searchTextField = new TextField(20);
    this.searchField = this.searchTextField;
    this.searchTextField.addActionListener(this);
    this.searchTextField.setPreferredSize(new Dimension(200, 22));
    add(this.searchFieldPanel);
    GroupLayoutUtil.makeColumns(this, 4, false);
    clear();
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    if (this.eventsEnabled) {
      try {
        final Object source = event.getSource();
        if (source == this.searchField) {
          updateCondition();
        } else if (source == this.nameField) {
          setSearchFieldName(getSearchFieldName());
        }
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error("Unable to search", e);
      }
    }
  }

  private void addListeners(final JComponent component) {
    if (component instanceof RecordStoreQueryTextField) {
      final RecordStoreQueryTextField queryField = (RecordStoreQueryTextField)component;
      queryField.addItemListener(this);
    } else if (component instanceof JXSearchField) {
      final JXSearchField searchTextField = (JXSearchField)component;
      searchTextField.addActionListener(this);
    } else if (component instanceof JTextComponent) {
      final JTextComponent searchTextField = (JTextComponent)component;
      searchTextField.getDocument().addDocumentListener(this);
    } else if (component instanceof DateField) {
      final DateField dateField = (DateField)component;
      dateField.addActionListener(this);
    }
    if (component instanceof Field) {
      final Field field = (Field)component;
      final String fieldName = field.getFieldName();
      Property.addListener(field, fieldName, this);
    }
  }

  @Override
  public void changedUpdate(final DocumentEvent event) {
    updateCondition();
  }

  public void clear() {
    try {
      this.settingFilter = true;
      String searchField = this.previousSearchFieldName;
      if (!StringUtils.hasText(searchField)) {
        searchField = this.layer.getProperty("searchField");
        if (!StringUtils.hasText(searchField)) {
          searchField = this.recordDefinition.getAttributeNames().get(0);
        }
      }
      setSearchFieldName(searchField);
      setFilter(null);
    } finally {
      this.settingFilter = false;
    }
  }

  public void fireSearchChanged(final String propertyName,
    final Object oldValue, final Object newValue) {
    if (!EqualsRegistry.equal(oldValue, newValue)) {
      if (SwingUtilities.isEventDispatchThread()) {
        final Method method = JavaBeanUtil.getMethod(getClass(),
          "fireSearchChanged", String.class, Object.class, Object.class);
        Invoke.background("Change search", this, method, propertyName,
          oldValue, newValue);
      } else {
        firePropertyChange(propertyName, oldValue, newValue);
      }
    }
  }

  public List<String> getAttributeNames() {
    return this.attributeNames;
  }

  public Condition getFilter() {
    return this.tableModel.getFilter();
  }

  public AbstractRecordLayer getLayer() {
    return this.layer;
  }

  public String getSearchFieldName() {
    return (String)this.nameField.getSelectedItem();
  }

  public final String getSearchOperator() {
    if (this.operatorField == null) {
      return "=";
    } else {
      return (String)this.operatorField.getSelectedItem();
    }
  }

  public Object getSearchValue() {
    if (this.searchField instanceof JTextComponent) {
      final JTextComponent textComponent = (JTextComponent)this.searchField;
      return textComponent.getText();
    } else {
      final Object value = SwingUtil.getValue(this.searchField);
      return value;
    }
  }

  @Override
  public void insertUpdate(final DocumentEvent event) {
    updateCondition();
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    if (!this.settingFilter) {
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
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (event.getPropertyName().equals("filter")) {
      final Condition filter = (Condition)event.getNewValue();
      setFilter(filter);
    } else if (event.getSource() == this.searchField) {
      updateCondition();
    }
  }

  @SuppressWarnings("rawtypes")
  private void removeListeners(final JComponent component) {
    if (component instanceof RecordStoreQueryTextField) {
      final RecordStoreQueryTextField queryField = (RecordStoreQueryTextField)component;
      queryField.removeItemListener(this);
    } else if (component instanceof JXSearchField) {
      final JXSearchField searchTextField = (JXSearchField)component;
      searchTextField.removeActionListener(this);
    } else if (component instanceof JComboBox) {
      final JComboBox comboField = (JComboBox)component;
      comboField.removeActionListener(this);
    } else if (component instanceof DateField) {
      final DateField dateField = (DateField)component;
      dateField.removeActionListener(this);
    }
    if (component instanceof Field) {
      final Field field = (Field)component;
      final String fieldName = field.getFieldName();
      Property.removeListener(field, fieldName, this);
    }
  }

  @Override
  public void removeUpdate(final DocumentEvent event) {
    updateCondition();
  }

  public void setFilter(final Condition filter) {
    try {
      this.settingFilter = true;
      setSearchFilter(filter);
      boolean simple = false;
      if (filter == null) {
        setSearchOperator("=");
        final Field searchField = (Field)this.searchField;
        if (searchField != null) {
          searchField.setFieldValue(null);
        }
        simple = true;
      } else if (filter instanceof Not) {
        final Not not = (Not)filter;
        final QueryValue condition = not.getQueryValue();
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
            String searchText = StringConverterRegistry.toString(searchValue);
            if (StringUtils.hasText(searchText)) {
              setSearchField(this.searchTextField);
              searchText = searchText.replaceAll("%", "");
              final String previousSearchText = this.searchTextField.getText();

              if (!EqualsRegistry.equal(searchText, previousSearchText)) {
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

            final String searchText = StringConverterRegistry.toString(searchValue);

            final Field searchField = (Field)this.searchField;
            final Object oldValue = searchField.getFieldValue();
            if (!searchText.equalsIgnoreCase(StringConverterRegistry.toString(oldValue))) {
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
        this.nameField.setVisible(true);
        this.operatorField.setVisible(true);
        this.searchFieldPanel.setVisible(true);
      } else {
        String filterText = filter.toString();
        if (filterText.length() > 40) {
          filterText = filterText.substring(0, 40) + "...";
        }
        this.whereLabel.setText(filterText);
        this.whereLabel.setToolTipText(filterText);
        this.whereLabel.setVisible(true);
        this.nameField.setVisible(false);
        this.operatorField.setVisible(false);
        this.searchFieldPanel.setVisible(false);
      }
    } finally {
      this.settingFilter = false;
    }
  }

  private void setOperatorField(final ComboBox field) {
    if (field != this.operatorField) {
      final String operator = getSearchOperator();
      if (this.operatorField != null) {
        this.operatorField.removeItemListener(this);
      }
      this.operatorFieldPanel.removeAll();
      this.operatorField = field;
      if (field != null) {
        this.operatorField.setSelectedIndex(0);
        if (operator != null) {
          this.operatorField.setSelectedItem(operator);
        }
        this.operatorField.addItemListener(this);
        this.operatorFieldPanel.add(field);
        GroupLayoutUtil.makeColumns(this.operatorFieldPanel, 1, false);
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
      if (this.searchField instanceof RecordStoreQueryTextField) {
        final RecordStoreQueryTextField recordStoreSearchTextField = (RecordStoreQueryTextField)this.searchField;
        recordStoreSearchTextField.setMaxResults(100);
        recordStoreSearchTextField.setPreferredSize(new Dimension(200, 22));
        recordStoreSearchTextField.setBelow(true);
      } else if (this.searchField instanceof JXSearchField) {
        final JXSearchField searchTextField = (JXSearchField)this.searchField;
        searchTextField.setPreferredSize(new Dimension(200, 22));
      }
      this.searchFieldPanel.add(this.searchField);
      GroupLayoutUtil.makeColumns(this.searchFieldPanel, 1, false);
    }
  }

  private void setSearchFieldName(final String searchFieldName) {
    if (!EqualsRegistry.equal(searchFieldName, this.previousSearchFieldName)) {
      this.previousSearchFieldName = searchFieldName;
      this.layer.setProperty("searchField", searchFieldName);
      this.codeTable = this.recordDefinition.getCodeTableByColumn(searchFieldName);
      final RecordDefinition recordDefinition = this.tableModel.getRecordDefinition();
      this.attribute = recordDefinition.getAttribute(searchFieldName);
      final Class<?> attributeClass = this.attribute.getTypeClass();
      if (!EqualsRegistry.equal(searchFieldName,
        this.nameField.getSelectedItem())) {
        this.nameField.setFieldValue(searchFieldName);
      }

      ComboBox operatorField;
      if (this.codeTable != null
          && !searchFieldName.equals(recordDefinition.getIdAttributeName())) {
        operatorField = this.codeTableOperatorField;
      } else if (Number.class.isAssignableFrom(attributeClass)) {
        operatorField = this.numericOperatorField;
      } else if (Date.class.isAssignableFrom(attributeClass)) {
        operatorField = this.dateOperatorField;
      } else {
        operatorField = this.generalOperatorField;
      }
      setOperatorField(operatorField);

      setSearchOperator("=");
      if (!this.settingFilter) {
        setSearchFilter(null);
      }
    }
  }

  private boolean setSearchFilter(final Condition filter) {
    return this.tableModel.setFilter(filter);
  }

  private boolean setSearchOperator(final String searchOperator) {
    final Object currentSearchOperator = this.operatorField.getSelectedItem();
    if (!EqualsRegistry.equal(searchOperator, currentSearchOperator)) {
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
        if (!this.settingFilter) {
          setFilter(Q.isNull(this.attribute));
        }
        this.codeTable = null;
      } else if ("IS NOT NULL".equals(searchOperator)) {
        if (!this.settingFilter) {
          setFilter(Q.isNotNull(this.attribute));
        }
        this.codeTable = null;
      } else {
        searchField = QueryWhereConditionField.createSearchField(
          this.attribute, this.codeTable);
      }

      setSearchField(searchField);
      return true;
    }
  }

  public void showAdvancedFilter() {
    final Condition filter = getFilter();
    final QueryWhereConditionField advancedFilter = new QueryWhereConditionField(
      this.layer, this, filter);
    advancedFilter.showDialog(this);
  }

  public void updateCondition() {
    if (this.eventsEnabled) {
      final Object searchValue = getSearchValue();
      Condition condition = null;
      final String searchOperator = getSearchOperator();
      if ("IS NULL".equalsIgnoreCase(searchOperator)) {
        condition = Q.isNull(this.attribute);
      } else if ("IS NOT NULL".equalsIgnoreCase(searchOperator)) {
        condition = Q.isNotNull(this.attribute);
      } else if (this.attribute != null) {
        if (StringUtils.hasText(StringConverterRegistry.toString(searchValue))) {
          if ("Like".equalsIgnoreCase(searchOperator)) {
            final String searchText = StringConverterRegistry.toString(searchValue);
            if (StringUtils.hasText(searchText)) {
              condition = Q.iLike(this.attribute, "%" + searchText + "%");
            }
          } else {
            Object value = null;
            if (this.codeTable == null) {
              try {
                final Class<?> attributeClass = this.attribute.getTypeClass();
                value = StringConverterRegistry.toObject(attributeClass,
                  searchValue);
              } catch (final Throwable t) {
                return;
              }
            } else {
              value = this.codeTable.getId(searchValue);
              if (value == null) {
                return;
              }
            }
            if (value != null) {
              condition = Q.binary(this.attribute, searchOperator, value);
            }
          }
        }
      }
      setSearchFilter(condition);
    }
  }
}
