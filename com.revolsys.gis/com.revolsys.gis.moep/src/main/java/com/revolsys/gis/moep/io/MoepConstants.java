package com.revolsys.gis.moep.io;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataTypes;

public final class MoepConstants {

  public static final String ADDITION_MODIFIED = "additionModified";

  public static final String ADDITION_NEW = "additionNew";

  public static final String ADMIT_INTEGRATION_DATE = "admitIntegrationDate";

  public static final String ADMIT_REASON_FOR_CHANGE = "admitReasonForChange";

  public static final String ADMIT_REVISION_KEY = "admitRevisionKey";

  public static final String ADMIT_SOURCE_DATE = "admitSourceDate";

  public static final String ADMIT_SPECIFICATIONS_RELEASE = "admitSpecificationsRelease";

  public static final String ANGLE = "angle";

  public static final String ATTRIBUTE = "attribute";

  public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyMMdd");

  public static final String DELETION_WITH_REPLACEMENT = "deletionWithReplacement";

  public static final String DELETION_WITHOUT_REPLACEMENT = "deletionWithoutReplacement";

  public static final String DISPLAY_TYPE = "displayType";

  public static final String ELEVATION = "elevation";

  public static final String FEATURE_CODE = "featureCode";

  public static final String CHARACTER_HEIGHT = "characterHeight";

  public static final String FONT_NAME = "fontName";

  public static final DateFormat FULL_DATE_FORMAT = new SimpleDateFormat(
    "yyyyMMdd");

  public static final String GEOMETRY = "geometry";

  public static final DataObjectMetaData META_DATA;

  static final String ORIGINAL_FILE_TYPE = "originalFileType";

  public static final String RETIRE_INTEGRATION_DATE = "retireIntegrationDate";

  public static final String RETIRE_REASON_FOR_CHANGE = "retireReasonForChange";

  public static final String RETIRE_REVISION_KEY = "retireRevisionKey";

  public static final String RETIRE_SOURCE_DATE = "retireSourceDate";

  public static final String RETIRE_SPECIFICATIONS_RELEASE = "retireSpecificationsRelease";

  public static final String TEXT = "text";

  public static final QName TYPE_NAME = new QName("MOEP", "Feature");

  public static final String OTHER = "other";

  public static final String TEXT_GROUP = "textGroup";

  public static final String ORIENTATION = "orientation";

  static {
    final DataObjectMetaDataImpl type = new DataObjectMetaDataImpl(TYPE_NAME);
    type.addAttribute(FEATURE_CODE, DataTypes.STRING, true);
    type.addAttribute(ADMIT_SOURCE_DATE, DataTypes.DATE, false);
    type.addAttribute(ADMIT_INTEGRATION_DATE, DataTypes.DATE, false);
    type.addAttribute(ADMIT_REASON_FOR_CHANGE, DataTypes.STRING, false);
    type.addAttribute(ADMIT_REVISION_KEY, DataTypes.STRING, false);
    type.addAttribute(ADMIT_SPECIFICATIONS_RELEASE, DataTypes.STRING, false);
    type.addAttribute(RETIRE_SOURCE_DATE, DataTypes.DATE, false);
    type.addAttribute(RETIRE_INTEGRATION_DATE, DataTypes.DATE, false);
    type.addAttribute(RETIRE_REASON_FOR_CHANGE, DataTypes.STRING, false);
    type.addAttribute(RETIRE_REVISION_KEY, DataTypes.STRING, false);
    type.addAttribute(RETIRE_SPECIFICATIONS_RELEASE, DataTypes.STRING, false);
    type.addAttribute(ORIGINAL_FILE_TYPE, DataTypes.STRING, false);
    type.addAttribute(ATTRIBUTE, DataTypes.STRING, false);
    type.addAttribute(DISPLAY_TYPE, DataTypes.STRING, true);
    type.addAttribute(ANGLE, DataTypes.DECIMAL, false);
    type.addAttribute(ELEVATION, DataTypes.DECIMAL, false);
    type.addAttribute(TEXT, DataTypes.STRING, false);
    type.addAttribute(GEOMETRY, DataTypes.GEOMETRY, true);
    type.setGeometryAttributeName(GEOMETRY);
    META_DATA = type;

  }

  private MoepConstants() {
  }
}
