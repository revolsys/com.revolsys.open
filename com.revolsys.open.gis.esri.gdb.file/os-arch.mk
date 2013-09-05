# requires the following libraries to be installed
# /usr/lib/libfgdbunixrtl.so
#/usr/lib/libFileGDBAPI.so

ESRI_FILE_GBD_HOME=/opt/EsriFileGdb/1.3/${OS}/${ARCH}
ESRI_FILE_GBD_INCLUDE=/opt/EsriFileGdb/1.3/${OS}/${ARCH}/include

CFG=Release

include ${ESRI_FILE_GBD_INCLUDE}/make.include
TARGET_OBJ=target/o/libEsriFileGdbJni-${ARCH}-${OS}.o
TARGET_LIB=src/main/resources/native/libEsriFileGdbJni-${ARCH}-${OS}.${EXT}

all: clean ${TARGET_LIB}
	
clean:
	rm -f ${TARGET_OBJ} ${TARGET_LIB}

src/main/cxx/EsriFileGdb_wrap.cxx:

${TARGET_OBJ}: src/main/cxx/EsriFileGdb_wrap.cxx
	mkdir -p target/o
	${CXX} \
		${CXXFLAGS} \
		-I${ESRI_FILE_GBD_INCLUDE} \
		-I$JAVA_HOME/include/ \
		-I/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers/ \
		-c src/main/cxx/EsriFileGdb_wrap.cxx \
		-o ${TARGET_OBJ}
	

${TARGET_LIB}: target/o/libEsriFileGdbJni-${ARCH}-${OS}.o
	${CXX} \
		${LDFLAGS} \
		-O2 \
		-fpic \
		-shared \
		-lFileGDBAPI \
		-lfgdbunixrtl \
		-L${ESRI_FILE_GBD_HOME}/lib/ \
		${TARGET_OBJ} \
		-o ${TARGET_LIB}
	

