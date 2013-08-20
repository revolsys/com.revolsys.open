package com.revolsys.swing.map.table;

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

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.spring.SpelUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.DataStoreSearchTextField;
import com.revolsys.swing.field.SearchField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.parallel.SwingWorkerManager;
import com.revolsys.util.JavaBeanUtil;

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
    this.searchTextField.setPreferredSize(new Dimension(200,
      this.searchTextField.getHeight()));
    add(this.searchFieldPanel);
    GroupLayoutUtil.makeColumns(this, 3);

    final String searchField = layer.getProperty("searchField");
    setSearchField(searchField);
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    try {
      final Object source = event.getSource();
      if (source == this.searchField) {
        final Object searchValue = getSearchValue();
        final Object oldValue = this.previousSearchValue;
        this.previousSearchValue = searchValue;
        fireSearchChanged("searchValue", oldValue, searchValue);
      } else if (source == this.nameField) {
        final String searchAttribute = getSearchAttribute();
        final Object oldValue = this.previousAttributeName;
        this.previousAttributeName = searchAttribute;
        if (!EqualsRegistry.equal(searchAttribute, oldValue)) {
          final CodeTable codeTable = this.metaData.getCodeTableByColumn(searchAttribute);
          this.searchFieldPanel.removeAll();
          if (this.searchField instanceof DataStoreSearchTextField) {
            final DataStoreSearchTextField dataStoreSearchTextField = (DataStoreSearchTextField)this.searchField;
            dataStoreSearchTextField.removeItemListener(this);
          } else if (this.searchField instanceof JXSearchField) {
            final JXSearchField searchTextField = (JXSearchField)this.searchField;
            searchTextField.removeActionListener(this);
          } else if (this.searchField instanceof JComboBox) {
            final JComboBox comboField = (JComboBox)this.searchField;
            comboField.removeActionListener(this);
          }
          final Attribute attribute = this.metaData.getAttribute(searchAttribute);
          final String searchFieldFactory = attribute.getProperty("searchFieldFactory");
          if (StringUtils.hasText(searchFieldFactory)) {
            final Map<String, Object> searchFieldFactoryParameters = attribute.getProperty("searchFieldFactoryParameters");
            this.searchField = SpelUtil.getValue(searchFieldFactory, attribute,
              searchFieldFactoryParameters);
          } else if (codeTable == null) {
            this.searchField = this.searchTextField;
          } else {
            this.searchField = SwingUtil.createComboBox(codeTable, false);
          }
          this.operatorField.setSelectedItem("=");
          if (this.searchField instanceof DataStoreSearchTextField) {
            final DataStoreSearchTextField dataStoreSearchTextField = (DataStoreSearchTextField)this.searchField;
            dataStoreSearchTextField.addItemListener(this);
            dataStoreSearchTextField.setMaxResults(5);
            this.operatorField.setEnabled(false);
            dataStoreSearchTextField.setPreferredSize(new Dimension(200,
              this.searchTextField.getHeight()));
          } else if (this.searchField instanceof JXSearchField) {
            final JXSearchField searchTextField = (JXSearchField)this.searchField;
            searchTextField.addActionListener(this);
            this.operatorField.setEnabled(true);
            searchTextField.setPreferredSize(new Dimension(200,
              searchTextField.getHeight()));
          } else if (this.searchField instanceof JComboBox) {
            final JComboBox comboField = (JComboBox)this.searchField;
            comboField.addActionListener(this);
            this.operatorField.setEnabled(false);
          }
          this.searchFieldPanel.add(this.searchField);
          GroupLayoutUtil.makeColumns(this.searchFieldPanel, 1);

          fireSearchChanged("searchAttribute", oldValue, searchAttribute);
        }
      }
    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error("Unable to search", e);
    }
  }

  public void clear() {
    this.nameField.setSelectedIndex(0);
    SwingUtil.setFieldValue(this.searchField, null);
  }

  public void fireSearchChanged(final String propertyName,
    final Object oldValue, final Object newValue) {
    if (!EqualsRegistry.equal(oldValue, newValue)) {
      if (SwingUtilities.isEventDispatchThread()) {
        final Method method = JavaBeanUtil.getMethod(getClass(),
          "fireSearchChanged", String.class, Object.class, Object.class);
        SwingWorkerManager.execute("Change search", this, method, propertyName,
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

  public final String getSearchOperator() {
    return (String)this.operatorField.getSelectedItem();
  }

  public Object getSearchValue() {
    final Object value = SwingUtil.getValue(this.searchField);
    return value;
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    final Object source = e.getSource();
    if (source == this.searchField) {
      final Object searchValue = getSearchValue();
      final Object oldValue = this.previousSearchValue;
      this.previousSearchValue = searchValue;
      fireSearchChanged("searchValue", oldValue, searchValue);
    } else if (source == this.operatorField) {
      final String searchOperator = getSearchOperator();
      final String oldValue = this.previousSearchOperator;
      this.previousSearchOperator = searchOperator;
      fireSearchChanged("searchOperator", oldValue, searchOperator);
    } else if (source == this.nameField) {
      final String searchAttribute = getSearchAttribute();
      final String oldValue = this.previousAttributeName;
      this.previousAttributeName = searchAttribute;
      fireSearchChanged("searchAttribute", oldValue, searchAttribute);
    }
  }

  public void setSearchField(final String searchField) {
    if (StringUtils.hasText(searchField)) {
      this.nameField.setSelectedItem(searchField);
    } else {
      this.nameField.setSelectedIndex(0);
    }
  }

}
