INTERFACE     = ..\swig\EsriFileGdbAPI.i
WRAPFILE      = ..\cxx\EsriFileGdb_wrap.cxx

TOOLS         = C:\Program Files\Microsoft Visual Studio 10.0\VC
WIN_SDK				= C:\Program Files\Microsoft SDKs\Windows\v7.0a
TARGET        = ..\lib\native\EsriFileGdbJni-x86-winnt.dll
CC            = "$(TOOLS)\bin\cl.exe"
LINK          = "$(TOOLS)\bin\link.exe"
INCLUDE32     = -I$(TOOLS)\include
MACHINE       = IX86

# C Library needed to build a DLL

DLLIBC        = msvcrt.lib oldnames.lib libcpmt.lib

# Windows libraries that are apparently needed
WINLIB        = FileGDBAPI.lib kernel32.lib advapi32.lib user32.lib gdi32.lib comdlg32.lib winspool.lib

# Libraries common to all DLLs
LIBS          = $(DLLIBC) $(WINLIB) 

# Linker options
LINKFLAGS      = /NODEFAULTLIB /NOLOGO \
             /MACHINE:$(MACHINE) -entry:_DllMainCRTStartup@12 /DLL

CFLAGS        = /c /nologo /EHsc
!IF "$(DEBUG)" == "1"
CLFLAGS   = /D_DEBUG $(CLFLAGS) /Od /Zi /f
LINKFLAGS = $(LINKFLAGS) /DEBUG
LIBS      = $(DLLIBC) $(WINLIB)
!ELSE
CLFLAGS   = $(CLFLAGS) /O2 
LINKFLAGS = $(LINKFLAGS) /RELEASE 
LIBS      = $(DLLIBC) $(WINLIB)
!ENDIF

JAVA_INCLUDE    = "-I$(JAVA_HOME)\include" "-I$(JAVA_HOME)\include\win32"
ESRI_INCLUDE    = -I..\include

all: EsriFileGdbJni-x86-winnt.dll

EsriFileGdbJni-x86-winnt.dll: EsriFileGdb_wrap.obj
	set LIB=$(TOOLS)\lib;$(WIN_SDK)\lib
	$(LINK) $(LINKFLAGS) -out:EsriFileGdbJni-x86-winnt.dll $(LIBS) EsriFileGdb_wrap.obj
	copy EsriFileGdbJni-x86-winnt.dll $(TARGET)

EsriFileGdb_wrap.obj: ..\cxx\EsriFileGdb_wrap.cxx
  $(CC) $(CFLAGS) $(JAVA_INCLUDE) $(ESRI_INCLUDE) $(WRAPFILE)

clean:
  del EsriFileGdb* 