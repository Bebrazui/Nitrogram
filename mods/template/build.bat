@echo off
setlocal
rem ===== Пути (подправь под свою систему) =====
set ANDROID_JAR=C:\Android\sdk\platforms\android-34\android.jar
set D8=C:\Android\sdk\build-tools\34.0.0\d8.bat
set CLANG=C:\Android\sdk\ndk\27.2.12479018\toolchains\llvm\prebuilt\windows-x86_64\bin\clang.exe
set JAVAC=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot\bin\javac.exe
set JAR=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot\bin\jar.exe
set PYTHON=python
rem Имя мода (итоговый файл: lib<MOD_ID>_fx.so)
set MOD_ID=mymod

if not exist dexout mkdir dexout
if not exist dexbuild mkdir dexbuild

echo [1/4] compile java
call "%JAVAC%" -encoding UTF-8 -cp "%ANDROID_JAR%;sdk.jar" -d dexout ModEntry.java
if errorlevel 1 goto :fail

echo [2/4] d8 -^> classes.dex
call "%JAR%" cf classes.jar -C dexout .
call "%D8%" --output dexbuild --min-api 21 classes.jar
if errorlevel 1 goto :fail

echo [3/4] embed dex -^> dex_payload.h
call %PYTHON% genheader.py
if errorlevel 1 goto :fail

echo [4/4] clang -^> lib%MOD_ID%_fx.so
call "%CLANG%" --target=aarch64-linux-android29 -shared -fPIC -o lib%MOD_ID%_fx.so mod.c
if errorlevel 1 goto :fail

echo.
echo Build OK: lib%MOD_ID%_fx.so
goto :eof

:fail
echo.
echo BUILD FAILED
exit /b 1
