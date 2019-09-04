@echo off

IF NOT DEFINED JAVA_HOME GOTO :MissingJavaHome

SET currentDir=%cd%
SET programDir=%~dp0
SET bkpClassPath=%CLASSPATH%

CD %programDir%\..
SET CLASSPATH=%programDir%..\lib;%programDir%..\conf

FOR /R ./lib %%a in (*.jar) DO CALL :AddToPath %%a

GOTO :OkClassPath

:AddToPath
SET CLASSPATH=%1;%CLASSPATH%
GOTO :EOF

:MissingJavaHome
ECHO "Environment variable JAVA_HOME not set!"
SET ERRNO=1
GOTO :END

:OkClassPath

CD %currentDir%

"%JAVA_HOME%\bin\java" -Xms64m -Xmx256m -classpath "%CLASSPATH%" com.axway.apim.ExportApp %*
SET ERRNO=%ERRORLEVEL%

:END
SET CLASSPATH=%bkpClassPath%

EXIT /B %ERRNO%
