#!/usr/bin/env sh
set -eu

: "${PORT:=8080}"

sed -i 's/<Server port="8005" shutdown="SHUTDOWN">/<Server port="-1" shutdown="SHUTDOWN">/' "$CATALINA_HOME/conf/server.xml"

sed -i "s/port=\"8080\" protocol=\"HTTP\/1.1\"/port=\"${PORT}\" protocol=\"HTTP\/1.1\"/" "$CATALINA_HOME/conf/server.xml"
exec catalina.sh run
