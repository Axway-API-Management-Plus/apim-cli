@echo off

where java >nul 2>nul
SET javaFound=%errorlevel%

IF DEFINED JAVA_HOME IF EXIST "%JAVA_HOME%\bin\java.exe" (
	REM ECHO Using Java from JAVA_HOME
	SET _java="%JAVA_HOME%\bin\java.exe"
)
IF NOT DEFINED _java (
	IF %javaFound%==0 (
		REM ECHO "Using Java runtime from search path."
		SET _java=java
	) ELSE (
		GOTO :MissingJava
	)
)

SET currentDir=%cd%

IF DEFINED AXWAY_APIM_CLI_HOME (
	SET programDir=%AXWAY_APIM_CLI_HOME%\
	REM ECHO Using AXWAY_APIM_CLI_HOME
) ELSE (
	SET programDir=%~dp0..
	REM ECHO Using Relative programDir
)
SET bkpClassPath=%CLASSPATH%

CD "%programDir%"
SET CLASSPATH="%programDir%\lib";"%programDir%\conf"

FOR /R ./lib %%a in (*.jar) DO CALL :AddToPath "%%a"

GOTO :OkClassPath

:AddToPath
SET CLASSPATH=%1;%CLASSPATH%
GOTO :EOF

:MissingJava
ECHO "No Java runtime available. Make java available to the path or set JAVA_HOME!"
SET ERRNO=1
GOTO :END

:OkClassPath

CD "%currentDir%"

%_java% -Xms64m -Xmx256m -Dlog4j.configuration=../lib/log4j.xml -classpath %CLASSPATH% com.axway.apim.cli.APIManagerCLI %*
SET ERRNO=%ERRORLEVEL%
IF %ERRNO% EQU 10 (
  ECHO "No changes detected. Existing with RC: 0"
  SET ERRNO=0
  GOTO :END
)

:END
SET CLASSPATH=%bkpClassPath%

EXIT /B %ERRNO%
