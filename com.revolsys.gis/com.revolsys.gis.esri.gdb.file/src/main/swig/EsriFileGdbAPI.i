%module "EsriFileGdb"

%{
#include "FileGDBAPI.h"
%}

%include "std_vector.i"
%include "std_string.i"
%include "std_wstring.i"
%include "typemaps.i"
%include "enums.swg"
%include "OutParam.i"
%include "OutArrayParam.i"
%javaconst(1);

%template(VectorOfString) std::vector<std::string>;
%template(VectorOfWString) std::vector<std::wstring>;

%include "arrays_java.i"
%apply int[] {int []};
%apply int[] {int *};
%apply float[] {float *};
%apply double[] {double *};

OUT_PARAM(bool, BoolValue)
OUT_PARAM(double, DoubleValue)
OUT_PARAM(float, FloatValue)
OUT_PARAM(int, IntValue)
OUT_PARAM(short, ShortValue)
OUT_PARAM(std::string, StringValue)
OUT_PARAM(std::wstring, WStringValue)
OUT_PARAM(FileGDBAPI::FieldType, FieldTypeValue)
OUT_PARAM(FileGDBAPI::GeometryType, GeometryTypeValue)
OUT_PARAM(FileGDBAPI::ShapeType, ShapeTypeValue)

OUT_ARRAY_PARAM(float, FloatArrayValue)
OUT_ARRAY_PARAM(double, DoubleArrayValue)
OUT_ARRAY_PARAM(int, IntArrayValue)
OUT_ARRAY_PARAM(unsigned char, UnsignedCharArrayValue)


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

%ignore FileGDBAPI::ByteArray::byteArray;
%extend FileGDBAPI::ByteArray {
  unsigned char get(int i) {
    return $self->byteArray[i];
  }

  void set(int i, unsigned char c) {
    $self->byteArray[i] = c;
  }
}

%ignore FileGDBAPI::ShapeBuffer::shapeBuffer;
%extend FileGDBAPI::ShapeBuffer {
  unsigned char get(int i) {
    return $self->shapeBuffer[i];
  }

  void set(int i, unsigned char c) {
    $self->shapeBuffer[i] = c;
  }
}

%include "Util.h"

%include "Raster.h"

%inline %{
  std::wstring getErrorDescription(fgdbError hr) {
    std::wstring errorDescription;
    FileGDBAPI::ErrorInfo::GetErrorDescription(hr, errorDescription);
    return errorDescription;
  }
%}


