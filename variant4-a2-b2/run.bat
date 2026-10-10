@echo off
rem Builds and runs this variant.
rem Usage: run.bat                 (uses config.txt in this folder)
rem        run.bat other-config.txt
cd /d "%~dp0"
javac --release 17 -Xlint:all -d out src\primesearch\*.java || exit /b 1
java -cp out primesearch.PrimeSearch %*
