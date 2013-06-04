package com.revolsys.swing.map.table;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXSearchField;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.layout.GroupLayoutUtil;

public class AttributeFilterPanel extends JComponent implements ActionListener {
  private static final long serialVersionUID = 1L;

  private String previousAttributeName;

  private String previousSearchText;

  private final List<String> attributeNames;

  private JComponent searchField;

  private final JPanel searchFieldPanel = new JPanel();

  private final JComboBox nameField;

  private final JXSearchField searchTextField;

  private final DataObjectMetaData metaData;

  public AttributeFilterPanel(final DataObjectMetaData metaData) {
    this(metaData, metaData.getAttributeNames());
  }

  public AttributeFilterPanel(final DataObjectMetaData metaData,
    final Collection<String> attributeNames) {
    this.metaData = metaData;
    this.attributeNames = new ArrayList<String>(attributeNames);
    attributeNames.remove(metaData.getGeometryAttributeName());
    nameField = new JComboBox(attributeNames.toArray());
    add(nameField);

    nameField.addActionListener(this);
    this.searchTextField = new JXSearchField();
    this.searchField = searchTextField;
    searchTextField.addActionListener(this);

    add(searchFieldPanel);
    GroupLayoutUtil.makeColumns(this, 2);
    nameField.setSelectedIndex(0);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final Object source = e.getSource();
    if (source == searchField) {
      final String searchText = getSearchText();
      final Object oldValue = previousSearchText;
      previousSearchText = searchText;
      firePropertyChange("searchText", oldValue, searchText);
    } else if (source == nameField) {
      final String searchAttribute = getSearchAttribute();
      final Object oldValue = previousAttributeName;
      previousAttributeName = searchAttribute;
      if (!EqualsRegistry.equal(searchAttribute, oldValue)) {
        final CodeTable codeTable = metaData.getCodeTableByColumn(searchAttribute);
        searchFieldPanel.removeAll();
        if (searchField instanceof JXSearchField) {
          final JXSearchField searchTextField = (JXSearchField)searchField;
          searchTextField.removeActionListener(this);
        } else if (searchField instanceof JComboBox) {
          final JComboBox comboField = (JComboBox)searchField;
          comboField.removeActionListener(this);
        }
        if (codeTable == null) {
          searchField = this.searchTextField;
        } else {
          searchField = SwingUtil.createComboBox(codeTable, false);
        }
        if (searchField instanceof JXSearchField) {
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
  }

  public void clear() {
    nameField.setSelectedIndex(0);
    SwingUtil.setFieldValue(searchField, null);
  }

  public List<String> getAttributeNames() {
    return attributeNames;
  }

  public String getSearchAttribute() {
    return (String)nameField.getSelectedItem();
  }

  public String getSearchText() {
    final Object value = SwingUtil.getValue(searchField);
    return StringConverterRegistry.toString(value);
  }

}
