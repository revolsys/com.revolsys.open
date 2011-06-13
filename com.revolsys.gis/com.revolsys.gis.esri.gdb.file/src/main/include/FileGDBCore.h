//
// FileGDBCore.h
//

/*
  COPYRIGHT © 2011 ESRI
  TRADE SECRETS: ESRI PROPRIETARY AND CONFIDENTIAL
  Unpublished material - all rights reserved under the
  Copyright Laws of the United States and applicable international
  laws, treaties, and conventions.

  For additional information, contact:
  Environmental Systems Research Institute, Inc.
  Attn: Contracts and Legal Services Department
  380 New York Street
  Redlands, California, 92373
  USA

  email: contracts@esri.com
*/

#pragma once

typedef unsigned char  byte;
typedef int            int32;
typedef unsigned int   uint32;
typedef short          int16;
typedef unsigned short uint16;

typedef int            fgdbError;

#if defined (S_OK)
  #undef S_OK
  #define S_OK ((fgdbError)0x00000000)
#else
  #define S_OK ((fgdbError)0x00000000)
#endif

#if defined (S_FALSE)
  #undef S_FALSE
  #define S_FALSE ((fgdbError)0x00000001)
#else
  #define S_FALSE ((fgdbError)0x00000001)
#endif

#if defined (E_FAIL)
  #undef E_FAIL
  #define E_FAIL ((fgdbError)0x80004005)
#else
  #define E_FAIL ((fgdbError)0x80004005)
#endif

#if defined (SUCCEEDED)
  #undef SUCCEEDED
  #define SUCCEEDED(result) ((fgdbError)(result) >= 0)
#else
  #define SUCCEEDED(result) ((fgdbError)(result) >= 0)
#endif

#if defined (FAILED)
  #undef FAILED
  #define FAILED(result) ((fgdbError)(result) < 0)
#else
  #define FAILED(result) ((fgdbError)(result) < 0)
#endif

namespace FileGDBAPI
{

enum FieldType
{
  fieldTypeSmallInteger = 0,
  fieldTypeInteger      = 1,
  fieldTypeSingle       = 2,
  fieldTypeDouble       = 3,
  fieldTypeString       = 4,
  fieldTypeDate         = 5,
  fieldTypeOID          = 6,
  fieldTypeGeometry     = 7,
  fieldTypeBlob         = 8,
  fieldTypeRaster       = 9,
  fieldTypeGUID         = 10,
  fieldTypeGlobalID     = 11,
  fieldTypeXML          = 12,
};

enum ShapeType
{
  shapeNull               = 0,
  shapePoint              = 1,
  shapePointM             = 21,
  shapePointZM            = 11,
  shapePointZ             = 9,
  shapeMultipoint         = 8,
  shapeMultipointM        = 28,
  shapeMultipointZM       = 18,
  shapeMultipointZ        = 20,
  shapePolyline           = 3,
  shapePolylineM          = 23,
  shapePolylineZM         = 13,
  shapePolylineZ          = 10,
  shapePolygon            = 5,
  shapePolygonM           = 25,
  shapePolygonZM          = 15,
  shapePolygonZ           = 19,
  shapeMultiPatchM        = 31,
  shapeMultiPatch         = 32,
  shapeGeneralPolyline    = 50,
  shapeGeneralPolygon     = 51,
  shapeGeneralPoint       = 52,
  shapeGeneralMultipoint  = 53,
  shapeGeneralMultiPatch  = 54,
};

enum ShapeModifiers
{
  shapeHasZs                  = 0x80000000,
  shapeHasMs                  = 1073741824,
  shapeHasCurves              = 536870912,
  shapeHasIDs                 = 268435456,
  shapeHasNormals             = 134217728,
  shapeHasTextures            = 67108864,
  shapeHasPartIDs             = 33554432,
  shapeHasMaterials           = 16777216,
  shapeIsCompressed           = 8388608,
  shapeModifierMask           = -16777216,
  shapeMultiPatchModifierMask = 15728640,
  shapeBasicTypeMask          = 255,
  shapeBasicModifierMask      = -1073741824,
  shapeNonBasicModifierMask   = 1056964608,
  shapeExtendedModifierMask   = -587202560
};

enum GeometryType
{
  geometryNull        = 0,
  geometryPoint       = 1,
  geometryMultipoint  = 2,
  geometryPolyline    = 3,
  geometryPolygon     = 4,
  geometryMultiPatch  = 9,
};

};  // namespace FileGDBAPI
