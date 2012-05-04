WRAPFILE      = target\cxx\EsriFileGdb_wrap.cxx

ESRI_FILE_GBD_HOME=C:\Apps\EsriFileGdb-1.2
ESRI_FILE_GBD_INCLUDE=C:\Apps\EsriFileGdb-1.2\include
ESRI_FILE_GBD_LIB=C:\Apps\EsriFileGdb-1.2\lib\winnt\x86
TOOLS         =C:\Program Files\Microsoft Visual Studio 10.0\VC
WIN_SDK				=C:\Program Files\Microsoft SDKs\Windows\v7.0a
CC            ="$(TOOLS)\bin\cl.exe"
LINK          ="$(TOOLS)\bin\link.exe"
INCLUDE32     ="-I$(TOOLS)\include"
MACHINE       =IX86
OS						=winnt
ARCH					=x86
DEBUG					=0

# Linker options
LINKFLAGS      = /NODEFAULTLIB /NOLOGO /MACHINE:$(MACHINE) -entry:_DllMainCRTStartup@12 /DLL
WINLIBS   = kernel32.lib advapi32.lib user32.lib gdi32.lib comdlg32.lib winspool.lib

CFLAGS        = /c /nologo /EHsc
!IF "$(DEBUG)" == "1"
CFLAGS   = /D_DEBUG $(CFLAGS) /Od /Zi /MTd
LINKFLAGS = $(LINKFLAGS) /DEBUG
DLLIBC    = msvcrtd.lib oldnames.lib libcpmtd.lib
LIBS      = $(DLLIBC) $(WINLIBS) FileGDBAPID.lib
!ELSE
CFLAGS    = $(CFLAGS) /O2 /MT
LINKFLAGS = $(LINKFLAGS) /RELEASE 
DLLIBC    = msvcrt.lib oldnames.lib libcpmt.lib
LIBS      = $(DLLIBC) $(WINLIBS) FileGDBAPI.lib 
!ENDIF

JAVA_INCLUDE    = "-I$(JAVA_HOME)\include" "-I$(JAVA_HOME)\include\win32"

TARGET_OBJ=target\o\EsriFileGdbJni-$(ARCH)-$(OS).obj
TARGET_LIB=src\main\resources\native\EsriFileGdbJni-$(ARCH)-$(OS).dll

all: clean $(TARGET_LIB)
	
clean:
	del /q target\o
  del /q $(TARGET_OBJ) $(TARGET_LIB)
	
init:
	mkdir /p target\cxx
	mkdir /p target\o

target/cxx/EsriFileGdb_wrap.cxx:

$(TARGET_OBJ): $(WRAPFILE)
  $(CC) $(CFLAGS) $(JAVA_INCLUDE) $(INCLUDE32) -I$(ESRI_FILE_GBD_INCLUDE) $(WRAPFILE) /Fo$(TARGET_OBJ)
	

$(TARGET_LIB): $(TARGET_OBJ)
	set LIB=$(TOOLS)\lib;$(WIN_SDK)\lib;$(ESRI_FILE_GBD_LIB)
	$(LINK) $(LINKFLAGS) -out:$(TARGET_LIB) $(LIBS) $(TARGET_OBJ)

