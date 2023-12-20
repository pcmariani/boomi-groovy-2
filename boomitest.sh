#!/usr/bin/env bash

nameOfBoomiGroovyScript="boomi2.groovy"

workingDir="$(pwd)"
# echo "workdingDir: $workingDir"
# echo "BOOMI_GROOVY_HOME: $BOOMI_GROOVY_HOME"

pushd $BOOMI_GROOVY_HOME >/dev/null

groovy "$BOOMI_GROOVY_HOME"/"$nameOfBoomiGroovyScript" -t $@ -w "$workingDir" -m "testResultsOnly"
exitCode="$?"

popd >/dev/null

if [[ "$exitCode" != "0" ]]; then
	exit 1
fi
