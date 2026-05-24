#!/bin/sh
set -eu

if [ -n "${DATABASE_URL:-}" ] && [ -z "${SPRING_DATASOURCE_URL:-}" ]; then
  case "$DATABASE_URL" in
    jdbc:*)
      export SPRING_DATASOURCE_URL="$DATABASE_URL"
      ;;
    postgresql://*)
      export SPRING_DATASOURCE_URL="jdbc:$DATABASE_URL"
      ;;
    postgres://*)
      export SPRING_DATASOURCE_URL="jdbc:postgresql://${DATABASE_URL#postgres://}"
      ;;
  esac
fi

exec java ${JAVA_OPTS:-} -Dserver.port="${PORT:-8080}" -jar /app/app.jar
