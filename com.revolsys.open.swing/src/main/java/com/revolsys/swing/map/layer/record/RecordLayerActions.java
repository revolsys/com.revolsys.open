package com.revolsys.swing.map.layer.record;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JLabel;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.simplify.DouglasPeuckerSimplifier;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.NumberTextField;

public class RecordLayerActions {

  public static void generalize(final AbstractRecordLayer layer, final List<LayerRecord> records) {
    final ValueField dialog = new ValueField(new BorderLayout());
    dialog.setTitle("Generalize Vertices");

    final NumberTextField distanceField = new NumberTextField(DataTypes.DOUBLE, 10);
    distanceField.setFieldValue(1.0);
    dialog.add(distanceField);
    final String unit = layer.getHorizontalCoordinateSystem().getLengthUnit().toString();
    final JLabel label = SwingUtil.newLabel("Distance Tolerance (" + unit + ")");
    final BasePanel fieldPanel = new BasePanel(new FlowLayout(), label, distanceField);
    dialog.add(fieldPanel, BorderLayout.CENTER);

    dialog.showDialog();
    if (dialog.isSaved()) {
      for (final LayerRecord record : records) {
        final Geometry geometry = record.getGeometry();
        final Double distanceTolerance = distanceField.getFieldValue();
        final Geometry newGeometry = DouglasPeuckerSimplifier.simplify(geometry, distanceTolerance);
        record.setGeometryValue(newGeometry);
      }
    }

  }

}
