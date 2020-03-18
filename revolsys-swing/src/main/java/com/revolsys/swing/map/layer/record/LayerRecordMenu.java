package com.revolsys.swing.map.layer.record;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.Icon;
import javax.swing.JPopupMenu;

import org.jeometry.common.logging.Logs;

import com.revolsys.collection.EmptyReference;
import com.revolsys.record.Record;
import com.revolsys.swing.Icons;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.menu.BaseJPopupMenu;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeNodes;

public class LayerRecordMenu extends MenuFactory {
  private static Reference<LayerRecord> eventRecord = new EmptyReference<>();

  public static <V extends Record> RunnableAction addMenuItem(final MenuFactory menu,
    final String groupName, final CharSequence name, final String iconName,
    final EnableCheck enableCheck, final Consumer<V> consumer) {
    return addMenuItem(menu, groupName, -1, name, null, iconName, enableCheck, consumer);
  }

  public static <V extends Record> RunnableAction addMenuItem(final MenuFactory menu,
    final String groupName, final int index, final CharSequence name, final String toolTip,
    final String iconName, final EnableCheck enableCheck, final Consumer<V> consumer) {
    final Icon icon = Icons.getIcon(iconName);
    final RunnableAction action = MenuFactory.newMenuItem(name, toolTip, icon, enableCheck, () -> {
      final V record = getEventRecord();
      if (record != null && consumer != null) {
        consumer.accept(record);
      }
    });
    menu.addComponentFactory(groupName, index, action);
    return action;
  }

  public static <V extends Record> RunnableAction addMenuItem(final MenuFactory menu,
    final String groupName, final int index, final CharSequence name, final String toolTip,
    final String iconName, final Predicate<V> enabledFilter, final Consumer<V> consumer) {
    final EnableCheck enableCheck = enableCheckLayerRecord(enabledFilter);
    return addMenuItem(menu, groupName, index, name, toolTip, iconName, enableCheck, consumer);
  }

  public static <V extends Record> EnableCheck enableCheckLayerRecord(final Predicate<V> filter) {
    if (filter == null) {
      return null;
    } else {
      return () -> {
        final V node = getEventRecord();
        if (node == null) {
          return false;
        } else {
          try {
            return filter.test(node);
          } catch (final Throwable e) {
            Logs.debug(TreeNodes.class, "Exception processing enable check", e);
            return false;
          }
        }
      };
    }
  }

  @SuppressWarnings("unchecked")
  public static <R extends Record> R getEventRecord() {
    return (R)eventRecord.get();
  }

  public static void setEventRecord(final LayerRecord eventRecord) {
    LayerRecordMenu.eventRecord = new WeakReference<>(eventRecord);
  }

  private final AbstractRecordLayer layer;

  public LayerRecordMenu(final AbstractRecordLayer layer) {
    super(layer.getName());
    this.layer = layer;
  }

  public <V extends Record> RunnableAction addMenuItem(final String groupName,
    final CharSequence name, final String iconName, final Consumer<V> consumer) {
    return addMenuItem(groupName, name, iconName, (Predicate<V>)null, consumer);
  }

  public <V extends Record> RunnableAction addMenuItem(final String groupName,
    final CharSequence name, final String iconName, final Predicate<V> enabledFilter,
    final Consumer<V> consumer) {
    return addMenuItem(this, groupName, -1, name, null, iconName, enabledFilter, consumer);
  }

  public <V extends Record> RunnableAction addMenuItem(final String groupName, final int index,
    final CharSequence name, final String toolTip, final String iconName,
    final Predicate<V> enabledFilter, final Consumer<V> consumer) {
    return addMenuItem(this, groupName, index, name, toolTip, iconName, enabledFilter, consumer);
  }

  public JPopupMenu showMenu(final LayerRecord record, final MouseEvent e) {
    if (record == null) {
      return null;
    } else {
      setEventRecord(record);
      return super.showMenu(this.layer, e);
    }
  }

  @Override
  public BaseJPopupMenu showMenu(final Object source, final Component component, final int x,
    final int y) {
    return super.showMenu(this.layer, component, x, y);
  }

  @Override
  public BaseJPopupMenu showMenu(final Object source, final MouseEvent e) {
    return super.showMenu(this.layer, e);
  }
}
