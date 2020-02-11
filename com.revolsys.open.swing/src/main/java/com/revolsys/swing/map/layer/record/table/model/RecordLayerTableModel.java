package com.revolsys.swing.map.layer.record.table.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javax.swing.JMenuItem;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SortOrder;

import org.jeometry.common.data.type.DataType;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.collection.CollectionUtil;
import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.io.BaseCloseable;
import com.revolsys.record.Record;
import com.revolsys.record.Records;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.functions.EnvelopeIntersects;
import com.revolsys.record.query.functions.F;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.EventQueue;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.listener.EventQueueRunnableListener;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.LayerRecordMenu;
import com.revolsys.swing.map.layer.record.table.RecordLayerTable;
import com.revolsys.swing.menu.BaseJPopupMenu;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.SortableTableModel;
import com.revolsys.swing.table.editor.BaseTableCellEditor;
import com.revolsys.swing.table.record.filter.RecordRowPredicateRowFilter;
import com.revolsys.swing.table.record.model.RecordRowTableModel;
import com.revolsys.util.Cancellable;
import com.revolsys.util.Property;

public class RecordLayerTableModel extends RecordRowTableModel
  implements SortableTableModel, PropertyChangeSupportProxy {
  private static final ModeEmpty MODE_EMPTY = new ModeEmpty();

  public static final String MODE_RECORDS_ALL = "all";

  public static final String MODE_RECORDS_CHANGED = "edits";

  public static final String MODE_RECORDS_SELECTED = "selected";

  private static final long serialVersionUID = 1L;

  public static RecordLayerTable newTable(final AbstractRecordLayer layer) {
    return newTable(layer, layer.getFieldNamesSet());
  }

  public static RecordLayerTable newTable(final AbstractRecordLayer layer,
    final Collection<String> fieldNames) {
    final RecordDefinition recordDefinition = layer.getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      final RecordLayerTableModel model = new RecordLayerTableModel(layer, fieldNames);
      final RecordLayerTable table = new RecordLayerTable(model);

      model.selectionChangedListener = EventQueue.addPropertyChange(layer, "hasSelectedRecords",
        () -> selectionChanged(table, model));
      return table;
    }
  }

  public static RecordLayerTable newTable(final AbstractRecordLayer layer,
    final String... fieldNames) {
    return newTable(layer, Arrays.asList(fieldNames));
  }

  public static final void selectionChanged(final RecordLayerTable table,
    final RecordLayerTableModel tableModel) {
    table.repaint();
  }

  private TableRecordsMode tableRecordsMode;

  private final Map<String, TableRecordsMode> tableRecordsModeByKey = new LinkedHashMap<>();

  private Condition filter = Condition.ALL;

  private boolean filterByBoundingBox;

  private final LinkedList<Condition> filterHistory = new LinkedList<>();

  private final AbstractRecordLayer layer;

  private Map<? extends CharSequence, Boolean> orderBy;

  private Comparator<Record> orderByComparatorIdentifier = null;

  private RowFilter<RecordRowTableModel, Integer> rowFilterCondition = null;

  private EventQueueRunnableListener selectionChangedListener;

  private final Object sync = new Object();

  private boolean useRecordMenu = true;

  private Query filterQuery;

  public RecordLayerTableModel(final AbstractRecordLayer layer,
    final Collection<String> fieldNames) {
    super(layer.getRecordDefinition());
    this.layer = layer;
    setFieldNames(fieldNames);
    setEditable(true);
    setReadOnlyFieldNames(layer.getUserReadOnlyFieldNames());
    final String idFieldName = getRecordDefinition().getIdFieldName();
    setSortOrder(idFieldName);

    addFieldFilterMode(new ModeAllPaged(this));
    addFieldFilterMode(new ModeChanged(this));
    addFieldFilterMode(new ModeSelected(this));
  }

  protected void addFieldFilterMode(final TableRecordsMode tableRecordsMode) {
    final String key = tableRecordsMode.getKey();
    this.tableRecordsModeByKey.put(key, tableRecordsMode);
  }

  protected void addIdFieldNames(final Query query) {
    for (final String fieldName : getRecordDefinition().getIdFieldNames()) {
      query.addFieldName(fieldName);
    }
  }

  @Override
  public void dispose() {
    getTable().setSelectionModel(null);
    Property.removeListener(this.layer, "hasSelectedRecords", this.selectionChangedListener);
    final TableRecordsMode tableRecordsMode = getTableRecordsMode();
    if (tableRecordsMode != null) {
      tableRecordsMode.deactivate();
    }
    this.selectionChangedListener = null;
    super.dispose();
  }

  public void exportRecords(final Object target, final boolean tableColumnsOnly) {
    final TableRecordsMode tableRecordsMode = getTableRecordsMode();
    if (tableRecordsMode != null) {
      final Query query = getFilterQuery();
      tableRecordsMode.exportRecords(query, target, tableColumnsOnly);
    }
  }

  public void forEachColumnDisplayValue(final Cancellable cancellable, final int columnIndex,
    final Consumer<String> action) {
    forEachColumnValue(cancellable, columnIndex, value -> {
      final String displayValue = toCopyValue(0, columnIndex, value);
      if (displayValue == null) {
        action.accept("");
      } else {
        action.accept(displayValue);
      }
    });
  }

  public void forEachColumnValue(final Cancellable cancellable, final int columnIndex,
    final Consumer<Object> action) {
    final String fieldName = getColumnFieldName(columnIndex);
    final TableRecordsMode tableRecordsMode = getTableRecordsMode();
    if (tableRecordsMode != null && fieldName != null) {
      final Query query = getFilterQuery().clone();
      addIdFieldNames(query);
      query.addFieldName(fieldName);
      try (
        BaseCloseable eventsDisabled = this.layer.eventsDisabled()) {
        query.setCancellable(cancellable);
        tableRecordsMode.forEachRecord(query, record -> {
          final Object value = record.getValue(fieldName);
          action.accept(value);
        });
      }
    }
  }

  public void forEachRecord(final Consumer<? super LayerRecord> action) {
    final TableRecordsMode tableRecordsMode = getTableRecordsMode();
    if (tableRecordsMode != null) {
      final Query query = getFilterQuery();
      try (
        BaseCloseable eventsDisabled = this.layer.eventsDisabled()) {
        tableRecordsMode.forEachRecord(query, action);
      }
      refresh();
    }
  }

  @Override
  public BaseTableCellEditor getCellEditor(final int columnIndex) {
    final String fieldName = getColumnFieldName(columnIndex);
    if (fieldName != null) {
      final AbstractRecordLayer layer = getLayer();
      final RecordLayerTable table = getTable();
      final BaseTableCellEditor editor = layer.newTableCellEditor(table, fieldName);
      if (editor != null) {
        return editor;
      }
    }
    return null;
  }

  public List<TableRecordsMode> getFieldFilterModes() {
    return Lists.toArray(this.tableRecordsModeByKey.values());
  }

  public Condition getFilter() {
    return this.filter;
  }

  public LinkedList<Condition> getFilterHistory() {
    return this.filterHistory;
  }

  public synchronized Query getFilterQuery() {
    return this.filterQuery;
  }

  public String getGeometryFilterMode() {
    if (this.filterByBoundingBox) {
      return "boundingBox";
    }
    return "all";
  }

  public AbstractRecordLayer getLayer() {
    return this.layer;
  }

  @Override
  public BaseJPopupMenu getMenu(final int rowIndex, final int columnIndex) {
    final LayerRecord record = getRecord(rowIndex);
    if (record != null) {
      final AbstractRecordLayer layer = getLayer();
      if (layer != null) {
        LayerRecordMenu.setEventRecord(record);
        if (isUseRecordMenu()) {
          final LayerRecordMenu menu = record.getMenu();

          final BaseJPopupMenu popupMenu = menu.newJPopupMenu();
          popupMenu.addSeparator();
          final RecordLayerTable table = getTable();
          final boolean cellEditable = isCellEditable(rowIndex, columnIndex);

          final Object value = getValueAt(rowIndex, columnIndex);

          final boolean canCopy = Property.hasValue(value);
          if (cellEditable) {
            final JMenuItem cutMenu = RunnableAction.newMenuItem("Cut Field Value", "cut",
              table::cutFieldValue);
            cutMenu.setEnabled(canCopy);
            popupMenu.add(cutMenu);
          }

          final JMenuItem copyMenu = RunnableAction.newMenuItem("Copy Field Value", "page_copy",
            table::copyFieldValue);
          copyMenu.setEnabled(canCopy);
          popupMenu.add(copyMenu);

          if (cellEditable) {
            popupMenu.add(RunnableAction.newMenuItem("Paste Field Value", "paste_plain",
              table::pasteFieldValue));
          }
          return popupMenu;
        } else {
          return super.getMenu().newJPopupMenu();
        }
      }
    }
    return null;
  }

  public Map<? extends CharSequence, Boolean> getOrderBy() {
    return this.orderBy;
  }

  public Comparator<Record> getOrderByComparatorIdentifier() {
    return this.orderByComparatorIdentifier;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Record> V getRecord(final int rowIndex) {
    LayerRecord record = null;
    if (rowIndex >= 0) {
      final TableRecordsMode tableRecordsMode = getTableRecordsMode();
      if (tableRecordsMode != null) {
        record = tableRecordsMode.getRecord(rowIndex);
      }
    }
    return (V)record;
  }

  @Override
  public final int getRowCount() {
    final TableRecordsMode tableRecordsMode = getTableRecordsMode();
    if (tableRecordsMode == null) {
      return 0;
    } else {
      return tableRecordsMode.getRecordCount();
    }
  }

  public RowFilter<RecordRowTableModel, Integer> getRowFilter() {
    if (isSortable()) {
      return this.rowFilterCondition;
    } else {
      return null;
    }
  }

  public ListSelectionModel getSelectionModel() {
    final TableRecordsMode tableRecordsMode = getTableRecordsMode();
    if (tableRecordsMode == null) {
      return null;
    } else {
      return tableRecordsMode.getSelectionModel();
    }
  }

  @Override
  public RecordLayerTable getTable() {
    return (RecordLayerTable)super.getTable();
  }

  public TableRecordsMode getTableRecordsMode() {
    if (this.tableRecordsMode == null) {
      setTableRecordsMode(CollectionUtil.get(this.tableRecordsModeByKey.values(), 0));
    }
    return this.tableRecordsMode;
  }

  public TableRecordsMode getTableRecordsMode(final String key) {
    return this.tableRecordsModeByKey.get(key);
  }

  public String getTypeName() {
    return getRecordDefinition().getPath();
  }

  @Override
  protected boolean isCellEditable(final int rowIndex, final int columnIndex, final Record record) {
    final AbstractRecordLayer layer = getLayer();
    final LayerRecord layerRecord = (LayerRecord)record;
    if (layer.isDeleted(layerRecord)) {
      return false;
    } else if (layer.isCanEditRecords() || layer.isNew(layerRecord) && layer.isCanAddRecords()) {
      return super.isCellEditable(rowIndex, columnIndex, record);
    } else {
      return false;
    }
  }

  public boolean isDeleted(final int rowIndex) {
    final LayerRecord record = getRecord(rowIndex);
    if (record != null) {
      final AbstractRecordLayer layer = getLayer();
      if (layer != null) {
        return layer.isDeleted(record);
      }
    }
    return false;
  }

  @Override
  public boolean isEditable() {
    return super.isEditable() && this.layer.isEditable() && this.layer.isCanEditRecords();
  }

  @Override
  public boolean isFieldEditable(final int columnIndex) {
    if (this.layer.isEditable()) {
      return super.isFieldEditable(columnIndex);
    } else {
      return false;
    }
  }

  public boolean isFilterByBoundingBox() {
    return this.filterByBoundingBox;
  }

  public boolean isFilterByBoundingBoxSupported() {
    if (this.tableRecordsMode == null) {
      return true;
    }
    return this.tableRecordsMode.isFilterByBoundingBoxSupported();
  }

  public boolean isHasFilter() {
    return this.filter != null && !this.filter.isEmpty();
  }

  public boolean isHasFilterHistory() {
    return !this.filterHistory.isEmpty();
  }

  @Override
  public boolean isSelected(final boolean selected, final int rowIndex, final int columnIndex) {
    final LayerRecord record = getRecord(rowIndex);
    return this.layer.isSelected(record);
  }

  public boolean isSortable() {
    final TableRecordsMode tableRecordsMode = getTableRecordsMode();
    if (tableRecordsMode == null) {
      return false;
    } else {
      return tableRecordsMode.isSortable();
    }
  }

  public boolean isUseRecordMenu() {
    return this.useRecordMenu;
  }

  public void refresh() {
    updateFilterQuery();
    final TableRecordsMode tableRecordsMode = getTableRecordsMode();
    if (tableRecordsMode != null) {
      tableRecordsMode.refresh();
    }
  }

  protected void repaint() {
    final RecordLayerTable table = getTable();
    if (table != null) {
      table.repaint();
    }
  }

  @Override
  public void setFieldNames(final Collection<String> fieldNames) {
    super.setFieldNames(fieldNames);
    final List<String> fieldTitles = new ArrayList<>();
    for (final String fieldName : fieldNames) {
      final String fieldTitle = this.layer.getFieldTitle(fieldName);
      fieldTitles.add(fieldTitle);
    }
    setFieldTitles(fieldTitles);
  }

  public void setFieldNames(final String... fieldNames) {
    setFieldNames(Arrays.asList(fieldNames));
  }

  public void setFieldNamesSetName(final String fieldNamesSetName) {
    this.layer.setFieldNamesSetName(fieldNamesSetName);
    final List<String> fieldNamesSet = this.layer.getFieldNamesSet();
    setFieldNames(fieldNamesSet);
  }

  public void setFilter(Condition filter) {
    if (Invoke.swingThread(this::setFilter, filter)) {
      if (filter == null) {
        filter = Condition.ALL;
      }
      if (!DataType.equal(filter, this.filter)) {
        final Object oldValue = this.filter;
        this.filter = filter;
        updateFilterQuery();
        if (Property.isEmpty(filter)) {
          this.rowFilterCondition = null;
        } else {
          this.rowFilterCondition = new RecordRowPredicateRowFilter(filter);
          if (!DataType.equal(oldValue, filter)) {
            this.filterHistory.remove(filter);
            this.filterHistory.addFirst(filter);
            while (this.filterHistory.size() > 20) {
              this.filterHistory.removeLast();
            }
            firePropertyChange("hasFilterHistory", false, true);
          }
        }
        if (this.layer.isShowAllRecordsOnFilter() && !filter.isEmpty()) {
          final TableRecordsMode modeAll = getTableRecordsMode(MODE_RECORDS_ALL);
          setTableRecordsMode(modeAll);
        }

        if (isSortable()) {
          final RecordLayerTable table = getTable();
          table.setRowFilter(this.rowFilterCondition);
        } else {
          refresh();
        }
        firePropertyChange("filter", oldValue, this.filter);
        final boolean hasFilter = isHasFilter();
        firePropertyChange("hasFilter", !hasFilter, hasFilter);
      }
    }
  }

  public void setFilterByBoundingBox(boolean filterByBoundingBox) {
    final String geometryFilterMode = getGeometryFilterMode();
    final String oldValue = geometryFilterMode;
    if (!this.tableRecordsMode.isFilterByBoundingBoxSupported()) {
      filterByBoundingBox = false;
    }
    if (this.filterByBoundingBox != filterByBoundingBox) {
      this.filterByBoundingBox = filterByBoundingBox;
      refresh();
    }
    firePropertyChange("geometryFilterMode", oldValue, geometryFilterMode);
  }

  public String setGeometryFilterMode(final String mode) {
    final boolean filterByBoundingBox = "boundingBox".equals(mode);
    setFilterByBoundingBox(filterByBoundingBox);
    return getGeometryFilterMode();
  }

  public void setOrderBy(final Map<? extends CharSequence, Boolean> orderBy) {
    if (orderBy != null) {
      for (final Iterator<? extends CharSequence> iterator = orderBy.keySet().iterator(); iterator
        .hasNext();) {
        final CharSequence fieldName = iterator.next();
        if (!this.layer.hasField(fieldName)) {
          iterator.remove();
        }
      }
    }
    setOrderByInternal(orderBy);
    final Map<Integer, SortOrder> sortedColumns = new LinkedHashMap<>();
    if (orderBy != null) {
      for (final Entry<? extends CharSequence, Boolean> entry : orderBy.entrySet()) {
        final CharSequence fieldName = entry.getKey();
        final Boolean order = entry.getValue();
        final int index = getColumnFieldIndex(fieldName.toString());
        if (index != -1) {
          SortOrder sortOrder;
          if (order) {
            sortOrder = SortOrder.ASCENDING;
          } else {
            sortOrder = SortOrder.DESCENDING;
          }
          sortedColumns.put(index, sortOrder);
        }
      }
    }
    setSortedColumns(sortedColumns);
  }

  private void setOrderByInternal(final Map<? extends CharSequence, Boolean> orderBy) {
    if (Property.hasValue(orderBy)) {
      this.orderBy = orderBy;
      this.orderByComparatorIdentifier = Records.newComparatorOrderByIdentifier(orderBy);
    } else {
      this.orderBy = Collections.emptyMap();
      this.orderByComparatorIdentifier = null;
    }
  }

  @Override
  protected void setRecordValueDo(final Record record, final String fieldName, final Object value) {
    this.layer.setRecordValue((LayerRecord)record, fieldName, value);
  }

  @Override
  public SortOrder setSortOrder(final int columnIndex) {
    if (isColumnSortable(columnIndex)) {
      final SortOrder sortOrder = super.setSortOrder(columnIndex);
      if (sortOrder != SortOrder.UNSORTED) {
        final FieldDefinition fieldName = getColumnFieldDefinition(columnIndex);
        if (Property.hasValue(fieldName)) {
          Map<FieldDefinition, Boolean> orderBy = null;
          if (sortOrder == SortOrder.ASCENDING) {
            orderBy = Collections.singletonMap(fieldName, true);
          } else if (sortOrder == SortOrder.DESCENDING) {
            orderBy = Collections.singletonMap(fieldName, false);
          }
          if (this.sync == null) {
            setOrderByInternal(orderBy);
          } else {
            setOrderByInternal(orderBy);
            refresh();
          }
        }
      }
      return sortOrder;
    } else {
      return SortOrder.UNSORTED;
    }
  }

  @Override
  public SortOrder setSortOrder(final int columnIndex, final SortOrder sortOrder) {
    super.setSortOrder(columnIndex, sortOrder);
    final FieldDefinition fieldDefinition = getColumnFieldDefinition(columnIndex);
    if (fieldDefinition != null) {
      Map<FieldDefinition, Boolean> orderBy;
      if (sortOrder == SortOrder.ASCENDING) {
        orderBy = Collections.singletonMap(fieldDefinition, true);
      } else if (sortOrder == SortOrder.DESCENDING) {
        orderBy = Collections.singletonMap(fieldDefinition, false);
      } else {
        orderBy = Collections.singletonMap(fieldDefinition, true);
      }
      if (this.sync == null) {
        setOrderByInternal(orderBy);
      } else {
        setOrderByInternal(orderBy);
        refresh();
      }
    }
    return sortOrder;
  }

  @Override
  public void setTable(final BaseJTable table) {
    super.setTable(table);
    final ListSelectionModel selectionModel = getSelectionModel();
    table.setSelectionModel(selectionModel);
  }

  public void setTableRecordsMode(final TableRecordsMode tableRecordsMode) {
    if (Invoke.swingThread(this::setTableRecordsMode, tableRecordsMode)) {
      final TableRecordsMode oldMode = this.tableRecordsMode;
      final RecordLayerTable table = getTable();
      if (table != null && tableRecordsMode != null && tableRecordsMode != oldMode) {
        if (oldMode != null) {
          oldMode.deactivate();
        }
        final String oldGeometryFilterMode = getGeometryFilterMode();
        this.tableRecordsMode = MODE_EMPTY;
        fireTableDataChanged();
        table.setSortable(false);
        table.setSelectionModel(null);
        table.setRowFilter(null);

        tableRecordsMode.activate();

        final ListSelectionModel selectionModel = tableRecordsMode.getSelectionModel();
        table.setSelectionModel(selectionModel);

        final boolean sortable = tableRecordsMode.isSortable();
        table.setSortable(sortable);

        final RowFilter<RecordRowTableModel, Integer> rowFilter = getRowFilter();
        table.setRowFilter(rowFilter);

        final boolean filterByBoundingBoxSupported = tableRecordsMode
          .isFilterByBoundingBoxSupported();
        if (!filterByBoundingBoxSupported) {
          this.filterByBoundingBox = false;
        }
        this.tableRecordsMode = tableRecordsMode;

        refresh();
        firePropertyChange("tableRecordsMode", oldMode, this.tableRecordsMode);
        firePropertyChange("geometryFilterMode", oldGeometryFilterMode, getGeometryFilterMode());
        firePropertyChange("filterByBoundingBox", !this.filterByBoundingBox,
          this.filterByBoundingBox);
        firePropertyChange("filterByBoundingBoxSupported", !filterByBoundingBoxSupported,
          filterByBoundingBoxSupported);
      }
    }
  }

  public void setUseRecordMenu(final boolean useRecordMenu) {
    this.useRecordMenu = useRecordMenu;
  }

  @Override
  public String toDisplayValueInternal(final int rowIndex, final int fieldIndex,
    final Object objectValue) {
    if (objectValue == null) {
      final String fieldName = getColumnFieldName(fieldIndex);
      final RecordDefinition recordDefinition = getRecordDefinition();
      if (recordDefinition.isIdField(fieldName)) {
        return "NEW";
      }
    }
    return super.toDisplayValueInternal(rowIndex, fieldIndex, objectValue);
  }

  private synchronized void updateFilterQuery() {
    final Query oldValue = this.filterQuery;
    final AbstractRecordLayer layer = this.layer;
    final Query layerQuery = layer.getQuery();
    Query query = layerQuery;
    final Condition filter = getFilter();
    if (!filter.isEmpty()) {
      query = query //
        .clone() //
        .and(filter);
    }
    if (this.filterByBoundingBox) {
      final FieldDefinition geometryField = layer.getGeometryField();
      if (geometryField != null) {
        final Project project = layer.getProject();
        final BoundingBox viewBoundingBox = project.getViewBoundingBox();
        if (!viewBoundingBox.isEmpty()) {
          final EnvelopeIntersects envelopeIntersects = F.envelopeIntersects(geometryField,
            viewBoundingBox);
          if (query == layerQuery) {
            query = query.clone();
          }
          query.and(envelopeIntersects);
        }
      }
    }
    if (this.orderBy != null && !this.orderBy.isEmpty()) {
      if (query == layerQuery) {
        query = query.clone();
      }
      query.setOrderBy(this.orderBy);
    }

    this.filterQuery = query;
    firePropertyChange("query", oldValue, query);
  }
}
