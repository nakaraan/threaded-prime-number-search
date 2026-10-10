@echo off
rem Builds and runs all four variants, one after another, each with its own config.txt
for %%v in (variant1-a1-b1 variant2-a2-b1 variant3-a1-b2 variant4-a2-b2) do (
    echo.
    echo ========== %%v ==========
    call "%~dp0%%v\run.bat"
    pause
)
