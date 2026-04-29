#!/usr/bin/env bash
# Start a self-contained Tomcat instance that serves the USC Marketplace webapp.
#
# Usage:
#   ./Servers/run.sh start    # start Tomcat in the background
#   ./Servers/run.sh stop     # stop the Tomcat instance
#   ./Servers/run.sh run      # run Tomcat in the foreground (Ctrl+C to stop)
#   ./Servers/run.sh build    # recompile Java sources and redeploy classes
#
# After 'start', open http://localhost:8080/Marketplace/

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APP_SRC="$PROJECT_ROOT/Marketplace"
SERVERS_DIR="$PROJECT_ROOT/Servers"

export CATALINA_HOME="${CATALINA_HOME:-/opt/homebrew/Cellar/tomcat/11.0.21/libexec}"
export CATALINA_BASE="$SERVERS_DIR/catalina-base"
export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 17 2>/dev/null || /usr/libexec/java_home)}"

WEBAPP_DIR="$CATALINA_BASE/webapps/Marketplace"

build() {
  echo "[build] compiling Java sources..."
  rm -rf "$APP_SRC/build/classes"
  mkdir -p "$APP_SRC/build/classes"
  CP="$APP_SRC/src/main/webapp/WEB-INF/lib/mysql-connector-j-9.7.0.jar"
  CP="$CP:$CATALINA_HOME/lib/servlet-api.jar"
  CP="$CP:$CATALINA_HOME/lib/jsp-api.jar"
  CP="$CP:$CATALINA_HOME/lib/el-api.jar"
  find "$APP_SRC/src/main/java" -name '*.java' > /tmp/usc-marketplace-sources.txt
  javac -d "$APP_SRC/build/classes" -cp "$CP" @/tmp/usc-marketplace-sources.txt

  echo "[build] deploying webapp..."
  rm -rf "$WEBAPP_DIR"
  mkdir -p "$WEBAPP_DIR"
  cp -R "$APP_SRC/src/main/webapp/." "$WEBAPP_DIR/"
  mkdir -p "$WEBAPP_DIR/WEB-INF/classes"
  cp -R "$APP_SRC/build/classes/." "$WEBAPP_DIR/WEB-INF/classes/"
  cp "$APP_SRC/src/main/resources/db.properties" "$WEBAPP_DIR/WEB-INF/classes/"
  echo "[build] deployed to $WEBAPP_DIR"
}

start() {
  if [ ! -d "$WEBAPP_DIR" ]; then
    build
  fi
  echo "[start] CATALINA_BASE=$CATALINA_BASE"
  "$CATALINA_HOME/bin/catalina.sh" start
  echo "[start] Tomcat starting. Open http://localhost:8080/Marketplace/"
  echo "[start] Logs: $CATALINA_BASE/logs/catalina.out"
}

stop() {
  "$CATALINA_HOME/bin/catalina.sh" stop || true
}

run() {
  if [ ! -d "$WEBAPP_DIR" ]; then
    build
  fi
  echo "[run] CATALINA_BASE=$CATALINA_BASE"
  exec "$CATALINA_HOME/bin/catalina.sh" run
}

case "${1:-start}" in
  start) start ;;
  stop)  stop ;;
  run)   run ;;
  build) build ;;
  *)     echo "Usage: $0 {start|stop|run|build}" >&2; exit 1 ;;
esac
