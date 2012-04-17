# requires the following libraries to be installed
# /usr/lib/libfgdbunixrtl.so
#/usr/lib/libFileGDBAPI.so

ESRI_FILE_GBD_HOME=/opt/EsriFileGdb-1.2/
ESRI_FILE_GBD_INCLUDE=/opt/EsriFileGdb-1.2/include

CFG=Debug

include ${ESRI_FILE_GBD_INCLUDE}/make.include
TARGET_OBJ=target/o/libEsriFileGdbJni-${ARCH}-${OS}.o
TARGET_LIB=src/main/resources/native/libEsriFileGdbJni-${ARCH}-${OS}.${SLIBEXT}

all: clean ${TARGET_LIB}
	
clean:
	rm -f ${TARGET_OBJ} ${TARGET_LIB}

target/cxx/EsriFileGdb_wrap.cxx:

${TARGET_OBJ}: target/cxx/EsriFileGdb_wrap.cxx
	mkdir -p target/o
	${CXX} \
		${CXXFLAGS} \
		-I${ESRI_FILE_GBD_INCLUDE} \
		-I$JAVA_HOME/include/ \
		-I/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers/ \
		-c target/cxx/EsriFileGdb_wrap.cxx \
		-o ${TARGET_OBJ}
	

${TARGET_LIB}: target/o/libEsriFileGdbJni-${ARCH}-${OS}.o
	${CXX} \
		${LDFLAGS} \
		-O2 \
		-fpic \
		-shared \
		-lFileGDBAPI \
		-lfgdbunixrtl \
		-L${ESRI_FILE_GBD_HOME}/lib/${OS}/${ARCH} \
		${TARGET_OBJ} \
		-o ${TARGET_LIB}
	

