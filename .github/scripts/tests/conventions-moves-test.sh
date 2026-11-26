#!/usr/bin/env bash
# Tests for conventions-moves.sh. Portable to bash 3.2.
set -euo pipefail

here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
script="$here/../conventions-moves.sh"
fx="$here/fixtures"
failures=0
assert_eq() {
  if [ "$1" = "$2" ]; then echo "ok   $3"; else echo "FAIL $3: expected '$1', got '$2'"; failures=$((failures + 1)); fi
}

tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT

"$script" versions "$fx/platform-old.pom" "$fx/catalog-old.toml" > "$tmp/old"
"$script" versions "$fx/platform-new.pom" "$fx/catalog-new.toml" > "$tmp/new"

assert_eq "com.example:bom 2.1.0" "$(grep ':bom ' "$tmp/old")" "import BOM entry is listed"
assert_eq "org.codehaus.groovy:groovy-ant 3.0.25" "$(grep groovy-ant "$tmp/old")" "version.ref resolved"
assert_eq "com.example:literal 1.0.0" "$(grep literal "$tmp/old")" "literal catalog version"
assert_eq "0" "$(grep -c 'managed' "$tmp/old" || true)" "empty catalog version skipped"
assert_eq "0" "$(grep -c 'x.y' "$tmp/old" || true)" "plugins ignored"
assert_eq "$(LC_ALL=C sort "$tmp/old")" "$(cat "$tmp/old")" "output is sorted"

expected="com.example:bom 2.1.0 -> 3.0.0
com.example:major-up 1.4.1 -> 2.0.0
com.example:pre 1.2.0-beta.2 -> 1.3.0
com.example:zero-minor 0.8.0 -> 0.9.0
com.google.guava:guava-major 33.8.0-jre -> 34.0.0-jre
org.codehaus.groovy:groovy-ant 3.0.25 -> 4.0.1"
assert_eq "$expected" "$("$script" breaking "$tmp/old" "$tmp/new")" "breaking moves (and only those)"

assert_eq "" "$("$script" breaking "$tmp/old" "$tmp/old")" "no moves, no output"

if [ "$failures" -ne 0 ]; then echo "$failures failed"; exit 1; fi
echo "all passed"
