%inline %{
typedef struct {
    FileGDBAPI::ShapeType value;
} ShapeTypeValue;
%}

%typemap(in) FileGDBAPI::ShapeType &OUTVALUE {
    jclass clazz = jenv->FindClass("ShapeTypeValue");
    jfieldID fid = jenv->GetFieldID(clazz, "swigCPtr", "J");
    jlong cPtr = jenv->GetLongField($input, fid);
    ShapeTypeValue *pShapeTypeValue = NULL;
    *(ShapeTypeValue **)&pShapeTypeValue = *(ShapeTypeValue **)&cPtr;
    $1 = &pShapeTypeValue->value;
}

%typemap(jtype) FileGDBAPI::ShapeType &OUTVALUE "ShapeTypeValue"
%typemap(jstype) FileGDBAPI::ShapeType &OUTVALUE "ShapeTypeValue"
%typemap(jni) FileGDBAPI::ShapeType &OUTVALUE "jobject"

%typemap(javain) FileGDBAPI::ShapeType &OUTVALUE "$javainput"
