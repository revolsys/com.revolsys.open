/* StringBuffer */
%typemap(jni) std::wstring & SBUF "jobject"

%typemap(jtype) std::wstring & SBUF "StringBuffer"
%typemap(jstype) std::wstring & SBUF "StringBuffer"

%typemap(in) std::wstring & SBUF {
  jclass sbufClass;
  jmethodID toStringID;
    jmethodID setLengthID;
  jstring js;
  
  $1 = NULL;
  if ($input != NULL) {
    /* get the String from the StringBuffer */
    sbufClass = jenv->GetObjectClass($input);
    toStringID = jenv->GetMethodID(sbufClass, "toString", "()Ljava/lang/String;");
    js = (jstring) jenv->CallObjectMethod( $input, toStringID);

    /* convert the String to a char * */
    //$1 = (char *)jenv->GetStringUTFChars(js, 0); 

    // Convert the String to std::wstring
    const char * pCharStr = (const char *)jenv->GetStringChars(js, 0);
    $1 = new std::wstring(pCharStr);

    // Release the UTF wstring we created in the JVM with
    GetStringUTFChars;
    // the std::wstring we just allocated now has a copy of it.
    jenv->ReleaseStringUTFChars( js, pCharStr );

    /* zero the original StringBuffer, so we can replace it with the result */
    setLengthID = jenv->GetMethodID( sbufClass, "setLength", "(I)V");
    jenv->CallVoidMethod( $input, setLengthID, (jint) 0);
  }
}

/* how to convert the C++ type to the Java type */
%typemap(argout) std::wstring & SBUF {
  jclass sbufClass;
  jmethodID appendStringID;

  if ($1 != NULL) {
    /* append the result to the empty StringBuffer */
    sbufClass = jenv->GetObjectClass( $input);
    appendStringID = jenv->GetMethodID( sbufClass, "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
    jenv->CallObjectMethod( $input, appendStringID, jenv->NewString( $1->c_str()));

    // Clean up the std::wstring object, no longer needed.
    delete $1;
    $1 = NULL;
  }
}

/* Prevent the default freearg typemap from being used */
%typemap(freearg) std::wstring & SBUF ""

/* StringBuffer */
%typemap(jni) std::string & SBUF "jobject"

%typemap(jtype) std::string & SBUF "StringBuffer"
%typemap(jstype) std::string & SBUF "StringBuffer"

%typemap(in) std::string & SBUF {
  jclass sbufClass;
  jmethodID toStringID;
    jmethodID setLengthID;
  jstring js;
  
  $1 = NULL;
  if ($input != NULL) {
    /* get the String from the StringBuffer */
    sbufClass = jenv->GetObjectClass($input);
    toStringID = jenv->GetMethodID(sbufClass, "toString", "()Ljava/lang/String;");
    js = (jstring) jenv->CallObjectMethod( $input, toStringID);

    /* convert the String to a char * */
    //$1 = (char *)jenv->GetStringUTFChars(js, 0); 

    // Convert the String to std::string
    const char * pCharStr = (const char *)jenv->GetStringUTFChars(js, 0);
    $1 = new std::string(pCharStr);

    // Release the UTF string we created in the JVM with
    GetStringUTFChars;
    // the std::string we just allocated now has a copy of it.
    jenv->ReleaseStringUTFChars( js, pCharStr );

    /* zero the original StringBuffer, so we can replace it with the result */
    setLengthID = jenv->GetMethodID( sbufClass, "setLength", "(I)V");
    jenv->CallVoidMethod( $input, setLengthID, (jint) 0);
  }
}

/* how to convert the C++ type to the Java type */
%typemap(argout) std::string & SBUF {
  jclass sbufClass;
  jmethodID appendStringID;

  if ($1 != NULL) {
    /* append the result to the empty StringBuffer */
    sbufClass = jenv->GetObjectClass( $input);
    appendStringID = jenv->GetMethodID( sbufClass, "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
    jenv->CallObjectMethod( $input, appendStringID, jenv->NewStringUTF( $1->c_str()));

    // Clean up the std::string object, no longer needed.
    delete $1;
    $1 = NULL;
  }
}

/* Prevent the default freearg typemap from being used */
%typemap(freearg) std::string & SBUF ""
