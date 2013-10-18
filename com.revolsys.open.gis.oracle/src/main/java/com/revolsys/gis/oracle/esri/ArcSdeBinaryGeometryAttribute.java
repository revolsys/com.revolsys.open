package com.revolsys.gis.oracle.esri;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRegistration;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.esri.sde.sdk.client.SeTable;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.jdbc.attribute.JdbcAttribute;
import com.revolsys.util.ExceptionUtil;

public class ArcSdeBinaryGeometryAttribute extends JdbcAttribute {

  private final ArcSdeSpatialReference spatialReference;

  private final GeometryFactory geometryFactory;

  private final int numAxis;

  private final ArcSdeBinaryGeometryDataStoreExtension extension;

  private String tableName;

  private String[] geometryColumns;

  private boolean valid;

  private String objectIdColumn;

  public ArcSdeBinaryGeometryAttribute(
    final ArcSdeBinaryGeometryDataStoreExtension extension, final String name,
    final DataType type, final boolean required,
    final Map<String, Object> properties,
    final ArcSdeSpatialReference spatialReference, final int numAxis) {
    super(name, type, -1, 0, 0, required, properties);
    this.extension = extension;
    this.spatialReference = spatialReference;
    final GeometryFactory factory = spatialReference.getGeometryFactory();
    this.geometryFactory = GeometryFactory.getFactory(factory.getSRID(),
      numAxis, factory.getScaleXY(), factory.getScaleZ());
    this.numAxis = numAxis;
    setProperty(AttributeProperties.GEOMETRY_FACTORY, this.geometryFactory);
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public int getNumAxis() {
    return this.numAxis;
  }

  public ArcSdeSpatialReference getSpatialReference() {
    return this.spatialReference;
  }

  @Override
  public int setAttributeValueFromResultSet(final ResultSet resultSet,
    final int columnIndex, final DataObject object) throws SQLException {
    if (valid) {
      final int geometryId = resultSet.getInt(columnIndex);
      if (!resultSet.wasNull()) {
        try {
          final SeConnection connection = extension.createSeConnection();
          try {
            final String where = "\"" + getName() + "\" = " + geometryId;
            final SeSqlConstruct sqlConstruct = new SeSqlConstruct(tableName,
              where);
            final SeQuery query = new SeQuery(connection, geometryColumns,
              sqlConstruct);
            try {

              final SeRow row = query.fetch();
              ArcSdeBinaryGeometryDataStoreExtension.setValueFromRow(object,
                row, 0);
            } finally {
              query.close();
            }

          } finally {
            ArcSdeBinaryGeometryDataStoreExtension.close(connection);
          }
        } catch (final SeException e) {
          ExceptionUtil.log(getClass(), "Unable to read geometry", e);
        }
      }
    }
    return columnIndex + 1;

  }

  @Override
  protected void setMetaData(final DataObjectMetaData metaData) {
    final SeConnection connection = extension.createSeConnection();

    try {
      tableName = extension.getTableName(metaData);
      final SeTable table = new SeTable(connection, tableName);
      for (final SeColumnDefinition column : table.describe()) {
        final String columnName = column.getName();
        final short rowIdType = column.getRowIdType();
        if (rowIdType == SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_SDE) {
          objectIdColumn = columnName;
        }
      }
      geometryColumns = new String[] {
        getName()
      };
      valid = true;
    } catch (final SeException e) {
      ExceptionUtil.log(getClass(), "Unable to initialize SDE column", e);
    } finally {
      ArcSdeBinaryGeometryDataStoreExtension.close(connection);
    }
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    throw new UnsupportedOperationException(
      "Editing ArcSDE binary geometries is not supported");
  }
}
