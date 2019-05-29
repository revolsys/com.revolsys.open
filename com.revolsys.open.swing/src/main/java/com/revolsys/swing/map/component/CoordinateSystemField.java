package com.revolsys.swing.map.component;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.HorizontalCoordinateSystem;
import com.revolsys.geometry.cs.HorizontalCoordinateSystemProxy;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.cs.epsg.EpsgId;
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

public class CoordinateSystemField extends BaseComboBox<Integer> implements ItemListener {
  private static final long serialVersionUID = 1L;

  public static String formatCoordinateSystem(final Integer value) {
    final CoordinateSystem coordinateSystem = getCoordinateSystem(value);
    if (coordinateSystem == null) {
      return DataTypes.toString(value);
    } else {
      return coordinateSystem.getHorizontalCoordinateSystemId() + " "
        + coordinateSystem.getCoordinateSystemName();
    }
  }

  public static String formatCoordinateSystem(final Object object) {
    if (object instanceof CoordinateSystem) {
      final CoordinateSystem coordinateSystem = (CoordinateSystem)object;
      final int id = coordinateSystem.getCoordinateSystemId();
      final String name = coordinateSystem.getCoordinateSystemName();
      if (id > 0) {
        return id + " " + name;
      } else {
        return name;
      }
    } else {
      return "-";
    }
  }

  public static CoordinateSystem getCoordinateSystem(final Object value) {
    CoordinateSystem coordinateSystem = null;
    if (value instanceof CoordinateSystem) {
      coordinateSystem = (CoordinateSystem)value;
    } else if (value != null) {
      try {
        final int coordinateSystemId = Integer.parseInt(DataTypes.toString(value));
        coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(coordinateSystemId);
      } catch (final Throwable t) {
      }
    }
    return coordinateSystem;
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
  public static <GFP extends GeometryFactoryProxy> void promptCoordinateSystem(final String title,
    final GFP geometryFactoryProxy, final Consumer<GeometryFactory> action) {
    if (geometryFactoryProxy.isHasHorizontalCoordinateSystem()) {
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
    final HorizontalCoordinateSystemProxy defaultCoordinateSystem,
    final Consumer<GeometryFactory> action) {
    selectHorizontalCoordinateSystem(title, defaultCoordinateSystem, coordinateSystem -> {
      final GeometryFactory geometryFactory = coordinateSystem.getGeometryFactory();
      action.accept(geometryFactory);
    });
  }

  public static void selectHorizontalCoordinateSystem(final String title,
    final HorizontalCoordinateSystemProxy defaultCoordinateSystem,
    final Consumer<HorizontalCoordinateSystem> action) {
    Invoke.later(() -> {
      final Project project = Project.get();
      final MapPanel mapPanel = project.getMapPanel();
      int coordinateSystemId = -1;
      if (defaultCoordinateSystem != null) {
        coordinateSystemId = defaultCoordinateSystem.getHorizontalCoordinateSystemId();
      }
      if (coordinateSystemId <= 0) {
        coordinateSystemId = mapPanel.getHorizontalCoordinateSystemId();
      }
      final CoordinateSystemField coordinateSystemField = new CoordinateSystemField(
        "coordinateSystem");
      if (coordinateSystemId > 0) {
        coordinateSystemField.setSelectedItem(coordinateSystemId);
      }
      final JPanel fieldPanel = new BasePanel(new JLabel("Coordinate System"),
        coordinateSystemField);
      GroupLayouts.makeColumns(fieldPanel, 2, true);
      final ValueField valueField = new ValueField(fieldPanel);
      valueField.add(fieldPanel);
      valueField.showDialog();
      if (valueField.isSaved()) {
        final HorizontalCoordinateSystem coordinateSystem = coordinateSystemField
          .getHorizontalCoordinateSystem();
        if (coordinateSystem != null) {
          Invoke.background(title, () -> action.accept(coordinateSystem));
        }
      }
    });
  }

  public CoordinateSystemField(final String fieldName) {
    super("fieldName", newModel(), CoordinateSystemField::formatCoordinateSystem);

    setEditable(false);
    addItemListener(this);
    final Dimension size = new Dimension(240, 22);
    setMaximumSize(size);
    setToolTipText("Coordinate System");
  }

  public void addCoordinateSystem(final CoordinateSystem coordinateSystem) {
    final int srid = coordinateSystem.getHorizontalCoordinateSystemId();
    addCoordinateSystem(srid);
  }

  public void addCoordinateSystem(final int srid) {
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

  public HorizontalCoordinateSystem getHorizontalCoordinateSystem() {
    final Integer coordinateSystemId = getSelectedItem();
    if (coordinateSystemId > 0) {
      return EpsgCoordinateSystems.getHorizontalCoordinateSystem(coordinateSystemId);
    } else {
      return null;
    }
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    final int stateChange = e.getStateChange();
    if (stateChange == ItemEvent.SELECTED) {
      final Object value = e.getItem();
      final CoordinateSystem coordinateSystem = getCoordinateSystem(value);
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
