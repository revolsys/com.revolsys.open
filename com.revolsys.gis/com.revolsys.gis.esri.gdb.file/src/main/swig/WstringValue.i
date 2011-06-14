%inline %{
typedef struct {
    std::wstring value;
} WstringValue;
%}

%typemap(in) std::wstring &OUTVALUE {
    jclass clazz = jenv->FindClass("WstringValue");
    jfieldID fid = jenv->GetFieldID(clazz, "swigCPtr", "J");
    jlong cPtr = jenv->GetLongField($input, fid);
    WstringValue *pWstringValue = NULL;
    *(WstringValue **)&pWstringValue = *(WstringValue **)&cPtr;
    $1 = &pWstringValue->value;
}

%typemap(jtype) std::wstring &OUTVALUE "WstringValue"
%typemap(jstype) std::wstring &OUTVALUE "WstringValue"
%typemap(jni) std::wstring &OUTVALUE "jstring"

%typemap(javain) std::wstring &OUTVALUE "$javainput"
