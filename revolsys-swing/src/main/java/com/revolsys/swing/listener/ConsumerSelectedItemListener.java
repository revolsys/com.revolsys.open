package com.revolsys.swing.listener;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.function.Consumer;

import javax.swing.JComboBox;

import com.revolsys.swing.parallel.Invoke;

public class ConsumerSelectedItemListener<T> implements ItemListener {
  public static <V> void addItemListener(final JComboBox<V> comboBox, final Consumer<V> consumer) {
    final ItemListener listener = new ConsumerSelectedItemListener<>(consumer);
    comboBox.addItemListener(listener);
  }

  private final boolean invokeLater;

  private final Consumer<T> consumer;

  public ConsumerSelectedItemListener(final boolean invokeLater, final Consumer<T> consumer) {
    this.consumer = consumer;
    this.invokeLater = invokeLater;
  }

  public ConsumerSelectedItemListener(final Consumer<T> consumer) {
    this(true, consumer);
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      @SuppressWarnings("unchecked")
      final T item = (T)e.getItem();
      if (this.invokeLater) {
        Invoke.later(() -> this.consumer.accept(item));
      } else {
        this.consumer.accept(item);
      }
    }
  }
}
