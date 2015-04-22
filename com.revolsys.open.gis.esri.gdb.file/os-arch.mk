# requires the following libraries to be installed
# /usr/lib/libfgdbunixrtl.so
#/usr/lib/libFileGDBAPI.so

ESRI_FILE_GBD_HOME=/opt/EsriFileGdb/1.4/${OS}/${ARCH}
ESRI_FILE_GBD_INCLUDE=/opt/EsriFileGdb/1.4/${OS}/${ARCH}/include
JAVA_HOME=`/usr/libexec/java_home -v 1.8`
CFG=Release
CXX=clang++

include $(ESRI_FILE_GBD_INCLUDE)/make.include
TARGET_OBJ=target/o/libEsriFileGdbJni-${ARCH}-${OS}.o
TARGET_DIR=src/main/resources/native/${OS}/${ARCH}
TARGET_LIB=${TARGET_DIR}/libEsriFileGdbJni.${EXT}

CXXFLAGS+=-W -fexceptions $(CXXDEF) -I$(ESRI_FILE_GBD_INCLUDE) $(CXXOTHER)
LD=$(CXX) $(CXXFLAGS)
LDFLAGS+= -L$(ESRI_FILE_GBD_HOME)/lib/
LIBS+= -lFileGDBAPI

all: clean ${TARGET_LIB}
	
clean:
	rm -f ${TARGET_OBJ} ${TARGET_LIB}

src/main/cxx/EsriFileGdb_wrap.cxx:

${TARGET_OBJ}: src/main/cxx/EsriFileGdb_wrap.cxx
	mkdir -p target/o
	cp $(ESRI_FILE_GBD_HOME)/lib/* src/main/resources/native/$(OS)/$(ARCH)
	${CXX} \
	${CXXFLAGS} ${CPPFLAGS)}\
	-I${JAVA_HOME}/include/ \
	-I${JAVA_HOME}/include/darwin \
	-c src/main/cxx/EsriFileGdb_wrap.cxx \
	-o ${TARGET_OBJ}
	

${TARGET_LIB}: target/o/libEsriFileGdbJni-${ARCH}-${OS}.o
	mkdir -p ${TARGET_DIR}
	$(LD) \
	$(LDFLAGS) \
	-shared \
	-o ${TARGET_LIB} \
	${TARGET_OBJ} $(LIBS)
