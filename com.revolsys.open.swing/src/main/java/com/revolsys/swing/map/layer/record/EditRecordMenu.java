package com.revolsys.swing.map.layer.record;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.Icon;
import javax.swing.JMenu;

import org.jeometry.common.function.Consumer3;

import com.revolsys.record.Record;
import com.revolsys.swing.Icons;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.menu.BaseJPopupMenu;
import com.revolsys.swing.menu.MenuFactory;

public class EditRecordMenu extends MenuFactory {

  public static EditRecordMenu newSelectedRecords() {

    final Supplier<AbstractRecordLayer> layerSupplier = () -> {
      final Object menuSource = MenuFactory.getMenuSource();
      if (menuSource instanceof AbstractRecordLayer) {
        return (AbstractRecordLayer)menuSource;
      } else {
        return null;
      }
    };
    final Function<AbstractRecordLayer, Integer> recordCountFunction = AbstractRecordLayer::getSelectedRecordsCount;
    final Function<AbstractRecordLayer, Consumer<Consumer<LayerRecord>>> forEachRecordFunction = (
      layer) -> (action) -> layer.forEachSelectedRecord(action);
    return new EditRecordMenu("Selected Records", layerSupplier, recordCountFunction,
      forEachRecordFunction);
  }

  public static EditRecordMenu newSingleRecord() {
    final Supplier<AbstractRecordLayer> layerSupplier = () -> {
      final LayerRecord record = LayerRecordMenu.getEventRecord();
      if (record == null) {
        return null;
      } else {
        return record.getLayer();
      }
    };
    final Function<AbstractRecordLayer, Integer> recordCountFunction = (layer) -> {
      final LayerRecord record = LayerRecordMenu.getEventRecord();
      if (record == null) {
        return 0;
      } else {
        return 1;
      }
    };
    final Function<AbstractRecordLayer, Consumer<Consumer<LayerRecord>>> forEachRecordFunction = (
      layer) -> (action) -> {
        final LayerRecord record = LayerRecordMenu.getEventRecord();
        if (record != null) {
          action.accept(record);
        }
      };
    final EditRecordMenu editRecordMenu = new EditRecordMenu("Record Operations", layerSupplier,
      recordCountFunction, forEachRecordFunction);
    return editRecordMenu;
  }

  private AbstractRecordLayer layer;

  private final Supplier<AbstractRecordLayer> layerSupplier;

  private final Function<AbstractRecordLayer, Integer> recordCountFunction;

  private final Function<AbstractRecordLayer, Consumer<Consumer<LayerRecord>>> forEachRecordFunction;

  private int recordCount;

  public EditRecordMenu(final String name, final Supplier<AbstractRecordLayer> layerSupplier,
    final Function<AbstractRecordLayer, Integer> recordCountFunction,
    final Function<AbstractRecordLayer, Consumer<Consumer<LayerRecord>>> forEachRecordFunction) {
    setName(name);
    setIconName("table:go");
    this.layerSupplier = layerSupplier;
    this.recordCountFunction = recordCountFunction;
    this.forEachRecordFunction = forEachRecordFunction;
  }

  public <R extends Record> RunnableAction addMenuItemRecord(final String groupName,
    final CharSequence name, final String iconName, final Consumer<R> consumer) {
    return addMenuItemRecord(groupName, -1, name, null, iconName, null, consumer);
  }

  public <R extends Record> RunnableAction addMenuItemRecord(final String groupName,
    final CharSequence name, final String iconName, final EnableCheck enableCheck,
    final Consumer<R> action) {
    return addMenuItemRecord(groupName, -1, name, null, iconName, enableCheck, action);
  }

  public <R extends Record> RunnableAction addMenuItemRecord(final String groupName,
    final int index, final CharSequence name, final String toolTip, final String iconName,
    final EnableCheck enableCheck, final Consumer<R> action) {
    @SuppressWarnings("unchecked")
    final Consumer3<AbstractRecordLayer, Integer, Consumer<Consumer<LayerRecord>>> recordsAction = (Consumer3<AbstractRecordLayer, Integer, Consumer<Consumer<LayerRecord>>>)(
      layer, recordCount, forEachRecord) -> layer.processTasks(name, recordCount, forEachRecord,
        (Consumer<LayerRecord>)action);
    return addMenuItemRecords(groupName, index, name, toolTip, iconName, enableCheck,
      recordsAction);
  }

  public <L extends AbstractRecordLayer, R extends LayerRecord> RunnableAction addMenuItemRecords(
    final String groupName, final CharSequence name, final String iconName,
    final Consumer3<L, Integer, Consumer<Consumer<R>>> recordsAction) {
    return addMenuItemRecords(groupName, -1, name, null, iconName, null, recordsAction);
  }

  public <L extends AbstractRecordLayer, R extends LayerRecord> RunnableAction addMenuItemRecords(
    final String groupName, final CharSequence name, final String iconName,
    final EnableCheck enableCheck,
    final Consumer3<L, Integer, Consumer<Consumer<R>>> recordsAction) {
    return addMenuItemRecords(groupName, -1, name, null, iconName, enableCheck, recordsAction);
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public <L extends AbstractRecordLayer, R extends LayerRecord> RunnableAction addMenuItemRecords(
    final String groupName, final int index, final CharSequence name, final String toolTip,
    final String iconName, final EnableCheck enableCheck,
    final Consumer3<L, Integer, Consumer<Consumer<R>>> recordsAction) {
    final Icon icon = Icons.getIcon(iconName);
    // Cache the values of these two fields at the time the menu was created
    final RunnableAction action = MenuFactory.newMenuItem(name, toolTip, icon, enableCheck, () -> {
      final L layer = (L)this.layer;
      final int recordCount = getRecordCount();
      if (layer != null && recordCount > 0) {
        final Consumer<Consumer<R>> forEachRecord = (Consumer)this.forEachRecordFunction
          .apply(layer);
        recordsAction.accept(layer, recordCount, forEachRecord);
      }
    });
    addComponentFactory(groupName, index, action);
    return action;
  }

  @Override
  public MenuFactory clone() {
    final String name = getName();
    return new EditRecordMenu(name, this.layerSupplier, this.recordCountFunction,
      this.forEachRecordFunction);
  }

  public int getRecordCount() {
    return this.recordCount;
  }

  private void initMenu() {
    clear();
    EnableCheck enableCheck = EnableCheck.DISABLED;
    this.layer = this.layerSupplier.get();
    this.recordCount = 0;
    if (this.layer != null && !this.layer.isDeleted()) {
      this.recordCount = this.recordCountFunction.apply(this.layer);
      if (this.recordCount > 0) {
        enableCheck = EnableCheck.ENABLED;
        this.layer.initEditRecordsMenu(this);
      }
    }
    setEnableCheck(enableCheck);
  }

  @Override
  public JMenu newComponent() {
    initMenu();
    return super.newComponent();
  }

  @Override
  public BaseJPopupMenu newJPopupMenu(final boolean forceEnable) {
    initMenu();
    return super.newJPopupMenu(forceEnable);
  }
}
