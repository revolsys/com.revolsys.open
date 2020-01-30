package com.revolsys.swing.field;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jeometry.common.awt.WebColors;
import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.io.PathName;

import com.revolsys.collection.map.LruMap;
import com.revolsys.io.BaseCloseable;
import com.revolsys.record.Record;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.query.BinaryCondition;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Equal;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.Value;
import com.revolsys.record.query.functions.F;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.list.ArrayListModel;
import com.revolsys.swing.listener.ActionListenable;
import com.revolsys.swing.listener.WeakFocusListener;
import com.revolsys.swing.map.list.RecordListCellRenderer;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;
import com.revolsys.value.ThreadBooleanValue;

public abstract class AbstractRecordQueryField extends ValueField
  implements DocumentListener, KeyListener, MouseListener, FocusListener, ListDataListener,
  ListSelectionListener, HighlightPredicate, ActionListenable {
  private static final Icon ICON_DELETE = Icons.getIcon("delete");

  private static final long serialVersionUID = 1L;

  private final JXBusyLabel busyLabel = new JXBusyLabel(new Dimension(16, 16));

  private final FieldDefinition displayField;

  private final ThreadBooleanValue eventsEnabled = new ThreadBooleanValue(true);

  private final Map<Identifier, String> idToDisplayMap = new LruMap<>(100);

  private final JXList list;

  private final ArrayListModel<Record> listModel = new ArrayListModel<>();

  private int maxResults = Integer.MAX_VALUE;

  private final JPopupMenu menu = new JPopupMenu();

  private int minSearchCharacters = 2;

  private final JLabel oldValueItem;

  private Identifier originalValue;

  private final List<Query> queries;

  private final TextField searchField = new TextField("search", 50);

  private final AtomicInteger searchIndex = new AtomicInteger();

  private Record selectedRecord;

  private final PathName typePath;

  public AbstractRecordQueryField(final String fieldName, final PathName typePath,
    final FieldDefinition displayField) {
    super(fieldName, null);
    this.typePath = typePath;
    this.displayField = displayField;
    this.queries = Arrays.asList(
      new Query(typePath, new Equal(F.upper(displayField), new Value(null))),
      new Query(typePath, Q.iLike(displayField, "")));

    final Document document = this.searchField.getDocument();
    document.addDocumentListener(this);
    this.searchField.addFocusListener(new WeakFocusListener(this));
    this.searchField.addKeyListener(this);
    this.searchField.addMouseListener(this);

    this.menu.setLayout(new BorderLayout(2, 2));
    this.oldValueItem = new JLabel();
    this.oldValueItem.addMouseListener(this);
    this.oldValueItem.setForeground(new Color(128, 128, 128));
    this.oldValueItem.setFont(SwingUtil.FONT);
    this.oldValueItem.setHorizontalAlignment(SwingConstants.LEFT);
    this.menu.add(this.oldValueItem, BorderLayout.NORTH);

    this.list = new JXList(this.listModel);
    this.list.setCellRenderer(new RecordListCellRenderer(displayField.getName()));
    this.list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.list.setHighlighters(HighlighterFactory.createSimpleStriping(Color.LIGHT_GRAY));
    this.list.addMouseListener(this);
    this.listModel.addListDataListener(this);
    this.list.addListSelectionListener(this);
    this.list.addHighlighter(new ColorHighlighter(this, WebColors.Blue, WebColors.White));

    this.menu.add(new JScrollPane(this.list), BorderLayout.CENTER);
    this.menu.setFocusable(false);
    this.menu.setBorderPainted(true);
    this.menu
      .setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.DARK_GRAY),
        BorderFactory.createEmptyBorder(1, 2, 1, 2)));

    this.searchField.setEditable(true);
    MenuFactory.getPopupMenuFactory(this.searchField);
    this.searchField.setPreferredSize(new Dimension(100, 20));
    add(this.searchField);
    this.busyLabel.setVisible(false);
    add(this.busyLabel);
    GroupLayouts.makeColumns(this, false);
  }

  @Override
  public void addActionListener(final ActionListener listener) {
    this.searchField.addActionListener(listener);
  }

  @Override
  public void changedUpdate(final DocumentEvent e) {
    search();
  }

  private void clear() {
    setSelectedRecord(null);
    this.listModel.clear();
    this.menu.setVisible(false);
  }

  @Override
  public void contentsChanged(final ListDataEvent e) {
    intervalAdded(e);
  }

  public BaseCloseable eventsDisabled() {
    return this.eventsEnabled.closeable(false);
  }

  @Override
  public void firePropertyChange(final String propertyName, final Object oldValue,
    final Object newValue) {
    super.firePropertyChange(propertyName, oldValue, newValue);
  }

  @Override
  public void focusGained(final FocusEvent e) {
    final Object source = e.getSource();
    if (!(source instanceof JComponent) && ((JComponent)source).getParent() != this) {
      showMenu();
    }
  }

  @Override
  public void focusLost(final FocusEvent e) {
    final Component oppositeComponent = e.getOppositeComponent();
    if (oppositeComponent != this.list) {
      this.menu.setVisible(false);
    }
  }

  protected CodeTable getCodeTable() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final String fieldName = getFieldName();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getCodeTableByFieldName(fieldName);
    }
  }

  public String getDisplayFieldName() {
    return this.displayField.getName();
  }

  protected String getDisplayText(final Identifier identifier) {
    if (identifier == null || this.idToDisplayMap == null) {
      return "";
    } else {
      final CodeTable codeTable = getCodeTable();
      if (codeTable == null) {
        String displayText = this.idToDisplayMap.get(identifier);
        if (displayText == null) {
          displayText = identifier.toString();
          try {
            final Record record = getRecord(identifier);
            if (record != null) {
              displayText = record.getString(this.displayField);
            }
          } catch (final Exception e) {
          }
        }
        return displayText;
      } else {
        return codeTable.getValue(identifier);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getFieldValue() {
    if (this.selectedRecord == null) {
      return (T)super.getFieldValue();
    } else {
      return (T)this.selectedRecord.getIdentifier();
    }
  }

  protected abstract Record getRecord(final Identifier identifier);

  public abstract RecordDefinition getRecordDefinition();

  protected abstract List<Record> getRecords(Query query);

  public TextField getSearchField() {
    return this.searchField;
  }

  public String getSearchText() {
    return this.searchField.getText();
  }

  public Record getSelectedRecord() {
    return this.selectedRecord;
  }

  public PathName getTypePath() {
    return this.typePath;
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
  public boolean isHighlighted(final Component renderer, final ComponentAdapter adapter) {
    final Record object = this.listModel.getElementAt(adapter.row);
    final String text = this.searchField.getText();
    final String value = object.getString(this.displayField);
    if (DataType.equal(text, value)) {
      return true;
    } else {
      return false;
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
        if (selectedIndex > 0 && selectedIndex < this.listModel.getSize()) {
          this.list.setSelectedIndex(selectedIndex);
          setSelectedRecord(this.listModel.getElementAt(selectedIndex));
          e.consume();
        }
      break;
      case KeyEvent.VK_ENTER:
        // if (size > 0) {
        // if (selectedIndex >= 0 && selectedIndex < size) {
        // final Record selectedRecord =
        // this.listModel.getElementAt(selectedIndex);
        // final String text = selectedRecord.getString(this.displayFieldName);
        // if (!text.equals(this.getText())) {
        // this.selectedItem = selectedRecord;
        // setText(text);
        // }
        // } else {
        // setText("");
        // }
        // }
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

  protected void queryDo(final int searchIndex, final String queryText) {
    if (searchIndex == this.searchIndex.get()) {
      Record selectedRecord = null;
      final Map<String, Record> allRecords = new TreeMap<>();
      for (Query query : this.queries) {
        if (allRecords.size() < this.maxResults) {
          query = query.clone()//
            .addOrderBy(this.displayField);
          final Condition whereCondition = query.getWhereCondition();
          if (whereCondition instanceof BinaryCondition) {
            final BinaryCondition binaryCondition = (BinaryCondition)whereCondition;
            if (binaryCondition.getOperator().equalsIgnoreCase("like")) {
              final String likeString = "%" + queryText.toUpperCase().replaceAll("[^A-Z0-9 ]", "%")
                + "%";
              Q.setValue(0, binaryCondition, likeString);
            } else {
              Q.setValue(0, binaryCondition, queryText);
            }
          }
          query.setLimit(this.maxResults);
          final List<Record> records = getRecords(query);
          for (final Record record : records) {
            if (allRecords.size() < this.maxResults) {
              final String key = record.getString(this.displayField);
              if (!allRecords.containsKey(key)) {
                if (queryText.equals(key)) {
                  selectedRecord = record;
                }
                allRecords.put(key, record);
              }
            }
          }
        }
      }
      setSearchResults(searchIndex, allRecords.values(), selectedRecord);
    }
  }

  @Override
  public void removeActionListener(final ActionListener listener) {
    this.searchField.removeActionListener(listener);
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    this.menu.setVisible(false);
  }

  @Override
  public void removeUpdate(final DocumentEvent e) {
    search();
  }

  protected void search() {
    if (this.eventsEnabled.isTrue()) {
      final String queryText = this.searchField.getText();
      if (Property.hasValue(queryText)) {
        if (queryText.length() >= this.minSearchCharacters) {
          this.searchField.setFieldValid();
          this.busyLabel.setBusy(true);
          this.busyLabel.setVisible(true);
          final int searchIndex = this.searchIndex.incrementAndGet();
          Invoke.background("search", () -> queryDo(searchIndex, queryText));
        } else {
          clear();
          this.searchField.setFieldInvalid(
            "Minimum " + this.minSearchCharacters + " characters required for search",
            WebColors.Red, WebColors.Pink);
        }
      } else {
        this.listModel.clear();
        this.menu.setVisible(false);
        setSelectedRecord(null);
        this.searchField.setFieldValid();
      }
    }
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    setToolTipText(toolTip);
  }

  @Override
  public boolean setFieldValue(final Object value) {
    final Identifier identifier = Identifier.newIdentifier(value);
    final String displayText = getDisplayText(identifier);
    super.setFieldValue(identifier);
    if (this.searchField != null) {
      if (value == null) {
        try (
          BaseCloseable eventsDisabled = this.eventsDisabled()) {
          this.searchField.setFieldValue("");
          clear();
        }
      } else {
        this.searchField.setFieldValue(displayText);
      }
    }

    this.originalValue = identifier;
    Icon icon;
    String originalText;
    if (value == null) {
      originalText = "-";
      icon = null;
    } else {
      originalText = getDisplayText(this.originalValue);
      icon = ICON_DELETE;
    }
    if (this.oldValueItem != null) {
      this.oldValueItem.setIcon(icon);
      this.oldValueItem.setText(originalText);
    }
    return true;
  }

  public void setMaxResults(final int maxResults) {
    this.maxResults = maxResults;
  }

  public void setMinSearchCharacters(final int minSearchCharacters) {
    this.minSearchCharacters = minSearchCharacters;
  }

  public void setSearchFieldBorder(final Border border) {
    this.searchField.setBorder(border);
  }

  protected void setSearchResults(final int searchIndex, final Collection<Record> records,
    final Record selectedRecord) {

    Invoke.later(() -> {
      if (searchIndex == this.searchIndex.get()) {
        this.busyLabel.setBusy(false);
        this.busyLabel.setVisible(false);
        this.listModel.setAll(records);
        if (isShowing()) {
          showMenu();
          setSelectedRecord(selectedRecord);
        }
      }
    });
  }

  private void setSelectedRecord(final Record selectedRecord) {
    final Record oldSelectedRecord = this.selectedRecord;
    if (!DataType.equal(selectedRecord, oldSelectedRecord)) {
      this.selectedRecord = selectedRecord;
      firePropertyChange("selectedRecord", oldSelectedRecord, selectedRecord);
    }
  }

  private void showMenu() {
    final List<Record> records = this.listModel;
    if (records.isEmpty()) {
      this.menu.setVisible(false);
    } else {
      this.menu.setVisible(true);
      int x;
      int y;
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
      this.menu.show(this, x, y);
      this.menu.pack();
    }
  }

  @Override
  public void updateFieldValue() {
  }

  @Override
  public void valueChanged(final ListSelectionEvent e) {
    if (!e.getValueIsAdjusting() && this.eventsEnabled.isTrue()) {
      try (
        final BaseCloseable eventsEnabled = eventsDisabled()) {
        final Record record = (Record)this.list.getSelectedValue();
        if (record != null) {
          setSelectedRecord(record);
          final Identifier identifier = record.getIdentifier();
          final String label = record.getString(this.displayField);
          this.idToDisplayMap.put(identifier, label);
          if (!DataType.equal(label, this.searchField.getText())) {
            this.searchField.setFieldValue(label);
          }
          super.setFieldValue(identifier);
        }
        this.menu.setVisible(false);
        this.searchField.requestFocus();
      }
    }
  }
}
