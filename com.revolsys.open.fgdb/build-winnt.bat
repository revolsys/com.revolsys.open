@echo off
set VCINSTALLDIR=E:\Apps\VisualC_2013

set PATH=%VCINSTALLDIR%\bin;%PATH%
nmake ARCH=x86 /f Makefile.win.nmake

set PATH=%VCINSTALLDIR%\bin\amd64;%PATH%
nmake ARCH=x86_64 /f Makefile.win.nmake

pause
