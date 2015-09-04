@echo off
set VSINSTALLDIR=C:\Program Files (x86)\Microsoft Visual Studio 11.0

set PATH=%VSINSTALLDIR%\Common7\IDE;%PATH%
set PATH=%VSINSTALLDIR%\VC\bin;%PATH%
nmake ARCH=x86 /f Makefile.nmake

set PATH=%VSINSTALLDIR%\VC\bin\x86_amd64;%PATH%
nmake ARCH=x86_64 /f Makefile.nmake

pause

