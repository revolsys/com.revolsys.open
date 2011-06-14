%inline %{
typedef struct {
    std::string value;
} StringValue;
%}

%typemap(in) std::string &OUTVALUE {
    jclass clazz = jenv->FindClass("StringValue");
    jfieldID fid = jenv->GetFieldID(clazz, "swigCPtr", "J");
    jlong cPtr = jenv->GetLongField($input, fid);
    StringValue *pStringValue = NULL;
    *(StringValue **)&pStringValue = *(StringValue **)&cPtr;
    $1 = &pStringValue->value;
}

%typemap(jtype) std::string &OUTVALUE "StringValue"
%typemap(jstype) std::string &OUTVALUE "StringValue"
%typemap(jni) std::string &OUTVALUE "jstring"

%typemap(javain) std::string &OUTVALUE "$javainput"
