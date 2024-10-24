#!/bin/sh

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]
then
        #### echo "Using java given in JAVA_HOME"
        _java=$JAVA_HOME/bin/java
else
        #### echo "Using java found in the PATH"
        _java=java
fi
if [ -z $_java ] ; then
        echo "No Java runtime available. Make java available to the path or set JAVA_HOME!"
        exit 1
fi

if [ -n "$AXWAY_APIM_CLI_HOME" ]
then
	programDir="$AXWAY_APIM_CLI_HOME"
else
	programDir="$( cd "$(dirname "$0")"; cd .. ; pwd -P )"
fi

currentDir=$PWD

cd "$programDir"

CP=$PWD/lib:$PWD/conf
for jar in lib/*
do
        CP=$CP:$PWD/$jar
done

cd "$currentDir"

### Enable Debugging of http headers and payload
### "$_java" -Xms64m -Xmx256m -Dlog4j.configurationFile=../lib/log4j2.xml  -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog -Dorg.apache.commons.logging.simplelog.showdatetime=true -Dorg.apache.commons.logging.simplelog.log.org.apache.http=DEBUG -classpath "$CP" com.axway.apim.cli.APIManagerCLI "${@}"


"$_java" -Xms64m -Xmx256m -Dlog4j.configurationFile=../lib/log4j2.xml -classpath "$CP" com.axway.apim.cli.APIManagerCLI "${@}"
rc=$?
exit $rc
