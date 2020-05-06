@echo off

where java >nul 2>nul
SET javaFound=%errorlevel%

IF DEFINED JAVA_HOME IF EXIST "%JAVA_HOME%\bin\java.exe" (
	REM ECHO Using Java from JAVA_HOME
	SET _java="%JAVA_HOME%\bin\java.exe"
) ELSE (
	IF %javaFound%==0 (
		REM ECHO "Using Java runtime from search path."
		SET _java=java
	) ELSE (
		GOTO :MissingJava
	)
)

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

:MissingJava
ECHO "No Java runtime available. Make java available to the path or set JAVA_HOME!"
SET ERRNO=1
GOTO :END

:OkClassPath

CD %currentDir%

%_java% -Xms64m -Xmx256m -Dlog4j.configuration=../lib/log4j.xml -classpath "%CLASSPATH%" com.axway.apim.APIExportApp %*
SET ERRNO=%ERRORLEVEL%

:END
SET CLASSPATH=%bkpClassPath%

EXIT /B %ERRNO%
