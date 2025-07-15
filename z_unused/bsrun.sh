#!/usr/bin/env bash

scriptName="BoomiScriptRun.groovy"
workingDir="$(pwd)"

pushd "$BOOMI_GROOVY_HOME/src" >/dev/null
echo "HEOO"
echo "$COLUMNS"
export termWidth="$COLUMNS"
groovy "$BOOMI_GROOVY_HOME/src/$scriptName" --workingDir "$workingDir" "$@"
popd >/dev/null
