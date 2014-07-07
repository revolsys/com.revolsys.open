package com.revolsys.swing.field;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.ItemSelectable;
import java.awt.Rectangle;
import java.awt.Toolkit;
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
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;

import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.springframework.util.StringUtils;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.LruMap;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.equals.StringEqualsIgnoreCase;
import com.revolsys.data.io.RecordStore;
import com.revolsys.data.query.Equal;
import com.revolsys.data.query.Q;
import com.revolsys.data.query.Query;
import com.revolsys.data.query.Value;
import com.revolsys.data.query.functions.F;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.listener.WeakFocusListener;
import com.revolsys.swing.map.list.RecordListCellRenderer;
import com.revolsys.swing.menu.PopupMenu;

public class RecordStoreQueryTextField extends TextField implements
  DocumentListener, KeyListener, MouseListener, FocusListener,
  ListDataListener, ItemSelectable, Field, ListSelectionListener,
  HighlightPredicate {
  private static final Icon ICON_DELETE = SilkIconLoader.getIcon("delete");

  private static final long serialVersionUID = 1L;

  private final RecordStore recordStore;

  private final String displayAttributeName;

  private final String idAttributeName;

  private final JXList list;

  private final RecordStoreQueryListModel listModel;

  private final JPopupMenu menu = new JPopupMenu();

  private final RecordDefinition recordDefinition;

  private final JLabel oldValueItem;

  public Record selectedItem;

  private final Map<String, String> valueToDisplayMap = new LruMap<String, String>(
    100);

  private Object originalValue;

  private boolean below = false;

  public RecordStoreQueryTextField(final RecordDefinition recordDefinition,
    final String displayAttributeName) {
    this(recordDefinition, displayAttributeName, new Query(recordDefinition, new Equal(
      F.upper(displayAttributeName), new Value(null))), new Query(recordDefinition,
      Q.iLike(displayAttributeName, "")));

  }

  public RecordStoreQueryTextField(final RecordDefinition recordDefinition,
    final String displayAttributeName, final List<Query> queries) {
    super(displayAttributeName);
    this.recordDefinition = recordDefinition;
    this.recordStore = recordDefinition.getDataStore();
    this.idAttributeName = recordDefinition.getIdAttributeName();
    this.displayAttributeName = displayAttributeName;

    final Document document = getDocument();
    document.addDocumentListener(this);
    addFocusListener(new WeakFocusListener(this));
    addKeyListener(this);
    addMouseListener(this);

    this.menu.setLayout(new BorderLayout(2, 2));
    this.oldValueItem = new JLabel();
    this.oldValueItem.addMouseListener(this);
    this.oldValueItem.setForeground(new Color(128, 128, 128));
    this.oldValueItem.setFont(SwingUtil.FONT);
    this.oldValueItem.setHorizontalAlignment(SwingConstants.LEFT);
    this.menu.add(this.oldValueItem, BorderLayout.NORTH);

    this.listModel = new RecordStoreQueryListModel(this.recordStore,
      displayAttributeName, queries);
    this.list = new JXList(this.listModel);
    this.list.setCellRenderer(new RecordListCellRenderer(
      displayAttributeName));
    this.list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.list.setHighlighters(HighlighterFactory.createSimpleStriping(Color.LIGHT_GRAY));
    this.list.addMouseListener(this);
    this.listModel.addListDataListener(this);
    this.list.addListSelectionListener(this);
    this.list.addHighlighter(new ColorHighlighter(this, WebColors.Blue,
      WebColors.White));

    this.menu.add(new JScrollPane(this.list), BorderLayout.CENTER);
    this.menu.setFocusable(false);
    this.menu.setBorderPainted(true);
    this.menu.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createLineBorder(Color.DARK_GRAY),
      BorderFactory.createEmptyBorder(1, 2, 1, 2)));

    setEditable(true);
    PopupMenu.getPopupMenuFactory(this);
    setPreferredSize(new Dimension(100, 22));
  }

  public RecordStoreQueryTextField(final RecordDefinition recordDefinition,
    final String displayAttributeName, final Query... queries) {
    this(recordDefinition, displayAttributeName, Arrays.asList(queries));

  }

  public RecordStoreQueryTextField(final RecordStore recordStore,
    final String typeName, final String displayAttributeName) {
    this(recordStore.getRecordDefinition(typeName), displayAttributeName, new Query(
      typeName, new Equal(F.upper(displayAttributeName), new Value(null))),
      new Query(typeName, Q.iLike(displayAttributeName, "")));
  }

  @Override
  public void addItemListener(final ItemListener l) {
    this.listenerList.add(ItemListener.class, l);
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
    final Object[] listeners = this.listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ItemListener.class) {
        ((ItemListener)listeners[i + 1]).itemStateChanged(e);
      }
    }
  }

  @Override
  public void firePropertyChange(final String propertyName,
    final Object oldValue, final Object newValue) {
    super.firePropertyChange(propertyName, oldValue, newValue);
  }

  @Override
  public void focusGained(final FocusEvent e) {
    showMenu();
  }

  @Override
  public void focusLost(final FocusEvent e) {
    final Component oppositeComponent = e.getOppositeComponent();
    if (oppositeComponent != this.list) {
      this.menu.setVisible(false);
    }
  }

  @Override
  protected String getDisplayText(final Object value) {
    final String stringValue = StringConverterRegistry.toString(value);
    String displayText = this.valueToDisplayMap.get(stringValue);
    if (!StringUtils.hasText(displayText) && StringUtils.hasText(stringValue)) {
      final Record record = this.recordStore.queryFirst(Query.equal(
        this.recordDefinition, this.idAttributeName, stringValue));
      if (record == null) {
        displayText = stringValue;
      } else {
        displayText = record.getString(this.displayAttributeName);
      }
    }
    return displayText;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getFieldValue() {
    if (this.selectedItem == null) {
      return (T)super.getFieldValue();
    } else {
      return (T)this.selectedItem.getIdentifier();
    }
  }

  public ItemListener[] getItemListeners() {
    return this.listenerList.getListeners(ItemListener.class);
  }

  public Record getSelectedItem() {
    return this.selectedItem;
  }

  @Override
  public Object[] getSelectedObjects() {
    final Record selectedItem = getSelectedItem();
    if (selectedItem == null) {
      return null;
    } else {
      return new Object[] {
        selectedItem
      };
    }
  }

  @Override
  public void insertUpdate(final DocumentEvent e) {
    search();
  }

  @Override
  public void intervalAdded(final ListDataEvent e) {
    this.list.getSelectionModel().clearSelection();
  }

  @Override
  public void intervalRemoved(final ListDataEvent e) {
    intervalAdded(e);
  }

  @Override
  public boolean isFieldValid() {
    return true;
  }

  @Override
  public boolean isHighlighted(final Component renderer,
    final ComponentAdapter adapter) {
    final Record object = this.listModel.getElementAt(adapter.row);
    final String text = getText();
    final String value = object.getString(this.displayAttributeName);
    if (StringEqualsIgnoreCase.equal(text, value)) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isTextSameAsSelected() {
    if (this.selectedItem == null) {
      return false;
    } else {
      final String text = getText();
      if (StringUtils.hasText(text)) {
        final String value = this.selectedItem.getString(this.displayAttributeName);
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
    final int size = this.listModel.getSize();
    int selectedIndex = this.list.getSelectedIndex();
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
        this.list.setSelectedIndex(selectedIndex);
        e.consume();
      break;
      case KeyEvent.VK_ENTER:
        if (size > 0) {
          if (selectedIndex >= 0 && selectedIndex < size) {
            final Record selectedItem = this.listModel.getElementAt(selectedIndex);
            final String text = selectedItem.getString(this.displayAttributeName);
            if (!text.equals(this.getText())) {
              this.selectedItem = selectedItem;
              setText(text);
            }
          }
        }
        return;
      case KeyEvent.VK_TAB:
        return;
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
    if (e.getSource() == this.oldValueItem) {
      if (e.getX() < 18) {
        setFieldValue(null);
      } else {
        setFieldValue(this.originalValue);
      }
      this.menu.setVisible(false);
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
    this.listenerList.remove(ItemListener.class, l);
  }

  @Override
  public void removeUpdate(final DocumentEvent e) {
    search();
  }

  protected void search() {
    if (this.selectedItem != null) {
      final Record oldValue = this.selectedItem;
      this.selectedItem = null;
      fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED,
        oldValue, ItemEvent.DESELECTED));
    }
    final String text = getText();
    this.listModel.setSearchText(text);
    if (isShowing()) {
      showMenu();
      this.selectedItem = this.listModel.getSelectedItem();
      if (this.selectedItem != null) {
        fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED,
          this.selectedItem, ItemEvent.SELECTED));
      }
    }
  }

  public void setBelow(final boolean below) {
    this.below = below;
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    setToolTipText(toolTip);
  }

  @Override
  public void setFieldValue(final Object value) {
    super.setFieldValue(value);
    this.originalValue = value;
    Icon icon;
    String originalText;
    if (value == null) {
      originalText = "-";
      icon = null;
    } else {
      originalText = getDisplayText(value);
      icon = ICON_DELETE;
    }
    this.oldValueItem.setIcon(icon);
    this.oldValueItem.setText(originalText);
  }

  public void setMaxResults(final int maxResults) {
    this.listModel.setMaxResults(maxResults);
  }

  @Override
  public void setText(final String text) {
    super.setText(text);
    if (this.listModel != null) {
      search();
    }
  }

  private void showMenu() {
    final List<Record> objects = this.listModel.getObjects();
    if (objects.isEmpty()) {
      this.menu.setVisible(false);
    } else {
      this.menu.setVisible(true);
      int x;
      int y;
      if (this.below) {
        x = 0;
        final Insets screenInsets = Toolkit.getDefaultToolkit()
          .getScreenInsets(getGraphicsConfiguration());

        final Rectangle bounds = getGraphicsConfiguration().getBounds();
        final int menuHeight = this.menu.getBounds().height;
        final int screenY = getLocationOnScreen().y;
        final int componentHeight = getHeight();
        final int bottomOfMenu = screenY + menuHeight + componentHeight;
        if (bottomOfMenu > bounds.height - screenInsets.bottom) {
          y = -menuHeight;
        } else {
          y = componentHeight;

        }
      } else {
        x = this.getWidth();
        y = 0;
      }
      this.menu.show(this, x, y);
      this.menu.pack();
    }
  }

  @Override
  public void updateFieldValue() {
    // setFieldValue(listModel.getSelectedItem());
  }

  @Override
  public void valueChanged(final ListSelectionEvent e) {
    if (!e.getValueIsAdjusting()) {
      final Record value = (Record)this.list.getSelectedValue();
      if (value != null) {
        final String label = value.getString(this.displayAttributeName);
        if (!EqualsRegistry.equal(label, getText())) {
          setFieldValue(value.getIdentifier());
        }
      }
      this.menu.setVisible(false);
      requestFocus();
    }

  }
}
