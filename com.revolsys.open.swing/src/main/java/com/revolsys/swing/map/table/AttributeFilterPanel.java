package com.revolsys.swing.map.table;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXSearchField;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.spring.SpelUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.DataStoreSearchTextField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;

public class AttributeFilterPanel extends JComponent implements ActionListener,
  ItemListener {

  private static final long serialVersionUID = 1L;

  private String previousAttributeName;

  private Object previousSearchValue;

  private final List<String> attributeNames;

  private JComponent searchField;

  private final JPanel searchFieldPanel = new JPanel();

  private final ComboBox nameField;

  private final JXSearchField searchTextField;

  private final DataObjectMetaData metaData;

  private final DataObjectLayer layer;

  private final ComboBox operatorField;

  private String previousSearchOperator;

  public AttributeFilterPanel(final DataObjectLayer layer) {
    this.layer = layer;
    this.metaData = layer.getMetaData();
    this.attributeNames = new ArrayList<String>(metaData.getAttributeNames());
    attributeNames.remove(metaData.getGeometryAttributeName());
    final AttributeTitleStringConveter converter = new AttributeTitleStringConveter(
      metaData);
    nameField = new ComboBox(converter, false, attributeNames.toArray());
    nameField.setRenderer(converter);
    nameField.addActionListener(this);
    add(nameField);

    operatorField = new ComboBox("=", "Like");
    operatorField.setSelectedIndex(0);
    operatorField.addItemListener(this);
    add(operatorField);

    this.searchTextField = new JXSearchField();
    this.searchField = searchTextField;
    searchTextField.addActionListener(this);
    searchTextField.setMinimumSize(new Dimension(200, 10));

    add(searchFieldPanel);
    GroupLayoutUtil.makeColumns(this, 3);

    final String searchField = layer.getProperty("searchField");
    setSearchField(searchField);
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    try {
      final Object source = event.getSource();
      if (source == searchField) {
        final Object searchValue = getSearchValue();
        final Object oldValue = previousSearchValue;
        previousSearchValue = searchValue;
        firePropertyChange("searchValue", oldValue, searchValue);
      } else if (source == nameField) {
        final String searchAttribute = getSearchAttribute();
        final Object oldValue = previousAttributeName;
        previousAttributeName = searchAttribute;
        if (!EqualsRegistry.equal(searchAttribute, oldValue)) {
          final CodeTable codeTable = metaData.getCodeTableByColumn(searchAttribute);
          searchFieldPanel.removeAll();
          if (searchField instanceof DataStoreSearchTextField) {
            final DataStoreSearchTextField dataStoreSearchTextField = (DataStoreSearchTextField)searchField;
            dataStoreSearchTextField.removeItemListener(this);
          } else if (searchField instanceof JXSearchField) {
            final JXSearchField searchTextField = (JXSearchField)searchField;
            searchTextField.removeActionListener(this);
          } else if (searchField instanceof JComboBox) {
            final JComboBox comboField = (JComboBox)searchField;
            comboField.removeActionListener(this);
          }
          final Attribute attribute = metaData.getAttribute(searchAttribute);
          final String searchFieldFactory = attribute.getProperty("searchFieldFactory");
          if (StringUtils.hasText(searchFieldFactory)) {
            final Map<String, Object> searchFieldFactoryParameters = attribute.getProperty("searchFieldFactoryParameters");
            searchField = SpelUtil.getValue(searchFieldFactory, attribute,
              searchFieldFactoryParameters);
          } else if (codeTable == null) {
            searchField = this.searchTextField;
          } else {
            searchField = SwingUtil.createComboBox(codeTable, false);
          }
          operatorField.setSelectedItem("=");
          if (searchField instanceof DataStoreSearchTextField) {
            final DataStoreSearchTextField dataStoreSearchTextField = (DataStoreSearchTextField)searchField;
            dataStoreSearchTextField.addItemListener(this);
            dataStoreSearchTextField.setMaxResults(5);
            operatorField.setEnabled(false);
          } else if (searchField instanceof JXSearchField) {
            final JXSearchField searchTextField = (JXSearchField)searchField;
            searchTextField.addActionListener(this);
            operatorField.setEnabled(true);
          } else if (searchField instanceof JComboBox) {
            final JComboBox comboField = (JComboBox)searchField;
            comboField.addActionListener(this);
            operatorField.setEnabled(false);
          }
          searchFieldPanel.add(searchField);
          GroupLayoutUtil.makeColumns(searchFieldPanel, 1);

          firePropertyChange("searchAttribute", oldValue, searchAttribute);
        }
      }
    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error("Unable to search", e);
    }
  }

  public void clear() {
    nameField.setSelectedIndex(0);
    SwingUtil.setFieldValue(searchField, null);
  }

  public List<String> getAttributeNames() {
    return attributeNames;
  }

  public DataObjectLayer getLayer() {
    return layer;
  }

  public String getSearchAttribute() {
    return (String)nameField.getSelectedItem();
  }

  public final String getSearchOperator() {
    return (String)operatorField.getSelectedItem();
  }

  public Object getSearchValue() {
    final Object value = SwingUtil.getValue(searchField);
    return value;
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    final Object source = e.getSource();
    if (source == searchField) {
      final Object searchValue = getSearchValue();
      final Object oldValue = previousSearchValue;
      previousSearchValue = searchValue;
      firePropertyChange("searchValue", oldValue, searchValue);
    } else if (source == operatorField) {
      final String searchOperator = getSearchOperator();
      final String oldValue = previousSearchOperator;
      previousSearchOperator = searchOperator;
      firePropertyChange("searchOperator", oldValue, searchOperator);
    } else if (source == nameField) {
      final String searchAttribute = getSearchAttribute();
      final String oldValue = previousAttributeName;
      previousAttributeName = searchAttribute;
      firePropertyChange("searchAttribute", oldValue, searchAttribute);
    }
  }

  public void setSearchField(final String searchField) {
    if (StringUtils.hasText(searchField)) {
      nameField.setSelectedItem(searchField);
    } else {
      nameField.setSelectedIndex(0);
    }
  }

}
