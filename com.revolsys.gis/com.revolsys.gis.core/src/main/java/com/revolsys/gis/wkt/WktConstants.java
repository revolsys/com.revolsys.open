package com.revolsys.gis.wkt;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataTypes;

public interface WktConstants {

  String FILE_EXTENSION = "wkt";

  String DESCRIPTION = "Well-Known Text Geometry";

  String MEDIA_TYPE = "text/x-wkt";

  DataObjectMetaData META_DATA = new DataObjectMetaDataImpl(
    new QName("Feature"), new Attribute("geometry", DataTypes.GEOMETRY, true));
}
