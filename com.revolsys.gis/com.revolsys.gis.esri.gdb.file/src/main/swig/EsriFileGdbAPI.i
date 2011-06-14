%module "EsriFileGdb"

%{
#include "FileGDBAPI.h"
%}

%include "std_vector.i"
%include "std_string.i"
%include "std_wstring.i"
%include "typemaps.i"
%include "enums.swg"
%include "FieldTypeValue.i"
%include "GeometryTypeValue.i"
%include "ShapeTypeValue.i"
%include "StringValue.i"
%include "WstringValue.i"
%javaconst(1);

%include "carrays.i"
%array_class(int, IntArray);
%array_class(float, FloatArray);
%array_class(double, DoubleArray);
%array_class(unsigned char, UnsignedCharArray);

%apply bool &OUTPUT { bool &isEditable };
%apply bool &OUTPUT { bool &isNullable };
%apply bool &OUTPUT { bool &isNull };


%apply int &OUTPUT { int &fieldCount };
%apply int &OUTPUT { int &recordCount };
%apply int &OUTPUT { int &fgdbError };
%apply int &OUTPUT { int &fieldLength };
%apply int &OUTPUT { int &defaultCode };
%apply int &OUTPUT { int &rowCount };
%apply short &OUTPUT { short &value };
%apply float &OUTPUT { float &value };
%apply double &OUTPUT { double &value };
%apply int &OUTPUT { int &value };

%apply int &OUTPUT { int &numParts };
%apply int &OUTPUT { int &numPoints };
%apply int &OUTPUT { int &parts };
%apply int &OUTPUT { int &numCurves };
%apply int &OUTPUT { int &parts };
%apply int &OUTPUT { int &partDescriptorArray };
%apply int &OUTPUT { int &numTextures };
%apply int &OUTPUT { int &textureDimension };
%apply int &OUTPUT { int &textureParts };
%apply float &OUTPUT { float &textureCoords };
%apply int &OUTPUT { int &numMaterials };
%apply int &OUTPUT { int &compressionType };
%apply int &OUTPUT { int &materialParts };
%apply unsigned char &OUTPUT { unsigned char &materials };
%apply int &OUTPUT { int &id };
%apply int &OUTPUT { int &objectID };

%apply FileGDBAPI::FieldType &OUTVALUE { FileGDBAPI::FieldType &fieldType };
%apply FileGDBAPI::GeometryType &OUTVALUE { FileGDBAPI::GeometryType &geometryType };
%apply FileGDBAPI::ShapeType &OUTVALUE { FileGDBAPI::ShapeType &shapeType };

%apply std::string &OUTVALUE { std::string &datasetDef };
%apply std::string &OUTVALUE { std::string &documentation };
%apply std::string &OUTVALUE { std::string &domainDef };
%apply std::string &OUTVALUE { std::string &value };
%apply std::string &OUTVALUE { std::string &tableDef };

%apply std::wstring &OUTVALUE { std::wstring &fieldName };
%apply std::wstring &OUTVALUE { std::wstring &guidString };
%apply std::wstring &OUTVALUE { std::wstring &queryName };
%apply std::wstring &OUTVALUE { std::wstring &errorDescription };
%apply std::wstring &OUTVALUE { std::wstring &value };

%define linux
%enddef

%include "FileGDBCore.h"
%include "GeodatabaseManagement.h"
%rename(createGeodatabase) FileGDBAPI::CreateGeodatabase;
%rename(openGeodatabase) FileGDBAPI::OpenGeodatabase;
%rename(closeGeodatabase) FileGDBAPI::CloseGeodatabase;
%rename(deleteGeodatabase) FileGDBAPI::DeleteGeodatabase;
%include "Geodatabase.h"
%include "Table.h"
%include "Row.h"
%rename(equal) FileGDBAPI::Guid::operator==;
%rename(notEqual) FileGDBAPI::Guid::operator!=;
%include "Util.h"
%include "Raster.h"

%inline %{
  std::wstring getErrorDescription(fgdbError hr) {
    std::wstring errorDescription;
    FileGDBAPI::ErrorInfo::GetErrorDescription(hr, errorDescription);
    return errorDescription;
  }
%}
