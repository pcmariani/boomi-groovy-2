#!/usr/bin/env bash

nameOfBoomiGroovyScript="BoomiGroovy.groovy"

workingDir="$(pwd)"
# echo "workdingDir: $workingDir"
# echo "BOOMI_GROOVY_HOME: $BOOMI_GROOVY_HOME"

pushd $BOOMI_GROOVY_HOME >/dev/null

# if [[ "$1" =~ ^--?[sdp] ]]; then
# 	echo "legacy mode"
# 	mode="legacy"
# 	args="$@"
# elif [[ "$1" =~ ^- ]]; then
# 	echo "catching error"
# 	args="-h"
# else
# 	echo "$1 mode"
# 	mode="$1"
# 	shift
# 	args="-testfile ___$@"
# fi

# echo "args $args"

groovy "$BOOMI_GROOVY_HOME"/"$nameOfBoomiGroovyScript" --working-dir "$workingDir" "$@"

exitCode="$?"

popd >/dev/null

if [[ "$exitCode" != "0" ]]; then
	exit 1
fi
