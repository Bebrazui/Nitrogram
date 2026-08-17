@echo off
setlocal
set ANDROID_JAR=C:\Android\sdk\platforms\android-34\android.jar
set D8=C:\Android\sdk\build-tools\34.0.0\d8.bat
set CLANG=C:\Android\sdk\ndk\27.2.12479018\toolchains\llvm\prebuilt\windows-x86_64\bin\clang.exe
set JAVAC=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot\bin\javac.exe
set JAR=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot\bin\jar.exe
set PYTHON=python
set MOD_ID=motionblur

if not exist dexout mkdir dexout
if not exist dexbuild mkdir dexbuild
call "%JAVAC%" -encoding UTF-8 -cp "%ANDROID_JAR%;sdk.jar" -d dexout ModEntry.java
if errorlevel 1 goto :fail
call "%JAR%" cf classes.jar -C dexout .
call "%D8%" --output dexbuild --min-api 21 classes.jar
if errorlevel 1 goto :fail
call %PYTHON% genheader.py
if errorlevel 1 goto :fail
call "%CLANG%" --target=aarch64-linux-android29 -shared -fPIC -o lib%MOD_ID%_fx.so mod.c
if errorlevel 1 goto :fail
echo Build OK: lib%MOD_ID%_fx.so
goto :eof
:fail
echo BUILD FAILED
exit /b 1
