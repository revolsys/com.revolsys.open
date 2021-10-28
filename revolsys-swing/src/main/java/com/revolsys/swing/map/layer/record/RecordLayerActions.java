package com.revolsys.swing.map.layer.record;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.function.Consumer;

import javax.measure.Unit;
import javax.measure.quantity.Length;
import javax.swing.JLabel;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.simplify.DouglasPeuckerSimplifier;
import com.revolsys.record.Record;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.NumberTextField;

import tech.units.indriya.ComparableQuantity;
import tech.units.indriya.quantity.Quantities;

public class RecordLayerActions {

  public static void generalize(final AbstractRecordLayer layer, final Integer recordCount,
    final Consumer<Consumer<LayerRecord>> forEachRecord) {
    final ComparableQuantity<Length> distanceTolerance = generalizeGetDistance(layer);
    if (distanceTolerance != null) {
      final Consumer<LayerRecord> action = record -> generalizeRecord(record, distanceTolerance);
      layer.processTasks("Generalize", recordCount, forEachRecord, action);
    }
  }

  public static ComparableQuantity<Length> generalizeGetDistance(final AbstractRecordLayer layer) {
    final ComparableQuantity<Length> defaultValue = layer.getGeneralizeGeometryTolerance();
    return getDistanceTolerance("Generalize Vertices", defaultValue);
  }

  public static void generalizeRecord(final Record record,
    final ComparableQuantity<Length> tolerance) {
    final Geometry geometry = record.getGeometry();
    if (geometry != null) {
      final double distanceTolerance = tolerance.getValue().doubleValue();
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

  public static ComparableQuantity<Length> getDistanceTolerance(final String title,
    final ComparableQuantity<Length> defaultValue) {
    final Unit<Length> unit = defaultValue.getUnit();
    final ValueField dialog = new ValueField(new BorderLayout());
    dialog.setTitle(title);

    final NumberTextField distanceField = new NumberTextField(DataTypes.DOUBLE, 10, 2);
    distanceField.setFieldValue(defaultValue.getValue());
    dialog.add(distanceField);
    final JLabel label = SwingUtil.newLabel("Distance Tolerance (" + unit + ")");
    final BasePanel fieldPanel = new BasePanel(new FlowLayout(), label, distanceField);
    dialog.add(fieldPanel, BorderLayout.CENTER);

    dialog.showDialog();
    if (dialog.isSaved()) {
      final Double tolerance = distanceField.getFieldValue();
      return Quantities.getQuantity(tolerance, unit);
    } else {
      return null;
    }
  }

}
