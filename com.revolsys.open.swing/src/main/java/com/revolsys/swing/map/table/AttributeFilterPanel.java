package com.revolsys.swing.map.table;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.jdesktop.swingx.JXSearchField;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.layout.GroupLayoutUtil;

public class AttributeFilterPanel extends JComponent implements ActionListener {
  private static final long serialVersionUID = 1L;

  private String previousAttributeName;

  private String previousSearchText;

  private final List<String> attributeNames;

  private final JComponent searchTextField;

  private final JComboBox nameField;

  public AttributeFilterPanel(final Collection<String> attributeNames) {
    this.attributeNames = new ArrayList<String>(attributeNames);
    nameField = new JComboBox(attributeNames.toArray());
    add(nameField);
    nameField.addActionListener(this);
    add(new JLabel(" like "));
    final JXSearchField searchTextField = new JXSearchField();
    this.searchTextField = searchTextField;
    searchTextField.addActionListener(this);

    add(searchTextField);
    GroupLayoutUtil.makeColumns(this, 3);
  }

  public AttributeFilterPanel(final DataObjectMetaData metaData) {
    this(metaData.getAttributeNames());
    attributeNames.remove(metaData.getGeometryAttributeName());
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final Object source = e.getSource();
    if (source == searchTextField) {
      final String searchText = getSearchText();
      final Object oldValue = previousSearchText;
      previousSearchText = searchText;
      firePropertyChange("searchText", oldValue, searchText);
    } else if (source == nameField) {
      final String searchAttribute = getSearchAttribute();
      final Object oldValue = previousAttributeName;
      previousAttributeName = searchAttribute;
      firePropertyChange("searchAttribute", oldValue, searchAttribute);
    }
  }

  public List<String> getAttributeNames() {
    return attributeNames;
  }

  public String getSearchAttribute() {
    return (String)nameField.getSelectedItem();
  }

  public String getSearchText() {
    final Object value = SwingUtil.getValue(searchTextField);
    return StringConverterRegistry.toString(value);
  }

}
