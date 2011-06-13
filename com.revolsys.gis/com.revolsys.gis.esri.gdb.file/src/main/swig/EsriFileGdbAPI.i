%module "EsriFileGdb"

%include "std_vector.i"
%include "std_string.i"
%include "std_wstring.i"
%include "typemaps.i"
%include "enums.swg"
%javaconst(1);

%apply int &OUTPUT { int &fieldCount };
%apply int &OUTPUT { int &recordCount };
%apply int &OUTPUT { int &fgdbError };
%apply int &OUTPUT { int &fieldLength };


%apply bool &INOUT { bool &result };
%apply bool *OUTPUT { bool *result };
%apply double *INOUT { double & result };

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
