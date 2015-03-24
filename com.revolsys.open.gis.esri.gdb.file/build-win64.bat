call "C:\Program Files (x86)\Microsoft Visual Studio 12.0\VC\vcvarsall.bat" amd64
nmake ARCH=x86_64 /f Makefile.win.nmake
pause
