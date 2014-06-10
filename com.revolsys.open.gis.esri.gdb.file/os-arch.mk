# requires the following libraries to be installed
# /usr/lib/libfgdbunixrtl.so
#/usr/lib/libFileGDBAPI.so

ESRI_FILE_GBD_HOME=/opt/EsriFileGdb/1.3/${OS}/${ARCH}
ESRI_FILE_GBD_INCLUDE=/opt/EsriFileGdb/1.3/${OS}/${ARCH}/include
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home
CFG=Release
CXX=clang++

include ${ESRI_FILE_GBD_INCLUDE}/make.include
TARGET_OBJ=target/o/libEsriFileGdbJni-${ARCH}-${OS}.o
TARGET_DIR=src/main/resources/native/${OS}/${ARCH}
TARGET_LIB=${TARGET_DIR}/libEsriFileGdbJni.${EXT}

all: clean ${TARGET_LIB}
	
clean:
	rm -f ${TARGET_OBJ} ${TARGET_LIB}

src/main/cxx/EsriFileGdb_wrap.cxx:

${TARGET_OBJ}: src/main/cxx/EsriFileGdb_wrap.cxx
	mkdir -p target/o
	clang++ \
		${CXXFLAGS} \
    -stdlib=libstdc++ \
		-mmacosx-version-min=10.6 \
		-I${ESRI_FILE_GBD_INCLUDE} \
		-I${JAVA_HOME}/include/ \
    -I${JAVA_HOME}/include/darwin \
		-c src/main/cxx/EsriFileGdb_wrap.cxx \
		-o ${TARGET_OBJ}
	

${TARGET_LIB}: target/o/libEsriFileGdbJni-${ARCH}-${OS}.o
  mkdir -p ${TARGET_DIR}
	clang++ \
		${LDFLAGS} \
    -stdlib=libstdc++ \
    -mmacosx-version-min=10.6 \
		-O2 \
		-fpic \
		-shared \
		-lFileGDBAPI \
		-lfgdbunixrtl \
		-L${ESRI_FILE_GBD_HOME}/lib/ \
		${TARGET_OBJ} \
		-o ${TARGET_LIB}
	

