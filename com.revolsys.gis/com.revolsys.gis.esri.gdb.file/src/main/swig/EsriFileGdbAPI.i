%module "EsriFileGdb"

%include "std_vector.i"
%include "std_string.i"
%include "std_wstring.i"
%include "typemaps.i"
%include "enums.swg"
%include "arrays_java.i"
%javaconst(1);

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
%apply int &OUTPUT { int &ids };
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

%apply int[] {int *};
%apply float[] {float *};

%define linux
%enddef

%{
#include "FileGDBAPI.h"
%}

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

  std::wstring getWstring(std::wstring& string) {
    return string;
  }
  
  std::vector<std::wstring> getVectorWstring(std::vector<std::wstring>& vector) {
    return vector;
  }

  std::string getString(std::string& string) {
    return string;
  }
  
  std::vector<std::string> getVectorString(std::vector<std::string>& vector) {
    return vector;
  }
%}
