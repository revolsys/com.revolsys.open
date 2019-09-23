package com.revolsys.swing.map.layer.record;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JLabel;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.simplify.DouglasPeuckerSimplifier;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ProgressMonitor;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.NumberTextField;

public class RecordLayerActions {

  public static void background(final String title, final List<LayerRecord> records,
    final Consumer<LayerRecord> action) {
    final int recordCount = records.size();
    final String fullTitle = title + " " + recordCount + " records";
    ProgressMonitor.background(fullTitle, records, action);
  }

  public static void generalize(final AbstractRecordLayer layer, final List<LayerRecord> records) {
    final Double distanceTolerance = generalizeGetDistance(layer);
    generalize(records, distanceTolerance);
  }

  public static void generalize(final List<LayerRecord> records, final Double distanceTolerance) {
    if (distanceTolerance != null) {
      final Consumer<LayerRecord> action = record -> {
        final Geometry geometry = record.getGeometry();
        final Geometry newGeometry = DouglasPeuckerSimplifier.simplify(geometry, distanceTolerance,
          true);
        record.setGeometryValue(newGeometry);
      };
      final String title = "Generalize";
      background(title, records, action);
    }
  }

  public static Double generalizeGetDistance(final AbstractRecordLayer layer) {
    final ValueField dialog = new ValueField(new BorderLayout());
    dialog.setTitle("Generalize Vertices");

    final NumberTextField distanceField = new NumberTextField(DataTypes.DOUBLE, 10);
    distanceField.setFieldValue(2.0);
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

}
