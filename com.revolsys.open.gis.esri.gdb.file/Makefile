# requires the following libraries to be installed
# /usr/lib/libfgdbunixrtl.so
#/usr/lib/libFileGDBAPI.so

ESRI_FILE_GBD_HOME=/opt/EsriFileGdb-1.2/
ESRI_FILE_GBD_INCLUDE=/opt/EsriFileGdb-1.2/include
ifndef OS
	OS := $(shell uname)
endif

all: clean swig libs
	
clean:
	rm -rf target/o
	rm -rf target/cxx
	
init:
	mkdir -p target/cxx
	mkdir -p target/o

swig: init src/main/swig/EsriFileGdbAPI.i
	mkdir -p src/main/java/com/revolsys/gis/esri/gdb/file/capi/swig/
	rm -f src/main/java/com/revolsys/gis/esri/gdb/file/capi/swig/*.java
	swig -c++ \
		-o target/cxx/EsriFileGdb_wrap.cxx \
		-java \
		-package com.revolsys.gis.esri.gdb.file.capi.swig \
		-outdir src/main/java/com/revolsys/gis/esri/gdb/file/capi/swig \
		-Isrc/main/swig \
		-I${ESRI_FILE_GBD_INCLUDE}/ \
		src/main/swig/EsriFileGdbAPI.i

libs:
ifeq "$(OS)" "Darwin"
	make -f os-arch.mk OS=macosx ARCH=x86_64
else 
	make -f os-arch.mk OS=linux ARCH=x86
	make -f os-arch.mk OS=linux ARCH=x86_64
endif
