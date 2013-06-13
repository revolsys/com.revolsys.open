package com.revolsys.swing.map.table;

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

  public AttributeFilterPanel(final DataObjectLayer layer) {
    this.layer = layer;
    this.metaData = layer.getMetaData();
    this.attributeNames = new ArrayList<String>(metaData.getAttributeNames());
    attributeNames.remove(metaData.getGeometryAttributeName());
    nameField = new ComboBox(false, attributeNames.toArray());
    add(nameField);

    nameField.addActionListener(this);
    this.searchTextField = new JXSearchField();
    this.searchField = searchTextField;
    searchTextField.addActionListener(this);

    add(searchFieldPanel);
    GroupLayoutUtil.makeColumns(this, 2);

    final String searchField = layer.getProperty("searchField");
    if (StringUtils.hasText(searchField)) {
      nameField.setSelectedItem(searchField);
    } else {
      nameField.setSelectedIndex(0);
    }
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
          if (searchField instanceof DataStoreSearchTextField) {
            final DataStoreSearchTextField dataStoreSearchTextField = (DataStoreSearchTextField)searchField;
            dataStoreSearchTextField.addItemListener(this);
            dataStoreSearchTextField.setMaxResults(5);
          } else if (searchField instanceof JXSearchField) {
            final JXSearchField searchTextField = (JXSearchField)searchField;
            searchTextField.addActionListener(this);
          } else if (searchField instanceof JComboBox) {
            final JComboBox comboField = (JComboBox)searchField;
            comboField.addActionListener(this);
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

  public Object getSearchValue() {
    final Object value = SwingUtil.getValue(searchField);
    return value;
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    final Object searchValue = getSearchValue();
    final Object oldValue = previousSearchValue;
    previousSearchValue = searchValue;
    firePropertyChange("searchValue", oldValue, searchValue);
  }

}
