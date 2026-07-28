#!/bin/zsh

# Do not lower the priority of background processes launched by this script.
setopt NO_BG_NICE

# Always run from the project folder, even when this file is double-clicked.
cd -- "$(dirname -- "$0")" || exit 1

APP_URL="http://localhost:8080"
JAR_FILE="target/thunderstorm-1.0.0.jar"

if ! command -v java >/dev/null 2>&1; then
  echo "Java 17 or newer is required to run Thunderstorm."
  echo "Install Java, then double-click this launcher again."
  read -r "?Press Return to close..."
  exit 1
fi

# If Thunderstorm is already running, just bring it up in the browser.
if curl --silent --fail "$APP_URL" >/dev/null 2>&1; then
  open "$APP_URL"
  exit 0
fi

# Build the application the first time, or after its source files change.
if [[ ! -f "$JAR_FILE" ]] || find pom.xml src -type f -newer "$JAR_FILE" -print -quit | grep -q .; then
  echo "Preparing Thunderstorm..."
  ./mvnw --quiet -DskipTests package
  if [[ $? -ne 0 ]]; then
    echo
    echo "Thunderstorm could not be built."
    read -r "?Press Return to close..."
    exit 1
  fi
fi

echo "Starting Thunderstorm..."
echo "Keep this window open while you use the application."
echo

java -jar "$JAR_FILE" &
APP_PID=$!

cleanup() {
  kill "$APP_PID" >/dev/null 2>&1
}
trap cleanup EXIT INT TERM

# Open the browser as soon as the local server is ready.
(
  for attempt in {1..60}; do
    if curl --silent --fail "$APP_URL" >/dev/null 2>&1; then
      open "$APP_URL"
      exit 0
    fi
    sleep 1
  done
  echo "Thunderstorm did not become ready within 60 seconds."
) &

wait "$APP_PID"
