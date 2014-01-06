package com.revolsys.swing.map.layer.dataobject.component;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
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
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.query.BinaryCondition;
import com.revolsys.gis.data.query.Column;
import com.revolsys.gis.data.query.Condition;
import com.revolsys.gis.data.query.Conditions;
import com.revolsys.gis.data.query.ILike;
import com.revolsys.gis.data.query.IsNotNull;
import com.revolsys.gis.data.query.IsNull;
import com.revolsys.gis.data.query.QueryValue;
import com.revolsys.gis.data.query.RightUnaryCondition;
import com.revolsys.gis.data.query.Value;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.spring.SpelUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.DataStoreSearchTextField;
import com.revolsys.swing.field.DateField;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.field.QueryWhereConditionField;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.table.DataObjectLayerTablePanel;
import com.revolsys.swing.map.layer.dataobject.table.model.DataObjectLayerTableModel;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.JavaBeanUtil;

public class AttributeFilterPanel extends JComponent implements ActionListener,
  ItemListener, DocumentListener {

  private static final long serialVersionUID = 1L;

  private String previousSearchFieldName;

  private final List<String> attributeNames;

  private JComponent searchField;

  private final JPanel searchFieldPanel = new JPanel();

  private final JPanel operatorFieldPanel = new JPanel();

  private final ComboBox nameField;

  private final TextField searchTextField;

  private final DataObjectMetaData metaData;

  private final AbstractDataObjectLayer layer;

  private final ComboBox numericOperatorField = new ComboBox("=", "<>", "Like",
    "IS NULL", "IS NOT NULL", "<", "<=", ">", ">=");

  private final ComboBox dateOperatorField = new ComboBox("=", "<>", "IS NULL",
    "IS NOT NULL", "<", "<=", ">", ">=");

  private final ComboBox generalOperatorField = new ComboBox("=", "<>", "Like",
    "IS NULL", "IS NOT NULL");

  private final ComboBox codeTableOperatorField = new ComboBox("=", "<>",
    "IS NULL", "IS NOT NULL");

  private ComboBox operatorField;

  private final DataObjectLayerTableModel tableModel;

  private String previousSearchOperator = "";

  private boolean eventsEnabled = true;

  private final JLabel whereLabel;

  public AttributeFilterPanel(final DataObjectLayerTablePanel tablePanel) {
    this.tableModel = tablePanel.getTableModel();
    this.layer = tablePanel.getLayer();
    this.metaData = layer.getMetaData();

    this.whereLabel = new JLabel();
    whereLabel.setMaximumSize(new Dimension(100, 250));
    whereLabel.setFont(SwingUtil.FONT);
    whereLabel.setOpaque(true);
    whereLabel.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createLoweredBevelBorder(),
      BorderFactory.createEmptyBorder(1, 2, 1, 2)));
    whereLabel.setBackground(WebColors.White);
    add(this.whereLabel);

    this.attributeNames = new ArrayList<String>(tablePanel.getColumnNames());
    this.attributeNames.remove(this.metaData.getGeometryAttributeName());
    final AttributeTitleStringConveter converter = new AttributeTitleStringConveter(
      layer);
    this.nameField = new ComboBox(converter, false,
      this.attributeNames.toArray());
    this.nameField.setRenderer(converter);
    this.nameField.addActionListener(this);
    add(this.nameField);

    add(operatorFieldPanel);

    this.searchTextField = new TextField(15);
    this.searchField = this.searchTextField;
    this.searchTextField.addActionListener(this);
    this.searchTextField.setPreferredSize(new Dimension(200, 22));
    add(this.searchFieldPanel);

    GroupLayoutUtil.makeColumns(this, 4, false);
    clear();
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    if (eventsEnabled) {
      try {
        final Object source = event.getSource();
        if (source == this.searchField) {
          updateCondition();
        } else if (source == this.nameField) {
          updateCondition();
        }
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error("Unable to search", e);
      }
    }
  }

  private void addListeners(final JComponent component) {
    if (component instanceof DataStoreSearchTextField) {
      final DataStoreSearchTextField dataStoreSearchTextField = (DataStoreSearchTextField)component;
      dataStoreSearchTextField.addItemListener(this);
    } else if (component instanceof JXSearchField) {
      final JXSearchField searchTextField = (JXSearchField)component;
      searchTextField.addActionListener(this);
    } else if (component instanceof JTextComponent) {
      final JTextComponent searchTextField = (JTextComponent)component;
      searchTextField.getDocument().addDocumentListener(this);
    } else if (component instanceof JComboBox) {
      final JComboBox comboField = (JComboBox)component;
      comboField.addActionListener(this);
    } else if (component instanceof DateField) {
      final DateField dateField = (DateField)component;
      dateField.addActionListener(this);
    }
  }

  @Override
  public void changedUpdate(final DocumentEvent event) {
    updateCondition();
  }

  public void clear() {
    final String searchField = layer.getProperty("searchField");
    setSearchField(searchField);

    setFilter(null);
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

  public AbstractDataObjectLayer getLayer() {
    return this.layer;
  }

  public String getSearchAttribute() {
    return (String)this.nameField.getSelectedItem();
  }

  private JComponent getSearchField(final String searchFieldName,
    final String searchOperator, final Object searchValue) {
    JComponent searchField;
    final Attribute attribute = this.metaData.getAttribute(searchFieldName);

    if ("Like".equals(searchOperator)) {
      searchField = this.searchTextField;
    } else if ("IS NULL".equals(searchOperator)) {
      return null;
    } else if ("IS NOT NULL".equals(searchOperator)) {
      return null;
    } else {
      final String searchFieldFactory = attribute.getProperty("searchFieldFactory");
      if (StringUtils.hasText(searchFieldFactory)) {
        final Map<String, Object> searchFieldFactoryParameters = attribute.getProperty("searchFieldFactoryParameters");
        searchField = SpelUtil.getValue(searchFieldFactory, attribute,
          searchFieldFactoryParameters);
      } else {
        final CodeTable codeTable = this.metaData.getCodeTableByColumn(searchFieldName);
        if (codeTable == null
          || searchFieldName.equals(metaData.getIdAttributeName())) {
          if (Date.class.isAssignableFrom(attribute.getTypeClass())) {
            searchField = SwingUtil.createDateField(searchFieldName);
          } else {
            searchField = this.searchTextField;
          }
        } else {
          searchField = SwingUtil.createComboBox(codeTable, false);
        }
      }
    }
    ((Field)searchField).setFieldValue(searchValue);
    return searchField;
  }

  public final String getSearchOperator() {
    if (operatorField == null) {
      return "=";
    } else {
      return (String)this.operatorField.getSelectedItem();
    }
  }

  public Object getSearchValue() {
    final Object value = SwingUtil.getValue(this.searchField);
    return value;
  }

  @Override
  public void insertUpdate(final DocumentEvent event) {
    updateCondition();
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    updateCondition();
  }

  private void removeListeners(final JComponent component) {
    if (component instanceof DataStoreSearchTextField) {
      final DataStoreSearchTextField dataStoreSearchTextField = (DataStoreSearchTextField)component;
      dataStoreSearchTextField.removeItemListener(this);
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
  }

  @Override
  public void removeUpdate(final DocumentEvent event) {
    updateCondition();
  }

  public void setFilter(final Condition filter) {
    final boolean eventsEnabled = this.eventsEnabled;
    try {
      this.eventsEnabled = false;
      this.tableModel.setFilter(filter);
      boolean simple = filter == null;
      final Field field = (Field)searchField;
      if (filter == null) {
        field.setFieldValue(null);
      } else if (filter instanceof ILike) {
        final ILike equal = (ILike)filter;
        final QueryValue leftCondition = equal.getLeft();
        final QueryValue rightCondition = equal.getRight();

        if (leftCondition instanceof Column && rightCondition instanceof Value) {
          final Column column = (Column)leftCondition;
          final Value value = (Value)rightCondition;
          nameField.setFieldValue(column.getName());

          final String searchValue = StringConverterRegistry.toString(
            value.getValue()).replaceAll("%", "");
          final String fieldValue = StringConverterRegistry.toString(field.getFieldValue());

          if (searchValue == null || fieldValue == null
            || !searchValue.equalsIgnoreCase(fieldValue)) {
            field.setFieldValue(searchValue);
          }
          if (((DefaultComboBoxModel)operatorField.getModel()).getIndexOf("Like") != -1) {
            simple = true;
            operatorField.setFieldValue("Like");
          }
        }
      } else if (filter instanceof BinaryCondition) {
        final BinaryCondition condition = (BinaryCondition)filter;
        final String operator = condition.getOperator();
        final QueryValue leftCondition = condition.getLeft();
        final QueryValue rightCondition = condition.getRight();

        if (leftCondition instanceof Column && rightCondition instanceof Value) {
          final Column column = (Column)leftCondition;
          final Value value = (Value)rightCondition;
          nameField.setFieldValue(column.getName());
          operatorField.setFieldValue(operator);
          final String text = StringConverterRegistry.toString(value.getValue());
          final Object oldValue = field.getFieldValue();
          if (!text.equalsIgnoreCase(StringConverterRegistry.toString(oldValue))) {
            field.setFieldValue(text);
          }
          simple = ((DefaultComboBoxModel)operatorField.getModel()).getIndexOf(operator) != -1;
        }
      } else if (filter instanceof RightUnaryCondition) {
        final RightUnaryCondition condition = (RightUnaryCondition)filter;
        final String operator = condition.getOperator();
        if (filter instanceof IsNull || filter instanceof IsNotNull) {
          final QueryValue leftValue = condition.getValue();
          if (leftValue instanceof Column) {
            final Column column = (Column)leftValue;
            nameField.setFieldValue(column.getName());
            operatorField.setFieldValue(operator);
            simple = ((DefaultComboBoxModel)operatorField.getModel()).getIndexOf(operator) != -1;
          }
        }

      }
      if (simple) {
        whereLabel.setVisible(false);
        nameField.setVisible(true);
        operatorField.setVisible(true);
        searchFieldPanel.setVisible(true);
      } else {
        final String filterText = filter.toString();
        whereLabel.setText(filterText);
        whereLabel.setToolTipText(filterText);
        whereLabel.setVisible(true);
        nameField.setVisible(false);
        operatorField.setVisible(false);
        searchFieldPanel.setVisible(false);
      }
    } finally {
      this.eventsEnabled = eventsEnabled;
    }
  }

  private void setOperatorField(final ComboBox field) {
    if (field != operatorField) {
      final String operator = getSearchOperator();
      if (this.operatorField != null) {
        this.operatorField.removeItemListener(this);
      }
      operatorFieldPanel.removeAll();
      this.operatorField = field;
      if (field != null) {
        this.operatorField.setSelectedIndex(0);
        if (operator != null) {
          this.operatorField.setSelectedItem(operator);
        }
        this.operatorField.addItemListener(this);
        operatorFieldPanel.add(field);
        GroupLayoutUtil.makeColumns(this.operatorFieldPanel, 1, false);
      }
    }
  }

  private void setSearchField(final JComponent searchField) {
    this.searchFieldPanel.removeAll();
    removeListeners(this.searchField);
    this.searchField = searchField;
    if (searchField != null) {
      addListeners(searchField);
      if (this.searchField instanceof DataStoreSearchTextField) {
        final DataStoreSearchTextField dataStoreSearchTextField = (DataStoreSearchTextField)this.searchField;
        dataStoreSearchTextField.setMaxResults(5);
        dataStoreSearchTextField.setPreferredSize(new Dimension(200, 22));
      } else if (this.searchField instanceof JXSearchField) {
        final JXSearchField searchTextField = (JXSearchField)this.searchField;
        searchTextField.setPreferredSize(new Dimension(200, 22));
      }
      this.searchFieldPanel.add(this.searchField);
      GroupLayoutUtil.makeColumns(this.searchFieldPanel, 1, false);
    }
  }

  public void setSearchField(final String searchField) {
    if (StringUtils.hasText(searchField)) {
      this.nameField.setSelectedItem(searchField);
    } else {
      this.nameField.setSelectedIndex(0);
    }
  }

  public void showAdvancedFilter() {
    final QueryWhereConditionField advancedFilter = new QueryWhereConditionField(
      this);
    advancedFilter.showDialog(this);
  }

  public void updateCondition() {
    if (eventsEnabled) {
      final String fieldName = getSearchAttribute();
      String searchOperator = getSearchOperator();
      final Object searchValue = getSearchValue();
      final DataObjectMetaData metaData = this.tableModel.getMetaData();
      final Attribute attribute = metaData.getAttribute(fieldName);
      final Class<?> attributeClass = attribute.getTypeClass();
      final boolean fieldChanged = !EqualsRegistry.equal(fieldName,
        previousSearchFieldName);
      if (fieldChanged
        || !EqualsRegistry.equal(searchOperator, previousSearchOperator)) {
        if (fieldChanged) {
          final CodeTable codeTable = this.metaData.getCodeTableByColumn(fieldName);

          ComboBox operatorField;
          if (codeTable != null
            && !fieldName.equals(metaData.getIdAttributeName())) {
            operatorField = codeTableOperatorField;
          } else if (Number.class.isAssignableFrom(attributeClass)) {
            operatorField = numericOperatorField;
          } else if (Date.class.isAssignableFrom(attributeClass)) {
            operatorField = dateOperatorField;
          } else {
            operatorField = generalOperatorField;
          }
          setOperatorField(operatorField);
          searchOperator = getSearchOperator();
        }
        final JComponent searchField = getSearchField(fieldName,
          searchOperator, searchValue);
        setSearchField(searchField);
      }
      Condition condition = null;
      if ("IS NULL".equalsIgnoreCase(searchOperator)) {
        condition = IsNull.column(fieldName);
      } else if ("IS NOT NULL".equalsIgnoreCase(searchOperator)) {
        condition = IsNotNull.column(fieldName);
      } else if (StringUtils.hasText(fieldName)) {
        layer.setProperty("searchField", fieldName);
        if (StringUtils.hasText(StringConverterRegistry.toString(searchValue))) {
          if ("Like".equalsIgnoreCase(searchOperator)) {
            final String searchText = StringConverterRegistry.toString(searchValue);
            if (StringUtils.hasText(searchText)) {
              condition = ILike.iLike(fieldName, "%" + searchText + "%");
            }
          } else {
            Object value = null;
            try {
              value = StringConverterRegistry.toObject(attributeClass,
                searchValue);
            } catch (final Throwable t) {
            }
            if (value != null) {
              condition = Conditions.binary(attribute, searchOperator, value);
            }
          }
        }
      }
      setFilter(condition);
      this.previousSearchFieldName = fieldName;
      this.previousSearchOperator = searchOperator;
    }
  }
}
