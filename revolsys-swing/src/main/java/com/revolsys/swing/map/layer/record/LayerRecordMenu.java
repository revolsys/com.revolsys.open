package com.revolsys.swing.map.layer.record;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.Icon;

import org.jeometry.common.logging.Logs;

import com.revolsys.record.Record;
import com.revolsys.swing.Icons;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.menu.BaseJPopupMenu;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeNodes;

public class LayerRecordMenu extends MenuFactory {

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
    return addMenuItem(groupName, -1, name, null, iconName, enabledFilter, consumer);
  }

  public <V extends Record> RunnableAction addMenuItem(final String groupName, final int index,
    final CharSequence name, final String toolTip, final String iconName,
    final EnableCheck enableCheck, final Consumer<V> consumer) {
    final Icon icon = Icons.getIcon(iconName);
    final RunnableAction action = MenuFactory.newMenuItem(name, toolTip, icon, enableCheck, () -> {
      final V record = this.layer.getMenuRecord();
      if (record != null && consumer != null) {
        consumer.accept(record);
      }
    });
    addComponentFactory(groupName, index, action);
    return action;
  }

  public <V extends Record> RunnableAction addMenuItem(final String groupName, final int index,
    final CharSequence name, final String toolTip, final String iconName,
    final Predicate<V> enabledFilter, final Consumer<V> consumer) {
    final EnableCheck enableCheck = enableCheckLayerRecord(enabledFilter);
    return addMenuItem(groupName, index, name, toolTip, iconName, enableCheck, consumer);
  }

  public <V extends Record> EnableCheck enableCheckLayerRecord(final Predicate<V> filter) {
    if (filter == null) {
      return null;
    } else {
      return () -> {
        try {
          final V record = this.layer.getMenuRecord();
          if (record == null) {
            return false;
          } else {
            return filter.test(record);
          }
        } catch (final ClassCastException e) {
          return false;
        } catch (final Throwable e) {
          Logs.errorOnce(TreeNodes.class, "Exception processing enable check", e);
          return false;
        }
      };
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
