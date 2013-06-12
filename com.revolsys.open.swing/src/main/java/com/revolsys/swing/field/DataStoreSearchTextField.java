package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.ItemSelectable;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.Document;

import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXSearchField;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.springframework.util.StringUtils;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Conditions;
import com.revolsys.gis.data.query.Function;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.data.query.Value;
import com.revolsys.swing.map.list.DataObjectListCellRenderer;
import com.revolsys.swing.menu.PopupMenu;

public class DataStoreSearchTextField extends JXSearchField implements
  DocumentListener, KeyListener, MouseListener, FocusListener,
  ListDataListener, ItemSelectable, Field {
  private static final long serialVersionUID = 1L;

  private final String displayAttributeName;

  private final JXList list;

  private final JPopupMenu menu = new JPopupMenu();

  private final DataStoreQueryListModel listModel;

  public DataObject selectedItem;

  private String errorMessage;

  public DataStoreSearchTextField(final Attribute attribute) {
    this(attribute.getMetaData(), attribute.getName());
  }

  public DataStoreSearchTextField(final DataObjectMetaData metaData,
    final String displayAttributeName) {
    this(metaData.getDataObjectStore(), displayAttributeName, new Query(
      metaData, Conditions.equal(Function.upper(displayAttributeName),
        new Value(null))), new Query(metaData, Conditions.likeUpper(
      displayAttributeName, "")));
  }

  public DataStoreSearchTextField(final DataObjectStore dataStore,
    final String displayAttributeName, final List<Query> queries) {
    this.displayAttributeName = displayAttributeName;

    final Document document = getDocument();
    document.addDocumentListener(this);
    addFocusListener(this);
    addKeyListener(this);
    addMouseListener(this);

    listModel = new DataStoreQueryListModel(dataStore, displayAttributeName,
      queries);
    list = new JXList(listModel);
    list.setCellRenderer(new DataObjectListCellRenderer(displayAttributeName));
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setHighlighters(HighlighterFactory.createSimpleStriping(Color.LIGHT_GRAY));
    list.addMouseListener(this);
    listModel.addListDataListener(this);

    menu.add(list);
    menu.setFocusable(false);
    menu.setBorderPainted(true);
    menu.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createLineBorder(Color.DARK_GRAY),
      BorderFactory.createEmptyBorder(1, 2, 1, 2)));

    setEditable(true);
    setSearchMode(SearchMode.REGULAR);
    PopupMenu.getPopupMenuFactory(this);
  }

  public DataStoreSearchTextField(final DataObjectStore dataStore,
    final String displayAttributeName, final Query... queries) {
    this(dataStore, displayAttributeName, Arrays.asList(queries));

  }

  public DataStoreSearchTextField(final DataObjectStore dataStore,
    final String typeName, final String displayAttributeName) {
    this(dataStore, displayAttributeName, new Query(typeName, Conditions.equal(
      Function.upper(displayAttributeName), new Value(null))), new Query(
      typeName, Conditions.likeUpper(displayAttributeName, "")));
  }

  @Override
  public void addItemListener(final ItemListener l) {
    listenerList.add(ItemListener.class, l);
  }

  @Override
  public void changedUpdate(final DocumentEvent e) {
    search();
  }

  @Override
  public void contentsChanged(final ListDataEvent e) {
    intervalAdded(e);
  }

  protected void fireItemStateChanged(final ItemEvent e) {
    final Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ItemListener.class) {
        ((ItemListener)listeners[i + 1]).itemStateChanged(e);
      }
    }
  }

  @Override
  public void focusGained(final FocusEvent e) {
    showMenu();
  }

  @Override
  public void focusLost(final FocusEvent e) {
    menu.setVisible(false);
  }

  @Override
  public String getFieldName() {
    return displayAttributeName;
  }

  @Override
  public <T> T getFieldValue() {
    if (selectedItem == null) {
      return null;
    } else {
      return selectedItem.getIdValue();
    }
  }

  public ItemListener[] getItemListeners() {
    return listenerList.getListeners(ItemListener.class);
  }

  public DataObject getSelectedItem() {
    return selectedItem;
  }

  @Override
  public Object[] getSelectedObjects() {
    final DataObject selectedItem = getSelectedItem();
    if (selectedItem == null) {
      return null;
    } else {
      return new Object[] {
        selectedItem
      };
    }
  }

  @Override
  public String getToolTipText() {
    if (StringUtils.hasText(errorMessage)) {
      return errorMessage;
    } else {
      return super.getToolTipText();
    }
  }

  @Override
  public void insertUpdate(final DocumentEvent e) {
    search();
  }

  @Override
  public void intervalAdded(final ListDataEvent e) {
    list.getSelectionModel().clearSelection();
  }

  @Override
  public void intervalRemoved(final ListDataEvent e) {
    intervalAdded(e);
  }

  public boolean isTextSameAsSelected() {
    if (selectedItem == null) {
      return false;
    } else {
      final String text = getText();
      if (StringUtils.hasText(text)) {
        final String value = selectedItem.getString(displayAttributeName);
        return text.equalsIgnoreCase(value);
      } else {
        return false;
      }
    }
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    final int keyCode = e.getKeyCode();
    int increment = 1;
    final int size = listModel.getSize();
    int selectedIndex = list.getSelectedIndex();
    switch (keyCode) {
      case KeyEvent.VK_UP:
        increment = -1;
      case KeyEvent.VK_DOWN:
        if (selectedIndex >= size) {
          selectedIndex = -1;
        }
        selectedIndex += increment;
        if (selectedIndex < 0) {
          selectedIndex = 0;
        } else if (selectedIndex >= size) {
          selectedIndex = size - 1;
        }
        list.setSelectedIndex(selectedIndex);
        e.consume();
      break;
      case KeyEvent.VK_ENTER:
        if (size > 0) {
          if (selectedIndex >= 0 && selectedIndex < size) {
            final DataObject selectedItem = listModel.getElementAt(selectedIndex);
            final String text = selectedItem.getString(displayAttributeName);
            if (!text.equals(this.getText())) {
              this.selectedItem = selectedItem;
              setText(text);
              e.consume();
              return;
            }
          }
        }
        if (listModel.getSelectedItem() != null) {
          final JButton findButton = getFindButton();
          findButton.doClick();
          setText("");
        }
      break;
      default:
      break;
    }
    showMenu();
  }

  @Override
  public void keyReleased(final KeyEvent e) {
  }

  @Override
  public void keyTyped(final KeyEvent e) {
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    if (e.getSource() == list) {
      final Point point = e.getPoint();
      final int index = list.locationToIndex(point);
      if (index > -1) {
        list.setSelectedIndex(index);
        final DataObject value = listModel.getElementAt(index);
        if (value != null) {
          final String label = value.getString(displayAttributeName);
          setText(label);
        }
        requestFocus();
      }
    }
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
  }

  @Override
  public void mouseExited(final MouseEvent e) {
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    if (e.getSource() == this) {
      showMenu();
    }
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
  }

  @Override
  public void removeItemListener(final ItemListener l) {
    listenerList.remove(ItemListener.class, l);
  }

  @Override
  public void removeUpdate(final DocumentEvent e) {
    search();
  }

  protected void search() {
    if (selectedItem != null) {
      final DataObject oldValue = selectedItem;
      selectedItem = null;
      fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED,
        oldValue, ItemEvent.DESELECTED));
    }
    final String text = getText();
    listModel.setSearchText(text);
    showMenu();

    selectedItem = listModel.getSelectedItem();
    if (selectedItem != null) {
      fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED,
        selectedItem, ItemEvent.SELECTED));
    }
  }

  @Override
  public void setFieldInvalid(final String message) {
    setForeground(Color.RED);
    setSelectedTextColor(Color.RED);
    setBackground(Color.PINK);
    this.errorMessage = message;
  }

  @Override
  public void setFieldValid() {
    setForeground(TextField.DEFAULT_FOREGROUND);
    setSelectedTextColor(TextField.DEFAULT_SELECTED_FOREGROUND);
    setBackground(TextField.DEFAULT_BACKGROUND);
    this.errorMessage = null;
  }

  @Override
  public void setFieldValue(final Object value) {
    // TODO Auto-generated method stub

  }

  public void setMaxResults(final int maxResults) {
    listModel.setMaxResults(maxResults);
  }

  @Override
  public void setText(final String text) {
    super.setText(text);
    search();
  }

  private void showMenu() {
    final List<DataObject> objects = listModel.getObjects();
    if (objects.isEmpty()) {
      menu.setVisible(false);
    } else {
      menu.setVisible(true);
      menu.show(this, 0, this.getHeight());
      menu.pack();
      if (objects.size() == 1) {
        list.setSelectedIndex(0);
      } else {
        int i = 0;
        for (final DataObject object : objects) {
          final String value = object.getString(displayAttributeName);
          final String text = getText();
          if (value.equals(text)) {
            list.setSelectedIndex(i);
          }
          i++;
        }
      }
    }
  }

}
