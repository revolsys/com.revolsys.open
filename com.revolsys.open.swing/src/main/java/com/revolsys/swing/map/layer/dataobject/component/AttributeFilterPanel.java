package com.revolsys.swing.map.layer.dataobject.component;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXSearchField;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.query.BinaryCondition;
import com.revolsys.gis.data.query.Column;
import com.revolsys.gis.data.query.Condition;
import com.revolsys.gis.data.query.Conditions;
import com.revolsys.gis.data.query.Value;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.spring.SpelUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.DataStoreSearchTextField;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.field.QueryWhereConditionField;
import com.revolsys.swing.field.SearchField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.table.DataObjectLayerTablePanel;
import com.revolsys.swing.map.layer.dataobject.table.model.DataObjectLayerTableModel;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.JavaBeanUtil;

public class AttributeFilterPanel extends JComponent implements ActionListener,
  ItemListener {

  private static final long serialVersionUID = 1L;

  private String previousAttributeName;

  private final List<String> attributeNames;

  private JComponent searchField;

  private final JPanel searchFieldPanel = new JPanel();

  private final ComboBox nameField;

  private final JXSearchField searchTextField;

  private final DataObjectMetaData metaData;

  private final DataObjectLayer layer;

  private final ComboBox operatorField;

  private final DataObjectLayerTableModel tableModel;

  private boolean eventsEnabled = true;

  public AttributeFilterPanel(final DataObjectLayerTablePanel tablePanel) {
    this.tableModel = tablePanel.getTableModel();
    this.layer = tablePanel.getLayer();
    this.metaData = layer.getMetaData();

    this.attributeNames = new ArrayList<String>(
      this.metaData.getAttributeNames());
    this.attributeNames.remove(this.metaData.getGeometryAttributeName());
    final AttributeTitleStringConveter converter = new AttributeTitleStringConveter(
      this.metaData);
    this.nameField = new ComboBox(converter, false,
      this.attributeNames.toArray());
    this.nameField.setRenderer(converter);
    this.nameField.addActionListener(this);
    add(this.nameField);

    this.operatorField = new ComboBox("=", "Like");
    this.operatorField.setSelectedIndex(0);
    this.operatorField.addItemListener(this);
    add(this.operatorField);

    this.searchTextField = new SearchField();
    this.searchField = this.searchTextField;
    this.searchTextField.addActionListener(this);
    this.searchTextField.setPreferredSize(new Dimension(200, 22));
    add(this.searchFieldPanel);

    GroupLayoutUtil.makeColumns(this, 3, false);

    final String searchField = layer.getProperty("searchField");
    setSearchField(searchField);
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    if (eventsEnabled) {
      try {
        final Object source = event.getSource();
        if (source == this.searchField) {
          updateCondition();
        } else if (source == this.nameField) {
          final String searchAttribute = getSearchAttribute();
          final Object oldValue = this.previousAttributeName;
          this.previousAttributeName = searchAttribute;
          if (!EqualsRegistry.equal(searchAttribute, oldValue)) {
            final CodeTable codeTable = this.metaData.getCodeTableByColumn(searchAttribute);
            this.searchFieldPanel.removeAll();
            removeListeners(this.searchField);
            final Attribute attribute = this.metaData.getAttribute(searchAttribute);
            final String searchFieldFactory = attribute.getProperty("searchFieldFactory");
            if (StringUtils.hasText(searchFieldFactory)) {
              final Map<String, Object> searchFieldFactoryParameters = attribute.getProperty("searchFieldFactoryParameters");
              this.searchField = SpelUtil.getValue(searchFieldFactory,
                attribute, searchFieldFactoryParameters);
            } else if (codeTable == null) {
              this.searchField = this.searchTextField;
            } else {
              this.searchField = SwingUtil.createComboBox(codeTable, false);
            }
            this.operatorField.setSelectedItem("=");
            addListeners(searchField);
            if (this.searchField instanceof DataStoreSearchTextField) {
              final DataStoreSearchTextField dataStoreSearchTextField = (DataStoreSearchTextField)this.searchField;
              dataStoreSearchTextField.setMaxResults(5);
              this.operatorField.setEnabled(false);
              dataStoreSearchTextField.setPreferredSize(new Dimension(200, 22));
            } else if (this.searchField instanceof JXSearchField) {
              final JXSearchField searchTextField = (JXSearchField)this.searchField;
              this.operatorField.setEnabled(true);
              searchTextField.setPreferredSize(new Dimension(200, 22));
            } else if (this.searchField instanceof JComboBox) {
              this.operatorField.setEnabled(false);
            }
            this.searchFieldPanel.add(this.searchField);
            GroupLayoutUtil.makeColumns(this.searchFieldPanel, 1, false);

            updateCondition();
          }
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
    } else if (component instanceof JComboBox) {
      final JComboBox comboField = (JComboBox)component;
      comboField.addActionListener(this);
    }
  }

  public void clear() {
    this.nameField.setSelectedIndex(0);
    setSearchCondition(null);
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

  public DataObjectLayer getLayer() {
    return this.layer;
  }

  public String getSearchAttribute() {
    return (String)this.nameField.getSelectedItem();
  }

  public Condition getSearchCondition() {
    return this.tableModel.getSearchCondition();
  }

  public final String getSearchOperator() {
    return (String)this.operatorField.getSelectedItem();
  }

  public Object getSearchValue() {
    final Object value = SwingUtil.getValue(this.searchField);
    return value;
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
    }
  }

  public void setSearchCondition(final Condition condition) {
    final boolean eventsEnabled = this.eventsEnabled;
    try {
      this.eventsEnabled = false;
      this.tableModel.setSearchCondition(condition);
      boolean simple = condition == null;
      if (condition instanceof BinaryCondition) {
        final BinaryCondition binaryCondition = (BinaryCondition)condition;
        final Condition leftCondition = binaryCondition.getLeft();
        final Condition rightCondition = binaryCondition.getRight();
        if ("=".equals(binaryCondition.getOperator())) {
          if (leftCondition instanceof Column
            && rightCondition instanceof Value) {
            final Column column = (Column)leftCondition;
            final Value value = (Value)rightCondition;
            nameField.setFieldValue(column.getName());
            operatorField.setFieldValue("=");
            ((Field)searchField).setFieldValue(StringConverterRegistry.toString(value.getValue()));
            simple = true;
          }
        }
      }
      if (simple) {
        operatorField.setEnabled(true);
        searchField.setEnabled(true);
        nameField.setEnabled(true);
      } else {
        nameField.setEnabled(false);
        operatorField.setEnabled(false);
        searchField.setEnabled(false);
      }
    } finally {
      this.eventsEnabled = eventsEnabled;
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
      final String searchAttribute = getSearchAttribute();
      final Object searchValue = getSearchValue();
      Condition condition = null;
      if (StringUtils.hasText(searchAttribute)
        && StringUtils.hasText(StringConverterRegistry.toString(searchValue))) {
        final String searchOperator = getSearchOperator();
        if ("Like".equalsIgnoreCase(searchOperator)) {
          final String searchText = (String)searchValue;
          if (StringUtils.hasText(searchText)) {
            condition = Conditions.likeUpper(searchAttribute, searchText);
          }
        } else {
          final DataObjectMetaData metaData = this.tableModel.getMetaData();
          final Class<?> attributeClass = metaData.getAttributeClass(searchAttribute);
          try {
            final Object value = StringConverterRegistry.toObject(
              attributeClass, searchValue);
            condition = Conditions.equal(searchAttribute, value);
          } catch (final Throwable t) {
          }
        }
      }
      setSearchCondition(condition);
    }
  }

}
