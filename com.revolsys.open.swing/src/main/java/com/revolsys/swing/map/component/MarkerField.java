package com.revolsys.swing.map.component;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.swing.component.ShapeIcon;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.map.layer.record.style.marker.Marker;
import com.revolsys.swing.map.layer.record.style.marker.ShapeMarker;
import com.revolsys.util.Property;

public class MarkerField extends ValueField implements PropertyChangeListener {

  private static final long serialVersionUID = 1L;

  private final ComboBox comboBox = new ComboBox("marker", new DefaultComboBoxModel(
    new Vector<Marker>(ShapeMarker.getMarkers())), null, new DefaultListCellRenderer() {
    private static final long serialVersionUID = 1L;

    @Override
    public Component getListCellRendererComponent(final JList list, final Object value,
      final int index, final boolean isSelected, final boolean cellHasFocus) {
      final Component component = super.getListCellRendererComponent(list, value, index,
        isSelected, cellHasFocus);
      setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
      final ShapeMarker marker = (ShapeMarker)value;
      setIcon(new ShapeIcon(marker.getShape(), 16, 16));
      return component;
    }
  });

  public MarkerField(final String fieldName, final Object fieldValue) {
    super(fieldName, fieldValue);
    setLayout(new VerticalLayout());
    add(this.comboBox);
    Property.addListener(this.comboBox, "marker", this);
    this.comboBox.setSelectedItem(fieldValue);
  }

  public Marker getMarker() {
    return (Marker)getFieldValue();
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (event.getSource() == this.comboBox) {
      final Object selectedItem = this.comboBox.getSelectedItem();
      setFieldValue(selectedItem);
    }
  }
}
