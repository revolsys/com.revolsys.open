package com.revolsys.io.moep;

import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.types.DataTypes;

public final class MoepConstants {
  public static final String ADMIT_INTEGRATION_DATE = "A_DATE";

  public static final String ADMIT_REASON_FOR_CHANGE = "A_REASON";

  public static final String ADMIT_REVISION_KEY = "A_REV_KEY";

  public static final String ADMIT_SOURCE_DATE = "A_SRC_DATE";

  public static final String ADMIT_SPECIFICATIONS_RELEASE = "A_SPEC";

  public static final String ANGLE = "ANGLE";

  public static final String DISPLAY_TYPE = "DISP_TYPE";

  public static final String ELEVATION = "ELEVATION";

  public static final String FEATURE_CODE = "FEAT_CODE";

  public static final String FONT_SIZE = "FONT_SIZE";

  public static final String FONT_NAME = "FONT_NAME";

  public static final String GEOMETRY = "geometry";

  public static final String MAPSHEET_NAME = "MAPSHEET";

  public static final RecordDefinition RECORD_DEFINITION;

  public static final String ORIGINAL_FILE_TYPE = "FILE_TYPE";

  public static final String RETIRE_INTEGRATION_DATE = "R_DATE";

  public static final String RETIRE_REASON_FOR_CHANGE = "R_REASON";

  public static final String RETIRE_REVISION_KEY = "R_REV_KEY";

  public static final String RETIRE_SOURCE_DATE = "R_SRC_DATE";

  public static final String RETIRE_SPECIFICATIONS_RELEASE = "R_SPEC";

  public static final String TEXT = "TEXT";

  public static final String TYPE_NAME = "/MOEP/Feature";

  public static final String FONT_WEIGHT = "FONTWEIGHT";

  public static final String TEXT_GROUP = "TEXT_GROUP";

  public static final String TEXT_INDEX = "TEXT_INDEX";

  static {
    RECORD_DEFINITION = createRecordDefinition(TYPE_NAME);
  }

  public static RecordDefinitionImpl createRecordDefinition(final String typePath) {
    final RecordDefinitionImpl type = new RecordDefinitionImpl(typePath);
    type.addAttribute(FEATURE_CODE, DataTypes.STRING, 10, true);
    type.addAttribute(MAPSHEET_NAME, DataTypes.STRING, 7, false);
    type.addAttribute(DISPLAY_TYPE, DataTypes.STRING, 20, true);
    type.addAttribute(ANGLE, DataTypes.DECIMAL, false);
    type.addAttribute(ELEVATION, DataTypes.DECIMAL, false);
    type.addAttribute(TEXT_GROUP, DataTypes.DECIMAL, false);
    type.addAttribute(TEXT_INDEX, DataTypes.DECIMAL, false);
    type.addAttribute(TEXT, DataTypes.STRING, 200, false);
    type.addAttribute(FONT_NAME, DataTypes.STRING, 10, false);
    type.addAttribute(FONT_SIZE, DataTypes.DECIMAL, false);
    type.addAttribute(FONT_WEIGHT, DataTypes.STRING, 10, false);
    type.addAttribute(ORIGINAL_FILE_TYPE, DataTypes.STRING, 20, false);
    type.addAttribute(ADMIT_SOURCE_DATE, DataTypes.DATE, false);
    type.addAttribute(ADMIT_REASON_FOR_CHANGE, DataTypes.STRING, 1, false);
    type.addAttribute(ADMIT_INTEGRATION_DATE, DataTypes.DATE, false);
    type.addAttribute(ADMIT_REVISION_KEY, DataTypes.STRING, 10, false);
    type.addAttribute(ADMIT_SPECIFICATIONS_RELEASE, DataTypes.STRING, 10, false);
    type.addAttribute(RETIRE_SOURCE_DATE, DataTypes.DATE, false);
    type.addAttribute(RETIRE_REASON_FOR_CHANGE, DataTypes.STRING, 1, false);
    type.addAttribute(RETIRE_INTEGRATION_DATE, DataTypes.DATE, false);
    type.addAttribute(RETIRE_REVISION_KEY, DataTypes.STRING, 10, false);
    type.addAttribute(RETIRE_SPECIFICATIONS_RELEASE, DataTypes.STRING, 10,
      false);
    type.addAttribute(GEOMETRY, DataTypes.GEOMETRY, true);
    type.setGeometryAttributeName(GEOMETRY);
    return type;
  }

  private MoepConstants() {
  }
}
