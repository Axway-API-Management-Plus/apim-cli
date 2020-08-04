#!/bin/sh

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]
then
        #### echo "Using java given in JAVA_HOME"
        _java=$JAVA_HOME/bin/java
elif command -v java
then
        #### echo "Using java found in the PATH"
        _java=java
else
        echo "No Java runtime available. Make java available to the path or set JAVA_HOME!"
        exit 1
fi

programDir="$( cd "$(dirname "$0")" ; pwd -P )"

currentDir=$PWD

cd "$programDir/.."

CP=$PWD/lib:$PWD/conf
for jar in lib/*
do
        CP=$CP:$PWD/$jar
done

echo ""

cd $currentDir

"$_java" -Xms64m -Xmx256m -Dlog4j.configuration=../lib/log4j.xml -classpath "$CP" com.axway.apim.cli.APIManagerCLI "${@}"
rc=$?
if [ $rc -eq 10 ];then
        echo "No changes detected. Existing with RC: 0"
        exit 0
fi
exit $rc