package com.revolsys.swing.map.layer.record.table.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SortOrder;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.collection.CollectionUtil;
import com.revolsys.collection.list.Lists;
import com.revolsys.datatype.DataType;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.record.Record;
import com.revolsys.record.Records;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Query;
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
import com.revolsys.swing.map.layer.record.table.predicate.DeletedPredicate;
import com.revolsys.swing.map.layer.record.table.predicate.ModifiedPredicate;
import com.revolsys.swing.map.layer.record.table.predicate.NewPredicate;
import com.revolsys.swing.menu.BaseJPopupMenu;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.SortableTableModel;
import com.revolsys.swing.table.record.RecordRowTable;
import com.revolsys.swing.table.record.filter.RecordRowPredicateRowFilter;
import com.revolsys.swing.table.record.model.RecordRowTableModel;
import com.revolsys.util.Property;

public class RecordLayerTableModel extends RecordRowTableModel
  implements SortableTableModel, PropertyChangeSupportProxy {
  public static final String MODE_RECORDS_ALL = "all";

  public static final String MODE_RECORDS_CHANGED = "edits";

  public static final String MODE_RECORDS_SELECTED = "selected";

  private static final long serialVersionUID = 1L;

  public static RecordLayerTable newTable(final AbstractRecordLayer layer) {
    final RecordDefinition recordDefinition = layer.getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      final RecordLayerTableModel model = new RecordLayerTableModel(layer);
      final RecordLayerTable table = new RecordLayerTable(model);

      ModifiedPredicate.add(table);
      NewPredicate.add(table);
      DeletedPredicate.add(table);

      model.selectionChangedListener = EventQueue.addPropertyChange(layer, "hasSelectedRecords",
        () -> selectionChanged(table, model));
      return table;
    }
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

  private Map<String, Boolean> orderBy;

  private Comparator<Record> orderByComparatorIdentifier = null;

  private RowFilter<? extends RecordRowTableModel, Integer> rowFilterCondition = null;

  private EventQueueRunnableListener selectionChangedListener;

  private final Object sync = new Object();

  public RecordLayerTableModel(final AbstractRecordLayer layer) {
    super(layer.getRecordDefinition());
    this.layer = layer;
    setFieldNames(layer.getFieldNamesSet());
    setEditable(true);
    setReadOnlyFieldNames(layer.getUserReadOnlyFieldNames());

    addFieldFilterMode(new ModeAllPaged(this));
    addFieldFilterMode(new ModeChanged(this));
    addFieldFilterMode(new ModeSelected(this));
  }

  protected void addFieldFilterMode(final TableRecordsMode tableRecordsMode) {
    final String key = tableRecordsMode.getKey();
    this.tableRecordsModeByKey.put(key, tableRecordsMode);
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

  public void exportRecords(final Object target) {
    final TableRecordsMode tableRecordsMode = getTableRecordsMode();
    if (tableRecordsMode != null) {
      final Query query = getFilterQuery();
      tableRecordsMode.exportRecords(query, target);
    }
  }

  public List<TableRecordsMode> getFieldFilterModes() {
    return Lists.array(this.tableRecordsModeByKey.values());
  }

  public Condition getFilter() {
    return this.filter;
  }

  public LinkedList<Condition> getFilterHistory() {
    return this.filterHistory;
  }

  protected Query getFilterQuery() {
    final Query query = this.layer.getQuery();
    final Condition filter = getFilter();
    query.and(filter);
    query.setOrderBy(this.orderBy);
    if (this.filterByBoundingBox) {
      final Project project = this.layer.getProject();
      final BoundingBox viewBoundingBox = project.getViewBoundingBox();
      final RecordDefinition recordDefinition = this.layer.getRecordDefinition();
      final FieldDefinition geometryField = recordDefinition.getGeometryField();
      if (geometryField != null) {
        query.and(F.envelopeIntersects(geometryField, viewBoundingBox));
      }
    }
    return query;
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
        final LayerRecordMenu menu = record.getMenu();

        final BaseJPopupMenu popupMenu = menu.newJPopupMenu();
        popupMenu.addSeparator();
        final RecordLayerTable table = getTable();
        final boolean editingCurrentCell = isCellEditable(rowIndex, columnIndex);
        if (editingCurrentCell) {
          popupMenu.add(RunnableAction.newMenuItem("Cut Field Value", "cut", table::cutFieldValue));
        }
        popupMenu
          .add(RunnableAction.newMenuItem("Copy Field Value", "page_copy", table::copyFieldValue));
        if (editingCurrentCell) {
          popupMenu.add(
            RunnableAction.newMenuItem("Paste Field Value", "paste_plain", table::pasteFieldValue));
        }
        return popupMenu;
      }
    }
    return null;
  }

  public Map<String, Boolean> getOrderBy() {
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

  public RowFilter<? extends RecordRowTableModel, Integer> getRowFilter() {
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
    if (this.tableRecordsMode == null || !this.tableRecordsMode.isEnabled()) {
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

  public boolean isFilterByBoundingBox() {
    return this.filterByBoundingBox;
  }

  public boolean isHasFilter() {
    return this.filter != null;
  }

  @Override
  public boolean isSelected(final boolean selected, final int rowIndex, final int columnIndex) {
    final LayerRecord object = getRecord(rowIndex);
    return this.layer.isSelected(object);
  }

  public boolean isSortable() {
    final TableRecordsMode tableRecordsMode = getTableRecordsMode();
    if (tableRecordsMode == null) {
      return false;
    } else {
      return tableRecordsMode.isSortable();
    }
  }

  public void refresh() {
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
    final List<String> fieldTitles = new ArrayList<>();
    for (final String fieldName : fieldNames) {
      final String fieldTitle = this.layer.getFieldTitle(fieldName);
      fieldTitles.add(fieldTitle);
    }
    super.setFieldNamesAndTitles(fieldNames, fieldTitles);
  }

  public void setFieldNamesSetName(final String fieldNamesSetName) {
    this.layer.setFieldNamesSetName(fieldNamesSetName);
    final List<String> fieldNamesSet = this.layer.getFieldNamesSet();
    setFieldNames(fieldNamesSet);
  }

  public boolean setFilter(Condition filter) {
    if (filter == null) {
      filter = Condition.ALL;
    }
    if (DataType.equal(filter, this.filter)) {
      return false;
    } else {
      final Object oldValue = this.filter;
      this.filter = filter;
      if (filter.isEmpty()) {
        this.rowFilterCondition = null;
      } else {
        this.rowFilterCondition = new RecordRowPredicateRowFilter(filter);
        if (!DataType.equal(oldValue, filter)) {
          this.filterHistory.remove(filter);
          this.filterHistory.addFirst(filter);
          while (this.filterHistory.size() > 20) {
            this.filterHistory.removeLast();
          }
        }
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
      return true;
    }
  }

  public void setFilterByBoundingBox(final boolean filterByBoundingBox) {
    if (this.filterByBoundingBox != filterByBoundingBox) {
      this.filterByBoundingBox = filterByBoundingBox;
      refresh();
    }
  }

  public void setGeometryFilterMode(final String mode) {
    setFilterByBoundingBox("boundingBox".equals(mode));
  }

  public void setOrderBy(final Map<String, Boolean> orderBy) {
    setOrderByInternal(orderBy);
    final Map<Integer, SortOrder> sortedColumns = new LinkedHashMap<>();
    for (final Entry<String, Boolean> entry : orderBy.entrySet()) {
      if (orderBy != null) {
        final String fieldName = entry.getKey();
        final Boolean order = entry.getValue();
        final int index = getFieldIndex(fieldName);
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

  private void setOrderByInternal(final Map<String, Boolean> orderBy) {
    if (Property.hasValue(orderBy)) {
      this.orderBy = orderBy;
      this.orderByComparatorIdentifier = Records.newComparatorOrderByIdentifier(orderBy);
    } else {
      this.orderBy = Collections.emptyMap();
      this.orderByComparatorIdentifier = null;
    }
  }

  @Override
  public SortOrder setSortOrder(final int column) {
    final SortOrder sortOrder = super.setSortOrder(column);
    final String fieldName = getFieldName(column);

    Map<String, Boolean> orderBy;
    if (sortOrder == SortOrder.ASCENDING) {
      orderBy = Collections.singletonMap(fieldName, true);
    } else if (sortOrder == SortOrder.DESCENDING) {
      orderBy = Collections.singletonMap(fieldName, false);
    } else {
      orderBy = Collections.singletonMap(fieldName, true);
    }
    if (this.sync == null) {
      setOrderByInternal(orderBy);
    } else {
      setOrderByInternal(orderBy);
      refresh();
    }
    return sortOrder;
  }

  @Override
  public void setTable(final RecordRowTable table) {
    super.setTable(table);
    table.setSelectionModel(getSelectionModel());
  }

  public void setTableRecordsMode(final TableRecordsMode tableRecordsMode) {
    Invoke.later(() -> {
      final TableRecordsMode oldMode = this.tableRecordsMode;
      final RecordLayerTable table = getTable();
      if (table != null && tableRecordsMode != null && tableRecordsMode != oldMode) {
        if (oldMode != null) {
          oldMode.deactivate();
        }
        this.tableRecordsMode = tableRecordsMode;
        this.tableRecordsMode.activate();
        final ListSelectionModel selectionModel = this.tableRecordsMode.getSelectionModel();
        table.setSelectionModel(selectionModel);
        table.setRowFilter(null);
        refresh();
        firePropertyChange("tableRecordsMode", oldMode, this.tableRecordsMode);
      }
    });
  }

  @Override
  public String toDisplayValueInternal(final int rowIndex, final int fieldIndex,
    final Object objectValue) {
    if (objectValue == null) {
      final String fieldName = getFieldName(fieldIndex);
      if (getRecordDefinition().getIdFieldNames().contains(fieldName)) {
        return "NEW";
      }
    }
    return super.toDisplayValueInternal(rowIndex, fieldIndex, objectValue);
  }
}
