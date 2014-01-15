package com.revolsys.swing.map.layer.dataobject.component;

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
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.query.BinaryCondition;
import com.revolsys.gis.data.query.Column;
import com.revolsys.gis.data.query.Condition;
import com.revolsys.gis.data.query.F;
import com.revolsys.gis.data.query.ILike;
import com.revolsys.gis.data.query.IsNotNull;
import com.revolsys.gis.data.query.IsNull;
import com.revolsys.gis.data.query.Not;
import com.revolsys.gis.data.query.QueryValue;
import com.revolsys.gis.data.query.RightUnaryCondition;
import com.revolsys.gis.data.query.Value;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.DataStoreQueryTextField;
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
  ItemListener, DocumentListener, PropertyChangeListener {

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

  private final boolean eventsEnabled = true;

  private final JLabel whereLabel;

  private CodeTable codeTable;

  private boolean settingFilter = false;

  private Attribute attribute;

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
    if (eventsEnabled) {
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
    if (component instanceof DataStoreQueryTextField) {
      final DataStoreQueryTextField queryField = (DataStoreQueryTextField)component;
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
      field.addPropertyChangeListener(fieldName, this);
    }
  }

  @Override
  public void changedUpdate(final DocumentEvent event) {
    updateCondition();
  }

  public void clear() {
    try {
      this.settingFilter = true;
      String searchField = previousSearchFieldName;
      if (!StringUtils.hasText(searchField)) {
        searchField = layer.getProperty("searchField");
        if (!StringUtils.hasText(searchField)) {
          searchField = metaData.getAttributeNames().get(0);
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

  public AbstractDataObjectLayer getLayer() {
    return this.layer;
  }

  public String getSearchFieldName() {
    return (String)this.nameField.getSelectedItem();
  }

  public final String getSearchOperator() {
    if (operatorField == null) {
      return "=";
    } else {
      return (String)this.operatorField.getSelectedItem();
    }
  }

  public Object getSearchValue() {
    if (searchField instanceof JTextComponent) {
      final JTextComponent textComponent = (JTextComponent)searchField;
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
    if (!settingFilter) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        final Object source = e.getSource();
        if (source == nameField) {
          final String searchFieldName = getSearchFieldName();
          setSearchFieldName(searchFieldName);
        } else if (source == operatorField) {
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
    } else if (event.getSource() == searchField) {
      updateCondition();
    }
  }

  private void removeListeners(final JComponent component) {
    if (component instanceof DataStoreQueryTextField) {
      final DataStoreQueryTextField queryField = (DataStoreQueryTextField)component;
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
      field.removePropertyChangeListener(fieldName, this);
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
              setSearchField(searchTextField);
              searchText = searchText.replaceAll("%", "");
              final String previousSearchText = searchTextField.getText();

              if (!EqualsRegistry.equal(searchText, previousSearchText)) {
                searchTextField.setFieldValue(searchText);
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
      this.settingFilter = false;
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
    if (searchField == null) {
      searchFieldPanel.setVisible(false);
    } else {
      searchFieldPanel.setVisible(true);
      addListeners(searchField);
      if (this.searchField instanceof DataStoreQueryTextField) {
        final DataStoreQueryTextField dataStoreSearchTextField = (DataStoreQueryTextField)this.searchField;
        dataStoreSearchTextField.setMaxResults(100);
        dataStoreSearchTextField.setPreferredSize(new Dimension(200, 22));
        dataStoreSearchTextField.setBelow(true);
      } else if (this.searchField instanceof JXSearchField) {
        final JXSearchField searchTextField = (JXSearchField)this.searchField;
        searchTextField.setPreferredSize(new Dimension(200, 22));
      }
      this.searchFieldPanel.add(this.searchField);
      GroupLayoutUtil.makeColumns(this.searchFieldPanel, 1, false);
    }
  }

  private void setSearchFieldName(final String searchFieldName) {
    if (!EqualsRegistry.equal(searchFieldName, previousSearchFieldName)) {
      previousSearchFieldName = searchFieldName;
      layer.setProperty("searchField", searchFieldName);
      codeTable = this.metaData.getCodeTableByColumn(searchFieldName);
      final DataObjectMetaData metaData = this.tableModel.getMetaData();
      attribute = metaData.getAttribute(searchFieldName);
      final Class<?> attributeClass = attribute.getTypeClass();
      if (!EqualsRegistry.equal(searchFieldName, nameField.getSelectedItem())) {
        nameField.setFieldValue(searchFieldName);
      }

      ComboBox operatorField;
      if (codeTable != null
        && !searchFieldName.equals(metaData.getIdAttributeName())) {
        operatorField = codeTableOperatorField;
      } else if (Number.class.isAssignableFrom(attributeClass)) {
        operatorField = numericOperatorField;
      } else if (Date.class.isAssignableFrom(attributeClass)) {
        operatorField = dateOperatorField;
      } else {
        operatorField = generalOperatorField;
      }
      setOperatorField(operatorField);

      setSearchOperator("=");
      if (!settingFilter) {
        setSearchFilter(null);
      }
    }
  }

  private boolean setSearchFilter(final Condition filter) {
    return this.tableModel.setFilter(filter);
  }

  private boolean setSearchOperator(final String searchOperator) {
    final Object currentSearchOperator = operatorField.getSelectedItem();
    if (!EqualsRegistry.equal(searchOperator, currentSearchOperator)) {
      operatorField.setSelectedItem(searchOperator);
    }
    if (operatorField.getSelectedIndex() < 0) {
      return false;
    } else {
      JComponent searchField = null;
      if ("Like".equals(searchOperator)) {
        codeTable = null;
        searchField = this.searchTextField;
      } else if ("IS NULL".equals(searchOperator)) {
        if (!settingFilter) {
          setFilter(F.isNull(attribute));
        }
        codeTable = null;
      } else if ("IS NOT NULL".equals(searchOperator)) {
        if (!settingFilter) {
          setFilter(F.isNotNull(attribute));
        }
        codeTable = null;
      } else {
        searchField = QueryWhereConditionField.createSearchField(attribute,
          codeTable);
      }

      setSearchField(searchField);
      return true;
    }
  }

  public void showAdvancedFilter() {
    final Condition filter = getFilter();
    final QueryWhereConditionField advancedFilter = new QueryWhereConditionField(
      layer, this, filter);
    advancedFilter.showDialog(this);
  }

  public void updateCondition() {
    if (eventsEnabled) {
      final Object searchValue = getSearchValue();
      Condition condition = null;
      final String searchOperator = getSearchOperator();
      if ("IS NULL".equalsIgnoreCase(searchOperator)) {
        condition = F.isNull(attribute);
      } else if ("IS NOT NULL".equalsIgnoreCase(searchOperator)) {
        condition = F.isNotNull(attribute);
      } else if (attribute != null) {
        if (StringUtils.hasText(StringConverterRegistry.toString(searchValue))) {
          if ("Like".equalsIgnoreCase(searchOperator)) {
            final String searchText = StringConverterRegistry.toString(searchValue);
            if (StringUtils.hasText(searchText)) {
              condition = F.iLike(attribute, "%" + searchText + "%");
            }
          } else {
            Object value = null;
            if (codeTable == null) {
              try {
                final Class<?> attributeClass = attribute.getTypeClass();
                value = StringConverterRegistry.toObject(attributeClass,
                  searchValue);
              } catch (final Throwable t) {
                return;
              }
            } else {
              value = codeTable.getId(searchValue);
              if (value == null) {
                return;
              }
            }
            if (value != null) {
              condition = F.binary(attribute, searchOperator, value);
            }
          }
        }
      }
      setSearchFilter(condition);
    }
  }
}
