package com.revolsys.swing.map.component;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.jdesktop.swingx.VerticalLayout;
import org.jeometry.common.awt.WebColors;

import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.marker.Marker;
import com.revolsys.swing.map.layer.record.style.marker.MarkerLibrary;
import com.revolsys.util.Property;

public class MarkerField extends ValueField implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private static final ListCellRenderer<Marker> renderer = new ListCellRenderer<>() {
    DefaultListCellRenderer renderer = new DefaultListCellRenderer();

    private final MarkerStyle markerStyle = new MarkerStyle() //
      .setMarkerLineColor(WebColors.Black)//
      .setMarkerFill(WebColors.Aqua) //
    ;

    @Override
    public Component getListCellRendererComponent(final JList<? extends Marker> list,
      final Marker marker, final int index, final boolean isSelected, final boolean cellHasFocus) {
      final Component component = this.renderer.getListCellRendererComponent(list, marker, index,
        isSelected, cellHasFocus);
      this.renderer.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
      this.renderer.setIcon(marker.newIcon(this.markerStyle));
      return component;
    }
  };

  private final ComboBox<Marker> comboBox = ComboBox.newComboBox("marker",
    MarkerLibrary.getAllMarkers(), renderer);

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
