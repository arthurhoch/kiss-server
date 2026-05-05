#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "usage: $0 SERVER_NAME" >&2
  echo "examples:" >&2
  echo "  BASE_URL=http://127.0.0.1:8080 $0 kiss-server-nio-worker-fast-static" >&2
  echo "  BASE_URL=http://127.0.0.1:8080 $0 kiss-server-nio-direct-fast-static" >&2
  echo "  BASE_URL=http://127.0.0.1:8080 $0 kiss-server-nio-virtual-threads-jdk21-fast-static" >&2
  echo "  BASE_URL=http://127.0.0.1:8080 $0 undertow" >&2
  echo "  BASE_URL=http://127.0.0.1:8080 $0 vertx" >&2
  exit 2
fi

if ! command -v wrk >/dev/null 2>&1; then
  echo "wrk is required and was not found on PATH" >&2
  exit 127
fi

SERVER_NAME="$1"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
BASE_URL="${BASE_URL%/}"
TIMESTAMP="$(date -u +%Y%m%dT%H%M%SZ)"
OUT_DIR="${OUT_DIR:-"$SCRIPT_DIR/../results/$SERVER_NAME-$TIMESTAMP"}"
WARMUP_DURATION="10s"
MEASURE_DURATION="30s"

mkdir -p "$OUT_DIR"

run_scenario() {
  local name="$1"
  local threads="$2"
  local connections="$3"
  local path="$4"
  local lua_script="${5:-}"
  local url="$BASE_URL$path"
  local result_file="$OUT_DIR/$name.txt"
  local -a command=(wrk --latency -t"$threads" -c"$connections")

  if [[ -n "$lua_script" ]]; then
    command+=(-s "$SCRIPT_DIR/$lua_script")
  fi

  echo "warmup: $name"
  "${command[@]}" -d"$WARMUP_DURATION" "$url" >/dev/null

  echo "measure: $name -> $result_file"
  {
    echo "# server: $SERVER_NAME"
    echo "# url: $url"
    echo "# command: ${command[*]} -d$MEASURE_DURATION $url"
    echo "# timestamp_utc: $(date -u +%Y-%m-%dT%H:%M:%SZ)"
    echo
    "${command[@]}" -d"$MEASURE_DURATION" "$url"
  } | tee "$result_file"
}

run_scenario "health_t4_c100" 4 100 "/health"
run_scenario "health_t8_c500" 8 500 "/health"
run_scenario "hello_t8_c500" 8 500 "/hello"
run_scenario "json_t8_c500" 8 500 "/json"
run_scenario "users_t8_c500" 8 500 "/users/123?active=true"
run_scenario "post_echo_t8_c500" 8 500 "/echo" "post-echo.lua"
run_scenario "post_consume_t8_c500" 8 500 "/consume" "post-consume.lua"

echo
echo "results saved under: $OUT_DIR"
