package com.revolsys.swing.map.layer.menu;

import java.awt.Component;
import java.awt.Dimension;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.SwingConstants;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.component.ComponentFactory;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.component.MapScale;
import com.revolsys.swing.menu.MenuFactory;

public class TreeItemScaleMenu<T> implements ComponentFactory<JMenu> {

  private final Predicate<T> enableCheck;

  private final Function<T, Long> getScaleFunction;

  private final boolean min;

  private String name;

  private final BiConsumer<T, Long> setScaleFunction;

  public TreeItemScaleMenu(final boolean min, final Predicate<T> enableCheck,
    final Function<T, Long> getScaleFunction, final BiConsumer<T, Long> setScaleFunction) {
    this.min = min;
    this.enableCheck = enableCheck;
    this.getScaleFunction = getScaleFunction;
    this.setScaleFunction = setScaleFunction;
    if (this.min) {
      this.name = "Hide zoomed out beyond (minimum) scale";
    } else {
      this.name = "Hide zoomed in beyond (maximum) scale";
    }
  }

  protected void addScaleMenuItem(final long layerScale, final JMenu menu, final T object,
    final long scale) {
    final String label;
    if (scale <= 0) {
      label = "Unlimited";
    } else {
      label = MapScale.formatScale(scale);
    }

    final RunnableAction action = new RunnableAction(label,
      () -> this.setScaleFunction.accept(object, scale));
    final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(action);
    final boolean selected = scale == layerScale;
    menuItem.setSelected(selected);
    menuItem.setPreferredSize(new Dimension(140, 22));
    menuItem.setHorizontalTextPosition(SwingConstants.RIGHT);
    menu.add(menuItem);
  }

  @SuppressWarnings("unchecked")
  @Override
  public TreeItemScaleMenu<T> clone() {
    try {
      return (TreeItemScaleMenu<T>)super.clone();
    } catch (final CloneNotSupportedException e) {
      return Exceptions.throwUncheckedException(e);
    }
  }

  @Override
  public void close(final Component component) {
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getIconName() {
    return null;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getToolTip() {
    return null;
  }

  @Override
  public JMenu newComponent() {
    final JMenu menu = new JMenu(this.name);
    final T object = MenuFactory.getMenuSource();
    if (object != null && (this.enableCheck == null || this.enableCheck.test(object))) {
      long layerScale = this.getScaleFunction.apply(object);
      if (layerScale == Long.MAX_VALUE) {
        layerScale = 0;
      }
      addScaleMenuItem(layerScale, menu, object, 0);
      boolean scaleIncluded = layerScale == 0;
      for (final long scale : Viewport2D.SCALES) {
        if (layerScale == scale) {
          scaleIncluded = true;
        } else if (!scaleIncluded) {
          if (layerScale > scale) {
            addScaleMenuItem(layerScale, menu, object, layerScale);
            scaleIncluded = true;
          }
        }
        addScaleMenuItem(layerScale, menu, object, scale);
      }
      if (!scaleIncluded) {
        addScaleMenuItem(layerScale, menu, object, layerScale);
      }
    } else {
      menu.setEnabled(false);
    }
    return menu;
  }
}
