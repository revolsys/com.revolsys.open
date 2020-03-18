package com.revolsys.swing.map.layer.record;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
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

  public static void generalize(final AbstractRecordLayer layer, final Integer recordCount,
    final Consumer<Consumer<LayerRecord>> forEachRecord) {
    final Double distanceTolerance = generalizeGetDistance(layer);
    if (distanceTolerance != null) {
      final Consumer<LayerRecord> action = record -> generalizeRecord(record, distanceTolerance);
      layer.processTasks("Generalize", recordCount, forEachRecord, action);
    }
  }

  public static Double generalizeGetDistance(final AbstractRecordLayer layer) {
    final double defaultValue = layer.getGeneralizeGeometryTolerance();
    return getDistanceTolerance(layer, "Generalize Vertices", defaultValue);
  }

  public static void generalizeRecord(final Record record, final double distanceTolerance) {
    final Geometry geometry = record.getGeometry();
    if (geometry != null) {
      final Geometry newGeometry = DouglasPeuckerSimplifier.simplify(geometry, distanceTolerance,
        true);
      record.setGeometryValue(newGeometry);
    }
    if (record instanceof LayerRecord) {
      final LayerRecord layerRecord = (LayerRecord)record;
      final AbstractRecordLayer layer = layerRecord.getLayer();
      layer.postProcess(layerRecord);
    }
  }

  public static Double getDistanceTolerance(final AbstractRecordLayer layer, final String title,
    final double defaultValue) {
    final ValueField dialog = new ValueField(new BorderLayout());
    dialog.setTitle(title);

    final NumberTextField distanceField = new NumberTextField(DataTypes.DOUBLE, 10, 2);
    distanceField.setFieldValue(defaultValue);
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
