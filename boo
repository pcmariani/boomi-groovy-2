#!/usr/bin/env bash

usage="Usage: b (run|test|init) <args>"

if [[ $# -eq 0 ]]; then
	echo "$usage"
	exit 1
fi

if [ "$1" == "run" ]; then
	scriptName="BoomiScriptRun.groovy"
	shift
elif [ "$1" == "test" ]; then
	scriptName="BoomiScriptTest.groovy"
	shift
elif [ "$1" == "init" ]; then
	scriptName="BoomiScriptInit.groovy"
	shift
else
	echo "$usage"
	exit 1
fi

workingDir="$(pwd)"

pushd "$BOOMI_GROOVY_HOME/src" >/dev/null
# echo "$BOOMI_GROOVY_HOME/src/$scriptName" --workingDir "$workingDir" "$@"
groovy "$BOOMI_GROOVY_HOME/src/$scriptName" --workingDir "$workingDir" "$@"
popd >/dev/null
