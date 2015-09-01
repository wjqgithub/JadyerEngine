@echo off
title 自动打包工具【玄玉制作】
color 02
set curdir=%~dp0

echo 开始打包Maven工程 =============================================================
echo;
call mvn clean install -DskipTests
echo;
echo Maven工程打包完毕 =============================================================

echo;
echo 开始拷贝War包至桌面 ===========================================================
echo;
if exist "%userprofile%\Desktop\" (
    xcopy %curdir%\JadyerEngine-web\target\*.war %userprofile%\Desktop\ /Y
    c:
    cd %userprofile%\Desktop\
) else if exist "%userprofile%\桌面\" (
    xcopy %curdir%\JadyerEngine-web\target\*.war %userprofile%\桌面\ /Y
    c:
    cd %userprofile%\桌面\
)
if exist "engine.war" (
    del engine.war /Q
)
ren JadyerEngine*.war engine.war
echo;
echo War包已经拷贝至桌面 ===========================================================

echo;
pause