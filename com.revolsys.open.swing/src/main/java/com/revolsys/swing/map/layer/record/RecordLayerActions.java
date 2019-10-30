package com.revolsys.swing.map.layer.record;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JLabel;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.simplify.DouglasPeuckerSimplifier;
import com.revolsys.record.Record;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.NumberTextField;

public class RecordLayerActions {

  public static void generalize(final AbstractRecordLayer layer, final List<LayerRecord> records) {
    final Double distanceTolerance = generalizeGetDistance(layer);
    generalize(layer, records, distanceTolerance);
  }

  public static void generalize(final AbstractRecordLayer layer, final List<LayerRecord> records,
    final Double distanceTolerance) {
    if (distanceTolerance != null) {
      final Consumer<LayerRecord> action = record -> generalizeRecord(record, distanceTolerance);
      layer.processTasks("Generalize", records, action);
    }
  }

  public static Double generalizeGetDistance(final AbstractRecordLayer layer) {
    final ValueField dialog = new ValueField(new BorderLayout());
    dialog.setTitle("Generalize Vertices");

    final NumberTextField distanceField = new NumberTextField(DataTypes.DOUBLE, 10, 2);
    distanceField.setFieldValue(layer.getGeneralizeGeometryTolerance());
    dialog.add(distanceField);
    final String unit = layer.getHorizontalCoordinateSystem().getLengthUnit().toString();
    final JLabel label = SwingUtil.newLabel("Distance Tolerance (" + unit + ")");
    final BasePanel fieldPanel = new BasePanel(new FlowLayout(), label, distanceField);
    dialog.add(fieldPanel, BorderLayout.CENTER);

    dialog.showDialog();
    if (dialog.isSaved()) {
      return distanceField.getFieldValue();
    } else {
      return null;
    }

  }

  public static void generalizeRecord(final Record record, final double distanceTolerance) {
    final Geometry geometry = record.getGeometry();
    if (geometry != null) {
      final Geometry newGeometry = DouglasPeuckerSimplifier.simplify(geometry, distanceTolerance,
        true);
      record.setGeometryValue(newGeometry);
    }
  }

}
