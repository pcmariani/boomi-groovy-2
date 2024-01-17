#!/usr/bin/env bash

scriptName="boomiScriptTest.groovy"
workingDir="$(pwd)"

pushd $BOOMI_GROOVY_HOME >/dev/null
groovy "$BOOMI_GROOVY_HOME"/"$scriptName" --workingDir "$workingDir" "$@"
popd >/dev/null
