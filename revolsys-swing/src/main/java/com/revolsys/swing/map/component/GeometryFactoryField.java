package com.revolsys.swing.map.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.VerticalLayout;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.coordinatesystem.model.systems.EpsgId;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.ArrayListComboBoxModel;
import com.revolsys.swing.field.BaseComboBox;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.parallel.Invoke;

public class GeometryFactoryField extends BaseComboBox<Integer> implements ItemListener {
  private static final long serialVersionUID = 1L;

  public static String formatGeometryFactory(final Integer value) {
    final GeometryFactory geometryFactory = getGeometryFactory(value);
    if (geometryFactory == null) {
      return DataTypes.toString(value);
    } else {
      return geometryFactory.getCoordinateSystemId() + " "
        + geometryFactory.getCoordinateSystemName();
    }
  }

  public static String formatGeometryFactory(final Object object) {
    if (object instanceof GeometryFactory) {
      final GeometryFactory geometryFactory = (GeometryFactory)object;
      final int id = geometryFactory.getCoordinateSystemId();
      final String name = geometryFactory.getCoordinateSystemName();
      if (id > 0) {
        return id + " " + name;
      } else {
        return name;
      }
    } else {
      return "-";
    }
  }

  public static GeometryFactory getGeometryFactory(final Object value) {
    GeometryFactory geometryFactory = null;
    if (value instanceof GeometryFactory) {
      geometryFactory = (GeometryFactory)value;
    } else if (value instanceof Integer) {
      geometryFactory = GeometryFactory.floating3d((Integer)value);
    } else if (value != null) {
      try {
        final int coordinateSystemId = Integer.parseInt(DataTypes.toString(value));
        geometryFactory = GeometryFactory.floating3d(coordinateSystemId);
      } catch (final Throwable t) {
      }
    }
    return geometryFactory;
  }

  private static ArrayListComboBoxModel<Integer> newModel() {
    return new ArrayListComboBoxModel<>(3857, 3005, //
      EpsgId.nad83Utm(7), //
      EpsgId.nad83Utm(8), //
      EpsgId.nad83Utm(9), //
      EpsgId.nad83Utm(10), //
      EpsgId.nad83Utm(11), //
      EpsgId.WGS84, EpsgId.NAD83, EpsgId.NAD27);
  }

  /**
   * Prompt for a coordinate system if the geometry factory does not have a coordinate system.
   */
  public static <GFP extends GeometryFactoryProxy> void promptGeometryFactory(final String title,
    final GFP geometryFactoryProxy, final Consumer<GeometryFactory> action) {
    if (geometryFactoryProxy != null && geometryFactoryProxy.isHasHorizontalCoordinateSystem()) {
      final GeometryFactory geometryFactory = geometryFactoryProxy.getGeometryFactory();
      action.accept(geometryFactory);
    } else {
      selectGeometryFactory(title, action);
    }
  }

  public static void selectGeometryFactory(final String title,
    final Consumer<GeometryFactory> action) {
    selectGeometryFactory(title, null, action);
  }

  public static void selectGeometryFactory(final String title,
    final GeometryFactoryProxy defaultGeometryFactory, final Consumer<GeometryFactory> action) {
    selectHorizontalGeometryFactory(title, defaultGeometryFactory, coordinateSystem -> {
      final GeometryFactory geometryFactory = coordinateSystem.getGeometryFactory();
      action.accept(geometryFactory);
    });
  }

  public static void selectHorizontalGeometryFactory(final String title,
    final GeometryFactoryProxy defaultGeometryFactory, final Consumer<GeometryFactory> action) {
    Invoke.later(() -> {
      final GeometryFactoryField coordinateSystemField = new GeometryFactoryField(
        "coordinateSystem");

      final ValueField valueField = new ValueField(new BorderLayout());
      valueField.setTitle("Select Coordinate System");

      {
        final JPanel titlePanel = new JPanel(new VerticalLayout(3));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        titlePanel.add(new JLabel(title));
        valueField.add(titlePanel, BorderLayout.NORTH);
      }

      {
        final Project project = Project.get();
        final MapPanel mapPanel = project.getMapPanel();
        int coordinateSystemId = -1;
        if (defaultGeometryFactory != null) {
          coordinateSystemId = defaultGeometryFactory.getCoordinateSystemId();
        }
        if (coordinateSystemId <= 0) {
          coordinateSystemId = mapPanel.getCoordinateSystemId();
        }
        if (coordinateSystemId > 0) {
          coordinateSystemField.setSelectedItem(coordinateSystemId);
        }
        final JPanel fieldPanel = new BasePanel(new JLabel("Coordinate System"),
          coordinateSystemField);
        fieldPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 5));
        GroupLayouts.makeColumns(fieldPanel, 2, true);

        valueField.add(fieldPanel, BorderLayout.CENTER);
      }

      valueField.showDialog();
      if (valueField.isSaved()) {
        final GeometryFactory geometryFactory = coordinateSystemField.getGeometryFactory();
        if (geometryFactory != null) {
          action.accept(geometryFactory);
        }
      }
    });
  }

  public GeometryFactoryField(final String fieldName) {
    super("fieldName", newModel(), GeometryFactoryField::formatGeometryFactory);

    setEditable(false);
    addItemListener(this);
    final Dimension size = new Dimension(240, 22);
    setMaximumSize(size);
    setToolTipText("Coordinate System");
  }

  public void addGeometryFactory(final GeometryFactory geometryFactory) {
    final int srid = geometryFactory.getCoordinateSystemId();
    addGeometryFactory(srid);
  }

  public void addGeometryFactory(final int srid) {
    final ArrayListComboBoxModel<Integer> model = (ArrayListComboBoxModel<Integer>)getModel();
    if (model.indexOf(srid) == -1) {
      model.addElement(srid);
    }
  }

  protected void geometryFactoryChanged(final GeometryFactory geometryFactory) {
  }

  public GeometryFactory getGeometryFactory() {
    final Integer coordinateSystemId = getSelectedItem();
    return GeometryFactory.floating2d(coordinateSystemId);
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    final int stateChange = e.getStateChange();
    if (stateChange == ItemEvent.SELECTED) {
      final Object value = e.getItem();
      final GeometryFactory coordinateSystem = getGeometryFactory(value);
      if (coordinateSystem != null) {
        final GeometryFactory geometryFactory = coordinateSystem.getGeometryFactory();
        geometryFactoryChanged(geometryFactory);
      }
    }
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
  }

  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    final int srid = geometryFactory.getHorizontalCoordinateSystemId();
    setSelectedItem(srid);
  }
}
