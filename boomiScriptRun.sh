#!/usr/bin/env bash

scriptName="boomiScriptRun.groovy"
workingDir="$(pwd)"

pushd "$BOOMI_GROOVY_HOME/src" >/dev/null
groovy "$BOOMI_GROOVY_HOME/src/$scriptName" --workingDir "$workingDir" "$@"
popd >/dev/null
