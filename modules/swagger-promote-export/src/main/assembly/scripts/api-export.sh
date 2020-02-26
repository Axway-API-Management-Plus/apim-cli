#!/bin/sh

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]
then
        #### echo "Using java given in JAVA_HOME"
        _java=$JAVA_HOME/bin/java
elif type -p java
then
        #### echo "Using java found in the PATH"
        _java=java
else
        echo "No Java runtime available. Make java available to the path or set JAVA_HOME!"
        exit 1
fi

#programDir="${0%/*}"
programDir="$( cd "$(dirname "$0")" ; pwd -P )"

cd $programDir/..

CP=lib:conf
for jars in lib/*
do
        CP=$CP:$jars
done

echo ""

"$_java" -Xms64m -Xmx256m -classpath "$CP" com.axway.apim.ExportApp "${@}"
rc=$?
exit $rc
