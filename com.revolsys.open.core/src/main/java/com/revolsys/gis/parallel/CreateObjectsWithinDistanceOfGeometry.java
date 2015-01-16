package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.context.HashMapContext;

import com.revolsys.data.record.ArrayRecord;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.operation.buffer.Buffer;
import com.revolsys.jts.operation.buffer.BufferParameters;
import com.revolsys.jts.simplify.DouglasPeuckerSimplifier;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.revolsys.util.JexlUtil;

public class CreateObjectsWithinDistanceOfGeometry extends
BaseInOutProcess<Record, Record> {

  private Map<String, Object> attributes = new HashMap<String, Object>();

  private double distance;

  private Channel<Record> geometryIn;

  private List<Record> geometryObjects = new ArrayList<Record>();

  private Map<RecordDefinition, Map<RecordDefinition, Geometry>> recordDefinitionGeometryMap = new HashMap<RecordDefinition, Map<RecordDefinition, Geometry>>();

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
    Map<RecordDefinition, Geometry> recordDefinitionGeometries = this.recordDefinitionGeometryMap.get(recordDefinition);
    if (recordDefinitionGeometries == null) {
      recordDefinitionGeometries = new LinkedHashMap<RecordDefinition, Geometry>();
      RecordDefinition newRecordDefinition;
      Geometry preparedGeometry;
      for (final Record record : this.geometryObjects) {
        Geometry geometry = record.getGeometryValue();
        if (geometry != null) {
          final JexlContext context = new HashMapContext();
          final Map<String, Object> vars = new HashMap<String, Object>(
              this.attributes);
          vars.putAll(record);
          vars.put("typePath", recordDefinition.getPath());
          context.setVars(vars);
          final String typePath = (String)JexlUtil.evaluateExpression(context,
            this.typePathTemplateExpression);
          newRecordDefinition = new RecordDefinitionImpl(typePath,
            recordDefinition.getFields());
          if (this.distance > 0) {
            final BufferParameters parameters = new BufferParameters(1, 3, 2,
              1.0D);
            geometry = Buffer.buffer(geometry, this.distance, parameters);
          }
          geometry = DouglasPeuckerSimplifier.simplify(geometry, 2D);
          preparedGeometry = geometry.prepare();
          recordDefinitionGeometries.put(newRecordDefinition, preparedGeometry);
        }
      }

      this.recordDefinitionGeometryMap.put(recordDefinition,
        recordDefinitionGeometries);
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
  protected void process(final Channel<Record> in, final Channel<Record> out,
    final Record object) {
    if (this.writeOriginal) {
      out.write(object);
    }
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    final Geometry geometryValue = object.getGeometryValue();
    final Map<RecordDefinition, Geometry> recordDefinitionGeometries = getRecordDefinitionGeometries(recordDefinition);
    for (final Entry<RecordDefinition, Geometry> recordDefinitionGeometry : recordDefinitionGeometries.entrySet()) {
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
      this.typePathTemplateExpression = JexlUtil.createExpression(
        typePathTemplate, "%\\{([^\\}]+)\\}");
    } catch (final Exception e) {
      throw new IllegalArgumentException(new StringBuilder().append(
          "Invalid type name template: ")
          .append(typePathTemplate)
          .toString(), e);
    }
  }

  public void setWriteOriginal(final boolean writeOriginal) {
    this.writeOriginal = writeOriginal;
  }
}
