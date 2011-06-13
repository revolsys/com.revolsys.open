//
// Util.h
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

#include <string>

#ifndef EXPORT_FILEGDB_API
# ifdef linux
#  define EXT_FILEGDB_API
# else
#  define EXT_FILEGDB_API _declspec(dllimport)
# endif
#else
# ifdef linux
#  define EXT_FILEGDB_API __attribute__((visibility("default")))
# else
#  define EXT_FILEGDB_API _declspec(dllexport)
#endif
#endif

#include "FileGDBCore.h"

struct IFields;
class SqlSelectCommand;

namespace FileGDBAPI
{

class Row;
class FieldInfo;
class Point;

/// An enumerator of rows. Used as a return type for table queries.

class EXT_FILEGDB_API EnumRows
{
public:

  /// Returns the next available row in the enumerator, or null if no rows remain.
  /// @param[out]   row The next row in the enumerator.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError Next(Row& row);

  /// Closes the enumerator and releases any resources it is holding.
  void Close();

  /// Return information about the fields in the row.
  /// @param[out]   fieldInfo The field information.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetFieldInformation(FieldInfo& fieldInfo);

  /// @name Constructors and destructors
  //@{
  /// The class constructor.
  EnumRows();

  /// The class destructor.
  ~EnumRows();
  //@}

private:

  /// @cond PRIVATE
  fgdbError SetupRows(SqlSelectCommand* pSqlSelectCommand);
  bool      IsSetup();

  SqlSelectCommand* m_pSqlSelectCommand;

  friend class Geodatabase;
  friend class Table;

  EnumRows(const EnumRows&)             { }
  EnumRows& operator=(const EnumRows&)  { return *this; }
  /// @endcond
};

/// A utility class for providing field information.
class EXT_FILEGDB_API FieldInfo
{
public:

  /// The number of fields.
  /// @param[out]   fieldCount The number of fields.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetFieldCount(int& fieldCount);

  /// The name of the field.
  /// @param[in]    fieldNumber The number of field.
  /// @param[out]   fieldName The name of the field.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetFieldName(int fieldNumber, std::wstring& fieldName);

  /// The data type of the field.
  /// @param[in]    fieldNumber The number of field.
  /// @param[out]   fieldType The data type of the field.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetFieldType(int fieldNumber, FieldType& fieldType);

  /// The length of the field.
  /// @param[in]    fieldNumber The number of field.
  /// @param[out]   fieldLength The length of the field.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetFieldLength(int fieldNumber, int& fieldLength);

  /// The nullability of the field.
  /// @param[in]    fieldNumber The number of field.
  /// @param[out]   isNullable The nullability of the field.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetFieldIsNullable(int fieldNumber, bool& isNullable);

  /// @name Constructors and destructors
  //@{
  /// The class constructor.
  FieldInfo();

  /// The class destructor.
  ~FieldInfo();
  //@}

private:

  fgdbError SetupFieldInfo(IFields* pFields);
  bool      IsSetup();

  IFields*            m_pFields;

  friend class EnumRows;
  friend class Table;
  friend class Row;

  FieldInfo(const FieldInfo&)             { }
  FieldInfo& operator=(const FieldInfo&)  { return *this; }
};

/// A utility class for working with serialized shapes.
class EXT_FILEGDB_API ShapeBuffer
{
public:

  /// Allocates a byte array of the specified size.
  /// @param[in]    length The number of bytes to allocate.
  /// @return       bool Indicates success.
  bool Allocate(size_t length);

  /// @name Constructors and destructors
  //@{
  /// The class constructor.
  ShapeBuffer(size_t length = 0);

  /// The class destructor.
  virtual ~ShapeBuffer();
  //@}

  /// The underlying byte array.
  byte*           shapeBuffer;

  /// The capacity of the byte array.
  size_t          allocatedLength;

  /// The number of bytes being used in the array.
  size_t          inUseLength;

  /// Is the ShapeBuffer empty.
  /// @return       bool Indicates if the shape buffer is empty.
  bool IsEmpty(void) const;

  /// Set the ShapeBuffer empty.
  void SetEmpty(void);

  /// Gets the shape type from the shape buffer.
  /// @param[out]   shapeType The shape type of the buffer. <a href="ShapeTypes.txt">Shape Type</a>
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetShapeType(ShapeType& shapeType) const;

  /// Gets the geometry type which corresponds to the shape type in the shape buffer.
  /// @param[out]   geometryType The geometry type of the shape. <a href="GeometryType.txt">Geometry Type</a>
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetGeometryType(GeometryType& geometryType) const;

  /// Does the shape buffer contain Z values.
  /// @param[in] shapeType The shape type of the buffer. <a href="ShapeTypes.txt">Shape Type</a>
  /// @return    bool Indicates if the shape buffer includes Zs.
  static bool HasZs(ShapeType shapeType);

  /// Does the shape buffer contain Ms.
  /// @param[in] shapeType The shape type of the buffer. <a href="ShapeTypes.txt">Shape Type</a>
  /// @return    bool Indicates if the shape buffer includes Ms.
  static bool HasMs(ShapeType shapeType);

  /// Does the shape buffer contain IDs.
  /// @param[in] shapeType The shape type of the buffer. <a href="ShapeTypes.txt">Shape Type</a>
  /// @return    bool Indicates if the shape buffer includes IDs.
  static bool HasIDs(ShapeType shapeType);

  /// Does the shape buffer contain Curves.
  /// @param[in] shapeType The shape type of the buffer. <a href="ShapeTypes.txt">Shape Type</a>
  /// @return    bool Indicates if the shape buffer includes Curves.
  static bool HasCurves(ShapeType shapeType);

  /// Does the shape buffer includes Normals.
  /// @param[in] shapeType The shape type of the buffer. <a href="ShapeTypes.txt">Shape Type</a>
  /// @return    bool Indicates if the shape buffer includes Normals.
  static bool HasNormals(ShapeType shapeType);

  /// Does the shape buffer include Textures.
  /// @param[in] shapeType The shape type of the buffer. <a href="ShapeTypes.txt">Shape Type</a>
  /// @return    bool Indicates if the shape buffer includes Textures
  static bool HasTextures(ShapeType shapeType);

  /// Does the shape buffer include Materials.
  /// @param[in] shapeType The shape type of the buffer. <a href="ShapeTypes.txt">Shape Type</a>
  /// @return    bool Indicates if the shape buffer includes Materials.
  static bool HasMaterials(ShapeType shapeType);

  /// Gets the geometry type from a shape type.
  /// @param[in]    shapeType The shape type of the buffer. <a href="ShapeTypes.txt">Shape Type</a>
  /// @return       GeometryType The geometry type of the shape. <a href="GeometryType.txt">Geometry Type</a>
  static GeometryType GetGeometryType(ShapeType shapeType);

private:

  ShapeBuffer(const ShapeBuffer&)             { }
  ShapeBuffer& operator=(const ShapeBuffer&)  { return *this; }
};

/// Point Shape Buffer accessor functions.
/// These functions provide access to the shape buffer. Consult the extended
/// shapefile format document for the buffer layout. When reading a point shape
/// buffer you should first create the appropriate shape buffer, get the geometry,
/// get the point, and any z or m values. To write a point create a row buffer,
/// create a shape buffer, set up the shape buffer, get the point,
/// assign the coordinates to the point, assign z and m values if present, set the
/// geometry and insert the row.
class EXT_FILEGDB_API PointShapeBuffer : public ShapeBuffer
{
public:

  /// Get a pointer to the points coordinates.
  /// @param[out]   point A pointer to the coordinate.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetPoint(Point*& point);

  /// Get a pointer to the points z coordinate.
  /// @param[out]   z A pointer to the z value.
  /// @return       Error code indicating whether the method finished successfully..
  fgdbError GetZ(double*& z);

  /// Get a pointer to the points measure.
  /// @param[out]   m A pointer to the m value.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetM(double*& m);

  /// Get a pointer to the points ID value.
  /// @param[out]   id A pointer to the id.
  /// @return       Error code indicating whether the method finished successfully..
  fgdbError GetID(int*& id);

  /// Setup a shape buffer for insert. Allocates the correct length buffer for the selected shape type.
  /// @param[in]    shapeType The shape type of the buffer. <a href="ShapeTypes.txt">Shape Type</a>
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError Setup(ShapeType shapeType);
};

/// MultiPoint Shape Buffer accessor functions.
/// These functions provide access to the shape buffer. Consult the extended shapefile format document
/// for the buffer layout. When reading a multipoint shape buffer you should first create the appropriate
/// shape buffer, get the geometry, get the number of points, get a pointer to the part array, get a pointer
/// to the point array, and, if present, pointers to the z, m, and id arrays. To write a multipoint shape
/// buffer create a row buffer, create a shape buffer, set up the shape buffer, get the
/// points, assign the coordinates to the point array, and if needed get the z, m, id and curve arrays and
/// populate, calculate the extent of the geometry, set the geometry and insert the row. To improve load
/// use Table::LoadOnlyMode. Set it to true before you insert any rows and set it too false when
/// finished. If all grid size values in the spatial index are zero, the values will be calculated based on
/// the existing geometries.
class EXT_FILEGDB_API MultiPointShapeBuffer : public ShapeBuffer
{
public:

  /// Get a pointer to the geomtries extent.
  /// @param[out]   extent A pointer to the geometries extent.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetExtent(double*& extent);

  /// Get the number of coordinates in the geometry.
  /// @param[out]   numPoints The number of points.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetNumPoints(int& numPoints);

  /// Get a pointer to the point array.
  /// @param[out]   points A pointer to the coordinate array.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetPoints(Point*& points);

  /// Get a pointer to the z extent.
  /// @param[out]   zExtent A pointer to the z extent.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetZExtent(double*& zExtent);

  /// Get a pointer to the z array.
  /// @param[out]   zArray A pointer to the z array.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetZs(double*& zArray);

  /// Get a pointer to the m extent.
  /// @param[out]   mExtent A pointer to the m extent.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetMExtent(double*& mExtent);

  /// Get a pointer to the m array.
  /// @param[out]   mArray A pointer to the m array.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetMs(double*& mArray);

  /// Get a pointer to the id array.
  /// @param[out]   ids A pointer to the id array.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetIDs(int*& ids);

  /// Setup a shape buffer for insert. Allocates the correct length buffer for the selected shape type.
  /// @param[in]    shapeType The shape type of the buffer. <a href="ShapeTypes.txt">Shape Type</a>
  /// @param[in]    numPoints The number of points to be loaded.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError Setup(ShapeType shapeType, int numPoints);

  /// Calculates the extent for the shape after all of the coordinate arrays have been filled.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError CalculateExtent(void);
};

/// MultiPart Shape Buffer accessor functions.
/// These functions provide access to the shape buffer. Consult the extended shapefile
/// format document for the buffer layout. When reading a multipart shape buffer you
/// should first create the appropriate shape buffer, get the geometry, get the number
/// of points, get the number of parts, get the number of curves, get a pointer to the
/// part array, get a pointer to the point array, and, if present pointers to the z, m,
/// and id arrays. To write a multipart shape buffer create a row buffer, create a shape
/// buffer, set up the shape buffer, get the points, assign the coordinates
/// to the point array, get the part array and populate the array as needed, and if needed
/// get the z, m, id and curve arrays and populate, calculate the extent of the geometry,
/// set the geometry and insert the row. If curves were added Pact the curves before setting
/// the geometry. To improve load use Table::LoadOnlyMode. Set it to true before you insert
/// any rows and set it too false when finished. If all grid size values in the spatial index
/// are zero, the values will be calculated based on the existing geometries.
class EXT_FILEGDB_API MultiPartShapeBuffer : public ShapeBuffer
{
public:

  /// Get a pointer to the geomtries extent.
  /// @param[out]   extent A pointer to the geometries extent.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetExtent(double*& extent);

  /// Get the number of parts.
  /// @param[out]   numParts The number of parts in the geometry.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetNumParts(int& numParts);

  /// Get the number of coordinates in the geometry.
  /// @param[out]   numPoints The number of points.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetNumPoints(int& numPoints);

  /// Get a pointer to the parts array.
  /// @param[out]   parts A pointer to the parts array.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetParts(int*& parts);

  /// Get a pointer to the point array.
  /// @param[out]   points A pointer to the point array.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetPoints(Point*& points);

  /// Get a pointer to the z extent.
  /// @param[out]   zExtent A pointer to the z extent.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetZExtent(double*& zExtent);

  /// Get a pointer to the z array.
  /// @param[out]   zArray A pointer to the z array.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetZs(double*& zArray);

  /// Get a pointer to the m extent.
  /// @param[out]   mExtent A pointer to the m extent.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetMExtent(double*& mExtent);

  /// Get a pointer to the m array.
  /// @param[out]   mArray A pointer to the m array.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetMs(double*& mArray);

  /// Get the number of curves in the geometry.
  /// @param[out]   numCurves The number of curves.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetNumCurves(int& numCurves);

  /// Get a pointer to the curve array.
  /// @param[out]   curves A pointer to the curve array.
  /// @return       Error code indicating whether the method finished successfully.s.
  fgdbError GetCurves(byte*& curves);

  /// Get a pointer to the id array.
  /// @param[out]    ids A pointer to the id array.
  /// @return       Error code indicating whether the method finished successfully..
  fgdbError GetIDs(int*& ids);

  /// Setup a shape buffer for insert. Allocates the correct length buffer for the selected shape type.
  /// @param[in]    shapeType The shape type of the buffer. <a href="ShapeTypes.txt">Shape Type</a>
  /// @param[in]    numParts The number of parts that the geometry will contain.
  /// @param[in]    numPoints The number of points that the geometry will contain.
  /// @param[in]    numCurves The number of curves that the geometry will contain, defaults to zero.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError Setup(ShapeType shapeType, int numParts, int numPoints, int numCurves = 0);

  /// Calculates the extent for the shape after all of the coordinate arrays have been filled.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError CalculateExtent(void);

  /// Remove excess space allocated for curves. Setup allocates space for curves based of the max size that could be
  /// required. Depending on the curves loaded, all of the allocated space may not be required.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError PackCurves(void);
};

/// MultiPatch Shape Buffer accessor functions.
/// These functions provide access to the shape buffer. Consult the extended shapefile format document
/// for the buffer layout.
class EXT_FILEGDB_API MultiPatchShapeBuffer : public ShapeBuffer
{
public:

  /// Get a pointer to the geomtries extent.
  /// @param[out]   extent A pointer to the geometries extent.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetExtent(double*& extent);

  /// Get the number of parts.
  /// @param[out]   numParts The number of parts in the geometry.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetNumParts(int& numParts);

  /// Get the number of coordinates in the geometry.
  /// @param[out]   numPoints The number of points.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetNumPoints(int& numPoints);

  /// Get a pointer to the parts array.
  /// @param[out]   parts A pointer to the parts array.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetParts(int*& parts);

  /// Get a pointer to the part descriptor array.
  /// @param[out]   partDescriptorArray A pointer to the part descriptor array.
  /// @return       Error code indicating whether the method finished successfully
  fgdbError GetPartDescriptors(int*& partDescriptorArray);

  /// Get a pointer to the point array.
  /// @param[out]   points A pointer to the point array.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetPoints(Point*& points);

  /// Get a pointer to the z extent.
  /// @param[out]   zExtent A pointer to the z extent.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetZExtent(double*& zExtent);

  /// Get a pointer to the z array.
  /// @param[out]   zArray A pointer to the z array.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetZs(double*& zArray);

  /// Get a pointer to the m extent.
  /// @param[out]   mExtent A pointer to the m extent.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetMExtent(double*& mExtent);

  /// Get a pointer to the m array.
  /// @param[out]   mArray A pointer to the m array.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetMs(double*& mArray);

  /// Get a pointer to the id array.
  /// @param[out]   ids A pointer to the id array.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetIDs(int*& ids);

  /// Get a pointer to the normals array.
  /// @param[out]   normals A pointer to the normals array.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetNormals(float*& normals);

  /// Returns textures.
  /// @param[out]   numTextures The number of textures
  /// @param[out]   textureDimension The texture dimension.
  /// @param[out]   textureParts A pointer to the texture parts. 
  /// @param[out]   textureCoords A pointer to the texture coordinates. 
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetTextures(int& numTextures, int& textureDimension, int*& textureParts, float*& textureCoords);

  /// Returns materials.
  /// @param[out]   numMaterials The number of materials
  /// @param[out]   compressionType The compression type.
  /// @param[out]   materialParts A pointer to the number of material parts. 
  /// @param[out]   materials A pointer to the materials array. 
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError GetMaterials(int& numMaterials, int& compressionType, int*& materialParts, byte*& materials);

  /// Setup a shape buffer for insert. Allocates the correct length buffer for the selected shape type.
  /// @param[in]    shapeType The shape type of the buffer. <a href="ShapeTypes.txt">Shape Type</a>
  /// @param[in]    numParts The number of parts that the geometry will contain.
  /// @param[in]    numPoints The number of points that the geometry will contain.
  /// @param[in]    numTextures The number of textures that the geometry will contain, defaults to zero.
  /// @param[in]    textureDimension The textureDimension that the geometry will contain, defaults to zero.
  /// @param[in]    numMaterials The number of materials that the geometry will contain, defaults to zero.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError Setup(ShapeType shapeType, int numParts, int numPoints, int numTextures = 0,
                  int textureDimension = 0, int numMaterials = 0);

  /// Calculates the extent for the shape after all of the coordinate arrays have been filled.
  /// @return       Error code indicating whether the method finished successfully.
  fgdbError CalculateExtent(void);
};

/// A utility class for working with database BLOBs.
class EXT_FILEGDB_API ByteArray
{
public:

  /// Allocates a byte array of the specified size.
  /// @param[in]    length The number of bytes to allocate.
  /// @return       bool Indicates success.
  bool Allocate(size_t length);

  /// @name Constructors and destructors
  //@{
  /// The class constructor.
  ByteArray(size_t length = 0);

  /// The class destructor.
  ~ByteArray();
  //@}

  /// The underlying byte array.
  byte*           byteArray;

  /// The capacity of the byte array.
  size_t          allocatedLength;

  /// The number of bytes being used in the array.
  size_t          inUseLength;

private:

  ByteArray(const ByteArray&)             { }
  ByteArray& operator=(const ByteArray&)  { return *this; }
};

/// Defines an XY spatial extent.
class EXT_FILEGDB_API Envelope
{
public:

  /// Indicates whether the envelope's attributes have been set.
  /// @return       True if one or more attributes are NaN, false otherwise.
  bool IsEmpty();

  /// Sets the envelope's attributes to NaN.
  /// @return       Void.
  void SetEmpty();

  /// @name Constructors and destructors
  //@{
  /// The class constructors.
  Envelope();
  Envelope(double xmin, double xmax, double ymin, double ymax);

  /// The class destructor.
  ~Envelope();
  //@}

  /// The lower X boundary of the envelope.
  double          xMin;

  /// The lower Y boundary of the envelope.
  double          yMin;

  /// The upper X boundary of the envelope.
  double          xMax;

  /// The upper Y boundary of the envelope.
  double          yMax;

  /// The lower Z boundary of the envelope.
  double          zMin;

  /// The upper Z boundary of the envelope.
  double          zMax;
};

class EXT_FILEGDB_API Point
{
public:

  double          x;
  double          y;
};

class EXT_FILEGDB_API Guid
{
public:

  Guid();
  ~Guid();

  void      SetNull(void);
  void      Create(void);

  fgdbError FromString(const std::wstring& guidString);
  fgdbError ToString(std::wstring& guidString);

  bool operator==(const Guid& other);
  bool operator!=(const Guid& other);

  uint32    data1;
  uint16    data2;
  uint16    data3;
  byte      data4[8];
};

/// Provides access to error text and extended error information.
namespace ErrorInfo
{
  /// Returns the text error message which corresponds to an error code.
  /// If there is no description corresponding to the error code, the error
  /// description string will be empty and a 1 (S_FALSE) error will be returned.
  /// @param[in]    fgdbError The error code to look up.
  /// @param[out]   errorDescription The description of the error.
  /// @return       Error code indicating whether the method finished successfully.
  EXT_FILEGDB_API fgdbError GetErrorDescription(fgdbError fgdbError, std::wstring& errorDescription);

  /// Returns the number of error records in the error stack.
  /// @param[out]   recordCount The number of error records.
  /// @return       Void.
  EXT_FILEGDB_API void GetErrorRecordCount(int& recordCount);

  /// Returns an error record.
  /// @param[in]    recordNum The error record to return.
  /// @param[out]   fgdbError The error code.
  /// @param[out]   errorDescription The description of the error.
  /// @return       Error code indicating whether the method finished successfully.
  EXT_FILEGDB_API fgdbError GetErrorRecord(int recordNum, fgdbError& fgdbError, std::wstring& errorDescription);

  /// Clears the error stack.
  /// @return       Void.
  EXT_FILEGDB_API void ClearErrors(void);
};

struct SpatialReferenceInfo
{
  std::wstring  auth_name;    // The name of the standard or standards body that is being cited for this reference system.
  int           auth_srid;    // The ID of the Spatial Reference System as defined by the Authority cited in AUTH_NAME.
  std::wstring  srtext;       // The Well-known Text Representation of the Spatial Reference System.
  std::wstring  srname;       // The name of the Spatial Reference System.
};

class EXT_FILEGDB_API EnumSpatialReferenceInfo
{
public:

  EnumSpatialReferenceInfo();
  ~EnumSpatialReferenceInfo();

  bool NextGeographicSpatialReference(SpatialReferenceInfo& spatialReferenceInfo);
  bool NextProjectedSpatialReference(SpatialReferenceInfo& spatialReferenceInfo);
  void Reset();

private:

  int           m_currentGCS;
  int           m_currentPCS;
};

///@ SpatialReferences
namespace SpatialReferences
{
  /// Returns information about a spatial reference given its AUTH_SRID.
  /// @param[in]    auth_srid The AUTH_SRID of the spatial reference to find.
  /// @param[out]   spatialRef The properties of the requested spatial reference.
  /// @return       Error code indicating whether the method finished successfully.
  EXT_FILEGDB_API bool FindSpatialReferenceBySRID(int auth_srid, SpatialReferenceInfo& spatialRef);

  /// Returns information about a spatial reference given its name.
  /// @param[in]    srname The name of the spatial reference to find.
  /// @param[out]   spatialRef The properties of the requested spatial reference.
  /// @return       Error code indicating whether the method finished successfully.
  EXT_FILEGDB_API bool FindSpatialReferenceByName(const std::wstring& srname, SpatialReferenceInfo& spatialRef);
};
//@

};  // namespace FileGDBAPI
