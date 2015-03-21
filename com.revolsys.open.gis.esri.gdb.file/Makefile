# requires the following libraries to be installed
# /usr/lib/libfgdbunixrtl.so
#/usr/lib/libFileGDBAPI.so


ifeq "$(shell uname)" "Darwin"
  OS=macosx
else 
  OS=linux
endif

ESRI_FILE_GBD_HOME=/opt/EsriFileGdb/1.4/macosx/x86_64
ESRI_FILE_GBD_INCLUDE=/opt/EsriFileGdb/1.4/macosx/x86_64/include

all: clean swig libs
	
clean:
	rm -rf target/o
	rm -rf src/main/cxx
	
init:
	mkdir -p src/main/cxx
	mkdir -p target/o

swig: init src/main/swig/EsriFileGdbAPI.i
	mkdir -p src/main/java/com/revolsys/gis/esri/gdb/file/capi/swig/
	rm -f src/main/java/com/revolsys/gis/esri/gdb/file/capi/swig/*.java
	swig -c++ \
		-o src/main/cxx/EsriFileGdb_wrap.cxx \
		-java \
		-package com.revolsys.gis.esri.gdb.file.capi.swig \
		-outdir src/main/java/com/revolsys/gis/esri/gdb/file/capi/swig \
		-Isrc/main/swig \
		-I${ESRI_FILE_GBD_INCLUDE}/ \
		src/main/swig/EsriFileGdbAPI.i

libs:
ifeq "$(OS)" "macosx"
	make -f os-arch.mk OS=${OS} ARCH=x86_64 EXT=dylib
else 
	make -f os-arch.mk OS=${OS} ARCH=x86 EXT=o
	make -f os-arch.mk OS=${OS} ARCH=x86_64 EXT=o
endif
