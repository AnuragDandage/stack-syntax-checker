@echo off
if not exist build mkdir build
javac -d build *.java
if errorlevel 1 exit /b 1
java -cp build Main
