%inline %{
template<class T> class OutArrayParamValue {
  private:
    T* value;
  public:
    T get(int i) {
      return this->value[i];
    };
    void set(int i, T value) {
      this->value[i] = value;
    };
 
    T* getOutParamArrayValue() {
      return this->value;
    };
};
%}
%ignore *::getOutParamArrayValue;

%define OUT_ARRAY_PARAM(C_TYPE, WRAPPER_TYPE)

%template(WRAPPER_TYPE) OutArrayParamValue<C_TYPE>;

%typemap(in) C_TYPE* &OUTVALUE {
    jclass clazz = jenv->GetObjectClass($input);
    jfieldID cPointerField = jenv->GetFieldID(clazz, "swigCPtr", "J");
    jlong cPtr = jenv->GetLongField($input, cPointerField);
    OutArrayParamValue<C_TYPE> *wrapper = NULL;
    *(OutArrayParamValue<C_TYPE> **)&wrapper = *(OutArrayParamValue<C_TYPE> **)&cPtr;
    C_TYPE* value = wrapper->getOutParamArrayValue();
    $1 = &value;
}

%typemap(jtype) C_TYPE* &OUTVALUE "WRAPPER_TYPE"
%typemap(jstype) C_TYPE* &OUTVALUE "WRAPPER_TYPE"
%typemap(jni) C_TYPE* &OUTVALUE "jobject"

%typemap(javain) C_TYPE* &OUTVALUE "$javainput"

%apply C_TYPE* &OUTVALUE { C_TYPE*&};

%enddef
