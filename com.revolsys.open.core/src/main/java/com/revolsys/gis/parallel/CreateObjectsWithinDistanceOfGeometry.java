package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.MapContext;
import org.jeometry.common.io.PathName;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineCap;
import com.revolsys.geometry.model.LineJoin;
import com.revolsys.geometry.simplify.DouglasPeuckerSimplifier;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.util.JexlUtil;

public class CreateObjectsWithinDistanceOfGeometry extends BaseInOutProcess<Record, Record> {

  private Map<String, Object> attributes = new HashMap<>();

  private double distance;

  private Channel<Record> geometryIn;

  private List<Record> geometryObjects = new ArrayList<>();

  private Map<RecordDefinition, Map<RecordDefinition, Geometry>> recordDefinitionGeometryMap = new HashMap<>();

  private String typePathTemplate;

  private Expression typePathTemplateExpression;

  private boolean writeOriginal;

  @Override
  protected void destroy() {
    super.destroy();
    if (this.geometryIn != null) {
      this.geometryIn.readDisconnect();
      this.geometryIn = null;
    }
    this.attributes = null;
    this.geometryObjects = null;
    this.recordDefinitionGeometryMap = null;
  }

  public double getDistance() {
    return this.distance;
  }

  public Map<String, Object> getFields() {
    return this.attributes;
  }

  public Channel<Record> getGeometryIn() {
    if (this.geometryIn == null) {
      setGeometryIn(new Channel<Record>());
    }
    return this.geometryIn;
  }

  public List<Record> getGeometryObjects() {
    return this.geometryObjects;
  }

  private final Map<RecordDefinition, Geometry> getRecordDefinitionGeometries(
    final RecordDefinition recordDefinition) {
    Map<RecordDefinition, Geometry> recordDefinitionGeometries = this.recordDefinitionGeometryMap
      .get(recordDefinition);
    if (recordDefinitionGeometries == null) {
      recordDefinitionGeometries = new LinkedHashMap<>();
      RecordDefinition newRecordDefinition;
      Geometry preparedGeometry;
      for (final Record record : this.geometryObjects) {
        Geometry geometry = record.getGeometry();
        if (geometry != null) {
          final Map<String, Object> vars = new HashMap<>(this.attributes);
          vars.putAll(record);
          vars.put("typePath", recordDefinition.getPath());
          final MapContext context = new MapContext(vars);
          final String typePath = (String)JexlUtil.evaluateExpression(context,
            this.typePathTemplateExpression);
          newRecordDefinition = new RecordDefinitionImpl(PathName.newPathName(typePath),
            recordDefinition.getFields());
          if (this.distance > 0) {
            geometry = geometry.buffer(this.distance, 1, LineCap.SQUARE, LineJoin.MITER, 1.0D);
          }
          geometry = DouglasPeuckerSimplifier.simplify(geometry, 2D);
          preparedGeometry = geometry.prepare();
          recordDefinitionGeometries.put(newRecordDefinition, preparedGeometry);
        }
      }

      this.recordDefinitionGeometryMap.put(recordDefinition, recordDefinitionGeometries);
    }
    return recordDefinitionGeometries;
  }

  public String getTypeNameTemplate() {
    return this.typePathTemplate;
  }

  private void initializeGeometries(final Channel<Record> geometryIn) {
    if (geometryIn != null) {
      for (final Record object : geometryIn) {
        this.geometryObjects.add(object);
      }
    }
  }

  public boolean isWriteOriginal() {
    return this.writeOriginal;
  }

  @Override
  protected void preRun(final Channel<Record> in, final Channel<Record> out) {
    initializeGeometries(this.geometryIn);
  }

  @Override
  protected void process(final Channel<Record> in, final Channel<Record> out, final Record object) {
    if (this.writeOriginal) {
      out.write(object);
    }
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    final Geometry geometryValue = object.getGeometry();
    final Map<RecordDefinition, Geometry> recordDefinitionGeometries = getRecordDefinitionGeometries(
      recordDefinition);
    for (final Entry<RecordDefinition, Geometry> recordDefinitionGeometry : recordDefinitionGeometries
      .entrySet()) {
      final RecordDefinition newRecordDefinition = recordDefinitionGeometry.getKey();
      final Geometry intersectsGeometry = recordDefinitionGeometry.getValue();
      if (intersectsGeometry.intersects(geometryValue)) {
        final Record newRecord = new ArrayRecord(newRecordDefinition, object);
        out.write(newRecord);
      }
    }
  }

  public void setAttributes(final Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  public void setDistance(final double distance) {
    this.distance = distance;
  }

  public void setGeometryIn(final Channel<Record> geometryIn) {
    this.geometryIn = geometryIn;
    geometryIn.readConnect();
  }

  public void setGeometryObjects(final List<Record> geometryObjects) {
    this.geometryObjects = geometryObjects;
  }

  public void setTypeNameTemplate(final String typePathTemplate) {
    this.typePathTemplate = typePathTemplate;
    try {
      this.typePathTemplateExpression = JexlUtil.newExpression(typePathTemplate,
        "%\\{([^\\}]+)\\}");
    } catch (final Exception e) {
      throw new IllegalArgumentException(new StringBuilder().append("Invalid type name template: ")
        .append(typePathTemplate)
        .toString(), e);
    }
  }

  public void setWriteOriginal(final boolean writeOriginal) {
    this.writeOriginal = writeOriginal;
  }
}
