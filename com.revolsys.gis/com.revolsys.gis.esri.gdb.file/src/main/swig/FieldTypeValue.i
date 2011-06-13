%inline %{
typedef struct {
    FileGDBAPI::FieldType value;
} FieldTypeValue;
%}

%typemap(in) FileGDBAPI::FieldType &OUTVALUE {
    jclass clazz = jenv->FindClass("FieldTypeValue");
    jfieldID fid = jenv->GetFieldID(clazz, "swigCPtr", "J");
    jlong cPtr = jenv->GetLongField($input, fid);
    FieldTypeValue *pFieldTypeValue = NULL;
    *(FieldTypeValue **)&pFieldTypeValue = *(FieldTypeValue **)&cPtr;
    $1 = &pFieldTypeValue->value;
}

%typemap(jtype) FileGDBAPI::FieldType &OUTVALUE "FieldTypeValue"
%typemap(jstype) FileGDBAPI::FieldType &OUTVALUE "FieldTypeValue"
%typemap(jni) FileGDBAPI::FieldType &OUTVALUE "jobject"

%typemap(javain) FileGDBAPI::FieldType &OUTVALUE "$javainput"
