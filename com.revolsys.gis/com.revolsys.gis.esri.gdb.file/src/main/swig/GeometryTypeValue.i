%inline %{
typedef struct {
    FileGDBAPI::GeometryType value;
} GeometryTypeValue;
%}

%typemap(in) FileGDBAPI::GeometryType &OUTVALUE {
    jclass clazz = jenv->FindClass("GeometryTypeValue");
    jfieldID fid = jenv->GetFieldID(clazz, "swigCPtr", "J");
    jlong cPtr = jenv->GetLongField($input, fid);
    GeometryTypeValue *pGeometryTypeValue = NULL;
    *(GeometryTypeValue **)&pGeometryTypeValue = *(GeometryTypeValue **)&cPtr;
    $1 = &pGeometryTypeValue->value;
}

%typemap(jtype) FileGDBAPI::GeometryType &OUTVALUE "GeometryTypeValue"
%typemap(jstype) FileGDBAPI::GeometryType &OUTVALUE "GeometryTypeValue"
%typemap(jni) FileGDBAPI::GeometryType &OUTVALUE "jobject"

%typemap(javain) FileGDBAPI::GeometryType &OUTVALUE "$javainput"
